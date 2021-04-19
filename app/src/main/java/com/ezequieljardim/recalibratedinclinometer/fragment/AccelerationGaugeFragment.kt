package com.ezequieljardim.recalibratedinclinometer.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.ezequieljardim.recalibratedinclinometer.R
import com.ezequieljardim.recalibratedinclinometer.gauge.GaugeAcceleration
import com.ezequieljardim.recalibratedinclinometer.viewmodel.SensorViewModel
import kotlin.math.cos
import kotlin.math.sin

class AccelerationGaugeFragment : Fragment(), Orientation.Listener {
    lateinit var yawView: TextView
    lateinit var pitchView: TextView
    lateinit var rollView: TextView

    lateinit var storedYawView: TextView
    lateinit var storedPitchView: TextView
    lateinit var storedRollView: TextView

    lateinit var newXAccelerationTV: TextView
    lateinit var newYAccelerationTV: TextView
    lateinit var newZAccelerationTV: TextView

    lateinit var calibrationBtn: Button
    lateinit var resetBtn: Button

    private var gaugeAcceleration: GaugeAcceleration? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var acceleration: FloatArray? = null
    private var rotationDegrees: FloatArray? = null
    private var rotationRadians: FloatArray? = null

    private var calibrationDegrees: FloatArray? = null
    private var calibrationRadians: FloatArray? = null

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

        yawView = view.findViewById(R.id.yaw)
        pitchView = view.findViewById(R.id.pitch)
        rollView = view.findViewById(R.id.roll)

        storedYawView = view.findViewById(R.id.stored_yaw)
        storedPitchView = view.findViewById(R.id.stored_pitch)
        storedRollView = view.findViewById(R.id.stored_roll)

        newXAccelerationTV = view.findViewById(R.id.new_acc_x)
        newYAccelerationTV = view.findViewById(R.id.new_acc_y)
        newZAccelerationTV = view.findViewById(R.id.new_acc_z)

        calibrationBtn = view.findViewById(R.id.calibrate_button)

        calibrationBtn.setOnClickListener { storeRotationValues() }

        resetBtn = view.findViewById(R.id.reset_button)
        resetBtn.setOnClickListener { resetRotationValues() }

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

        if (calibrationRadians == null) {
            gaugeAcceleration!!.updatePoint(acceleration!![0], acceleration!![1])
        } else {
            val newAcc = FloatArray(3)
            val horizontalPitch = Math.PI / 2.0f
            val newPitch = calibrationRadians!![1]
            val rotRads = horizontalPitch - newPitch

            // Multiply acceleration with rotation matrix around X axis (pitch)
            newAcc[0] = acceleration!![0]
            newAcc[1] = (acceleration!![1] * cos(rotRads) + acceleration!![2] * -sin(rotRads)).toFloat()
            newAcc[2] = (acceleration!![1] * sin(rotRads) + acceleration!![2] * cos(rotRads)).toFloat()

            newXAccelerationTV.text = parseFloat(newAcc[0], "%.2f")
            newYAccelerationTV.text = parseFloat(newAcc[1], "%.2f")
            newZAccelerationTV.text = parseFloat(newAcc[2], "%.2f")

            gaugeAcceleration!!.updatePoint(newAcc[0], newAcc[1])
        }
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

    override fun onOrientationChanged(yaw: Float, pitch: Float, roll: Float, yawRad: Float, pitchRad: Float, rollRad: Float) {
        yawView.text = parseFloat(yaw, "%.0f")
        pitchView.text = parseFloat(pitch, "%.0f")
        rollView.text = parseFloat(roll, "%.0f")

        rotationDegrees = floatArrayOf(yaw, pitch, roll)
        rotationRadians = floatArrayOf(yawRad, pitchRad, rollRad)
    }

    private fun storeRotationValues() {
        calibrationDegrees = rotationDegrees!!.clone()
        calibrationRadians = rotationRadians!!.clone()

        storedYawView.text = parseFloat(calibrationDegrees!![0], "%.0f")
        storedPitchView.text = parseFloat(calibrationDegrees!![1], "%.0f")
        storedRollView.text = parseFloat(calibrationDegrees!![2], "%.0f")
    }

    private fun resetRotationValues() {
        calibrationDegrees = null
        calibrationRadians = null

        storedYawView.text = "-"
        storedPitchView.text = "-"
        storedRollView.text = "-"

        newXAccelerationTV.text = "-"
        newYAccelerationTV.text = "-"
        newZAccelerationTV.text = "-"
    }

}