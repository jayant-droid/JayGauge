package com.jcorp.jaygauge

import android.content.Context
import androidx.core.content.res.ResourcesCompat

class FontProducer(context: Context) {
    val uniformCondensed by lazy { ResourcesCompat.getFont(context, R.font.uniform_cond) }
    val uniformCondensedMedium by lazy {
        ResourcesCompat.getFont(
            context,
            R.font.uniform_cond_medium
        )
    }
    val uniformCondensedLight by lazy {
        ResourcesCompat.getFont(
            context,
            R.font.uniform_cond_light
        )
    }
    val uniformExtraCondensed by lazy {
        ResourcesCompat.getFont(
            context,
            R.font.uniform_extra_cond
        )
    }
    val uniformExtraCondensedMedium by lazy {
        ResourcesCompat.getFont(
            context,
            R.font.uniform_extra_cond_medium
        )
    }
    val uniformExtraCondensedLight by lazy {
        ResourcesCompat.getFont(
            context,
            R.font.uniform_extra_cond_light
        )
    }
    val calculatorRegular by lazy {
        ResourcesCompat.getFont(context, R.font.calculator_regular)
    }
}