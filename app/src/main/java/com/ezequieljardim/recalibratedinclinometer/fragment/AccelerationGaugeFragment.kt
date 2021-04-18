package com.ezequieljardim.recalibratedinclinometer.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ezequieljardim.recalibratedinclinometer.R
import com.ezequieljardim.recalibratedinclinometer.gauge.GaugeAcceleration
import com.ezequieljardim.recalibratedinclinometer.viewmodel.SensorViewModel

/*
* AccelerationExplorer
* Copyright 2018 Kircher Electronics, LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/**
 * Created by kaleb on 7/8/17.
 */
class AccelerationGaugeFragment : Fragment(), Orientation.Listener {

//    var senseManager: SensorManager? = null
//    lateinit var linearAcceleration: Sensor

    var xDegreesTv: TextView? = null
    var yDegreesTv: TextView? = null
    var zDegreesTv: TextView? = null

    private var gaugeAcceleration: GaugeAcceleration? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var acceleration: FloatArray? = null

//    private var accelerometerReading = FloatArray(3)
//    private var magnetometerReading = FloatArray(3)
//    private var rotationMatrix = FloatArray(9)
//    private var orientationAngles = FloatArray(3)

    private var mOrientation: Orientation? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                updateAccelerationGauge()
                handler!!.postDelayed(this, 20)
            }
        }
        acceleration = FloatArray(4)

        mOrientation = Orientation(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_acceleration_gauge, container, false)
        gaugeAcceleration = view.findViewById(R.id.gauge_acceleration)

        xDegreesTv = view.findViewById(R.id.orientation_x)
        yDegreesTv = view.findViewById(R.id.orientation_y)
        zDegreesTv = view.findViewById(R.id.orientation_z)


//        senseManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager? // initialization of sensorManager
//        if (senseManager != null) {
//            senseManager?.registerListener(rotationListener, senseManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL)
//        }

        return view
    }

    override fun onPause() {
        handler!!.removeCallbacks(runnable)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        initViewModel()
        handler!!.post(runnable)
    }

    override fun onStart() {
        super.onStart()
        mOrientation!!.startListening(this)
    }

    override fun onStop() {
        super.onStop()
        mOrientation!!.stopListening()
    }

    private fun updateAccelerationGauge() {
        gaugeAcceleration!!.updatePoint(acceleration!![0], acceleration!![1])
        //        System.out.println("Accelerations (x,y) = (" + acceleration[0] + "," + acceleration[1] + ")");
    }

    private fun initViewModel() {
        val model = ViewModelProviders.of(activity!!).get(SensorViewModel::class.java)
        model.accelerationSensorLiveData.removeObservers(this)
        model.accelerationSensorLiveData.observe(this, { floats -> acceleration = floats })
    }

    private fun parseFloat(value: Float, format: String = "%.6f"): String {
        return String.format(java.util.Locale.US, format, value)
    }

    private fun parseDouble(value: Double, format: String = "%.6f"): String {
        return String.format(java.util.Locale.US, format, value)
    }

    override fun onOrientationChanged(yaw: Float, pitch: Float, roll: Float) {
        Log.d("Eshe", "yaw ${yaw}, pitch ${pitch}, roll ${roll}")

        xDegreesTv!!.text = parseFloat(yaw, "%.0f")
        yDegreesTv!!.text = parseFloat(roll, "%.0f")
        zDegreesTv!!.text = parseFloat(pitch, "%.0f")
    }


//    fun updateOrientationAngles() {
//        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
//        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)
//        val xDegrees = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
//        val xAngle = round(xDegrees * 100) / 100
//
//        val yDegrees = (Math.toDegrees(orientation[1].toDouble()) + 360.0) % 360.0
//        val yAngle = round(yDegrees * 100) / 100
//
//        val zDegrees = (Math.toDegrees(orientation[2].toDouble()) + 360.0) % 360.0
//        val zAngle = round(zDegrees * 100) / 100
//
////        Log.d("Eshe", "Orientation angles x, y, z: ${xAngle}, ${yAngle}, ${zAngle}")
//        xDegreesTv!!.text = parseDouble(xAngle, "%.0f")
//        yDegreesTv!!.text = parseDouble(yAngle, "%.0f")
//        zDegreesTv!!.text = parseDouble(zAngle, "%.0f")
//    }
//
//    private val rotationListener = object : SensorEventListener {
//        override fun onSensorChanged(event: SensorEvent) {
//            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
//            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
//                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
//            }
//            updateOrientationAngles()
//
//        }
//
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//            // Do nothing
//        }
//    }

}