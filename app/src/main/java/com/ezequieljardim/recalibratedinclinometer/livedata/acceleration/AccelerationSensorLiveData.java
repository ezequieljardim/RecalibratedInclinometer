package com.ezequieljardim.recalibratedinclinometer.livedata.acceleration;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.hardware.SensorManager;

import com.kircherelectronics.fsensor.filter.averaging.AveragingFilter;
import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.sensor.acceleration.AccelerationSensor;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class AccelerationSensorLiveData extends LiveData<float[]> {
    private AccelerationSensor sensor;
    private CompositeDisposable compositeDisposable;
    private Context context;
    private AveragingFilter averagingFilter;

    public AccelerationSensorLiveData(Context context) {
        this.context = context;
        this.sensor = new AccelerationSensor(context);
    }

    @Override
    protected void onActive() {
        this.sensor.setSensorFrequency(SensorManager.SENSOR_DELAY_FASTEST);

        averagingFilter = new MeanFilter();
        ((MeanFilter) averagingFilter).setTimeConstant(0.5f);

        this.compositeDisposable = new CompositeDisposable();
        this.sensor.getPublishSubject().subscribe(new Observer<float[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(float[] values) {
                if (averagingFilter != null) {
                    setValue(averagingFilter.filter(values));
                } else {
                    setValue(values);
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        this.sensor.onStart();
    }

    @Override
    protected void onInactive() {
        this.compositeDisposable.dispose();
        this.sensor.onStop();
    }
}
