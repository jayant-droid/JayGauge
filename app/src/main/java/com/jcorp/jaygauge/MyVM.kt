package com.jcorp.jaygauge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MyVM: ViewModel() {
    private val _clockSpeed = MutableLiveData<Float>()
    val clockSpeed: LiveData<Float> = _clockSpeed

    private val _temperature = MutableLiveData<Float>()
    val temperature: LiveData<Float> = _temperature

    fun startTempGauge(minValue:Float,maxValue:Float){
        viewModelScope.launch {
            GaugeDataProducer.startFloatDataGeneration(
                minValue, maxValue,
                2000L
            ) { value ->
                _temperature.postValue(value)
            }
        }
    }
    fun startCpuGauge(minValue:Float,maxValue:Float){
        viewModelScope.launch {
            GaugeDataProducer.startFloatDataGeneration(
                minValue, maxValue,
                2000L
            ) { value ->
                _clockSpeed.postValue(value)
            }
        }
    }
}