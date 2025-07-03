package com.jcorp.jaygauge

import kotlin.math.abs

object GaugeUtils {
    fun getAnimValue(obj: Any): Float =
        if (obj is Int) {
            obj.toFloat()
        } else obj as? Float ?: obj.toString().toFloat()



    fun computeNeedleJumpDuration(
        currentValue: Float,
        clamped: Float,
        minValue: Float,
        maxValue: Float,
        pollingInterval: Long = 2000L
    ): Long {
        val diff = abs(currentValue - clamped)
        val scaleRange = maxValue - minValue

        val percentChange = (diff / scaleRange) * 100f

        val baseDuration = 800L // Minimum duration for tiny jumps
        val maxDuration = pollingInterval

        // Example: up to 100% jump -> up to maxDuration
        val duration = ((maxDuration * percentChange) / 100f).toLong()
        return duration.coerceIn(baseDuration, maxDuration)
    }

    fun Float.hasDecimal(): Boolean {
        return this % 1 != 0f
    }
}