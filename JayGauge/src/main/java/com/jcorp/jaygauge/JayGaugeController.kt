package com.jcorp.jaygauge

interface JayGaugeController {
    fun demoMode(isOn: Boolean)
    fun setTheme(theme: GaugeTheme)
    fun setProgress(progress: Float)
    fun setMinMax(min: Float, max: Float)
    fun setUnit(unit: Units)
    fun setArcTheme(theme: GaugeArcColorTheme)

}