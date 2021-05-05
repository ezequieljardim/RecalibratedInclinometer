package com.ezequieljardim.recalibratedinclinometer.livedata.acceleration

import android.content.Context
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import com.kircherelectronics.fsensor.filter.averaging.AveragingFilter
import com.kircherelectronics.fsensor.filter.averaging.MeanFilter
import com.kircherelectronics.fsensor.sensor.acceleration.AccelerationSensor
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class AccelerationSensorLiveData(context: Context) : LiveData<FloatArray?>() {
    private val sensor: AccelerationSensor = AccelerationSensor(context)
    private var compositeDisposable: CompositeDisposable? = null
    private var averagingFilter: AveragingFilter? = null

    override fun onActive() {
        sensor.setSensorFrequency(SensorManager.SENSOR_DELAY_FASTEST)
        averagingFilter = MeanFilter()
        (averagingFilter as MeanFilter).setTimeConstant(0.5f)
        compositeDisposable = CompositeDisposable()
        sensor.publishSubject.subscribe(object : Observer<FloatArray?> {
            override fun onSubscribe(d: Disposable) {
                compositeDisposable!!.add(d)
            }

            override fun onNext(values: FloatArray) {
                value = if (averagingFilter != null) {
                    averagingFilter?.filter(values)
                } else {
                    values
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
        sensor.onStart()
    }

    override fun onInactive() {
        compositeDisposable!!.dispose()
        sensor.onStop()
    }

}