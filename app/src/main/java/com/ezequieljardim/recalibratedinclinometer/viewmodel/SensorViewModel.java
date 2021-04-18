package com.ezequieljardim.recalibratedinclinometer.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import com.ezequieljardim.recalibratedinclinometer.livedata.acceleration.AccelerationSensorLiveData;

public class SensorViewModel extends AndroidViewModel {
    private AccelerationSensorLiveData accelerationSensorLiveData;

    public SensorViewModel(Application application) {
        super(application);

        this.accelerationSensorLiveData = new AccelerationSensorLiveData(application);
    }

    public AccelerationSensorLiveData getAccelerationSensorLiveData() {
        return accelerationSensorLiveData;
    }
}
