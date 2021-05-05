package com.ezequieljardim.recalibratedinclinometer.fragment

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager

const val radsToDegree = 57.2959

class Orientation(activity: Activity) : SensorEventListener {
    interface Listener {
        fun onOrientationChanged(yaw: Float, pitch: Float, roll: Float, yawRad: Float, pitchRad: Float, rollRad: Float)
    }

    private val mWindowManager: WindowManager = activity.window.windowManager
    private val mSensorManager: SensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    private val mRotationSensor: Sensor?

    private var mLastAccuracy = 0
    private var mListener: Listener? = null

    fun startListening(listener: Listener) {
        if (mListener === listener) {
            return
        }
        mListener = listener
        if (mRotationSensor == null) {
            return
        }
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS)
    }

    fun stopListening() {
        mSensorManager.unregisterListener(this)
        mListener = null
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (mListener == null) {
            return
        }
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return
        }
        if (event.sensor == mRotationSensor) {
            updateOrientation(event.values)
        }
    }

    private fun updateOrientation(rotationVector: FloatArray) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        val worldAxisForDeviceAxisX = SensorManager.AXIS_X
        val worldAxisForDeviceAxisY = SensorManager.AXIS_Y

        // Leave this uncommented since won't be used
//        when (mWindowManager.defaultDisplay.rotation) {
//            Surface.ROTATION_0 -> {
//                worldAxisForDeviceAxisX = SensorManager.AXIS_X
//                worldAxisForDeviceAxisY = SensorManager.AXIS_Z
//            }
//            Surface.ROTATION_90 -> {
//                worldAxisForDeviceAxisX = SensorManager.AXIS_Z
//                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X
//            }
//            Surface.ROTATION_180 -> {
//                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X
//                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z
//            }
//            Surface.ROTATION_270 -> {
//                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z
//                worldAxisForDeviceAxisY = SensorManager.AXIS_X
//            }
//            else -> {
//                worldAxisForDeviceAxisX = SensorManager.AXIS_X
//                worldAxisForDeviceAxisY = SensorManager.AXIS_Z
//            }
//        }
        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                worldAxisForDeviceAxisY, adjustedRotationMatrix)

        // Transform rotation matrix into yaw/pitch/roll
        val orientation = FloatArray(3)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)

        // Convert radians to degrees

        val yaw = (orientation[0] * radsToDegree).toFloat()
        val pitch = (orientation[1] * -radsToDegree).toFloat()
        val roll = (orientation[2] * radsToDegree).toFloat()

        mListener!!.onOrientationChanged(yaw, pitch, roll , orientation[0], orientation[1], orientation[2])
    }

    companion object {
        private const val SENSOR_DELAY_MICROS = 16 * 1000 // 16ms
    }

    init {
        // Can be null if the sensor hardware is not available
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) //TYPE_ROTATION_VECTOR)
    }
}