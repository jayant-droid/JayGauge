package com.jcorp.jaygauge

interface JayGaugeController {
    fun isDemoMode(isOn: Boolean)
    fun setTheme(theme: GaugeTheme)
    fun setProgress(progress: Float)
    fun setMinValue(min: Float)
    fun setMaxValue(max: Float)
    fun setUnit(unit: Units)
    fun setCustomUnit(unit: String)
    fun setArcTheme(theme: GaugeArcColorTheme)
    fun setGaugeListener(gaugeListener: JayGauge.GaugeListener)
    fun setNumOfTicks(numOfTicks: Int)
    fun getMin():Float
    fun getMax():Float

}