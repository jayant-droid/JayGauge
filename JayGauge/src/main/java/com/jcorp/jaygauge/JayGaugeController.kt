package com.jcorp.jaygauge

interface JayGaugeController {
    fun isDemoMode(isOn: Boolean)
    fun setTheme(theme: GaugeTheme)
    fun setProgress(progress: Float)
    fun setMinMax(min: Float, max: Float)
    fun setUnit(unit: Units)
    fun setCustomUnit(unit: String)
    fun setArcTheme(theme: GaugeArcColorTheme)
    fun setGaugeListener(gaugeListener: JayGauge.GaugeListener)

}