package com.ezequieljardim.recalibratedinclinometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ezequieljardim.recalibratedinclinometer.livedata.acceleration.AccelerationSensorLiveData

class SensorViewModel(application: Application?) : AndroidViewModel(application!!) {
    val accelerationSensorLiveData: AccelerationSensorLiveData = AccelerationSensorLiveData(application!!)

}