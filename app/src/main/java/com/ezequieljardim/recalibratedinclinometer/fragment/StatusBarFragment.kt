package com.ezequieljardim.recalibratedinclinometer.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ezequieljardim.recalibratedinclinometer.R
import com.ezequieljardim.recalibratedinclinometer.viewmodel.SensorViewModel
import java.util.*

class StatusBarFragment : Fragment() {
    // Text views for real-time output
    private var textViewXAxis: TextView? = null
    private var textViewYAxis: TextView? = null
    private var textViewZAxis: TextView? = null
    private var textViewHzFrequency: TextView? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var acceleration: FloatArray? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                handler!!.postDelayed(this, 20)
                updateAccelerationText()
            }
        }
        acceleration = FloatArray(4)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status_bar, container, false)
        textViewXAxis = view.findViewById(R.id.value_x_axis)
        textViewYAxis = view.findViewById(R.id.value_y_axis)
        textViewZAxis = view.findViewById(R.id.value_z_axis)
        textViewHzFrequency = view.findViewById(R.id.value_hz_frequency)
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

    private fun updateAccelerationText() {
        if (acceleration!!.size == 4) {
            // Update the acceleration data
            textViewXAxis!!.text = String.format(Locale.getDefault(), "%.2f", acceleration!![0])
            textViewYAxis!!.text = String.format(Locale.getDefault(), "%.2f", acceleration!![1])
            textViewZAxis!!.text = String.format(Locale.getDefault(), "%.2f", acceleration!![2])
            textViewHzFrequency!!.text = String.format(Locale.getDefault(), "%.0f", acceleration!![3])
        }
    }

    private fun initViewModel() {
        val model = ViewModelProviders.of(activity!!).get(SensorViewModel::class.java)
        model.accelerationSensorLiveData.removeObservers(this)
        model.accelerationSensorLiveData.observe(this, { floats -> acceleration = floats })
    }

    companion object {
        private val tag = StatusBarFragment::class.java.simpleName
    }
}