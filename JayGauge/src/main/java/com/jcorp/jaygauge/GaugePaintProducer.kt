package com.jcorp.jaygauge

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Paint

class GaugePaintProducer(colorProducer: ColorProducer, fontProducer: FontProducer)  {

    //paint objects
     val bgArcPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 35f
            color = Color.LTGRAY // or any light color you want
        }
    }

     val progressArcPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 41f
        }
    }

    // 1️⃣ Add a new Paint for the glow
     val glowArcPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
            strokeWidth = progressArcPaint.strokeWidth * 1.06f  // or tweak 2.0~3.0
            color = Color.TRANSPARENT  // or your base progress color
            alpha = 35// 0-255, 80~120 works well for soft glow
        }
    }

    val needlePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true // smooth when scaling or rotating
            isDither = true       // optional: better color blending
        }
    }
    val valueTextPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 64f
            textAlign = Paint.Align.CENTER
            typeface = fontProducer.uniformCondensed
        }
    }
    val unitTextPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 64f
            textAlign = Paint.Align.CENTER
            typeface = fontProducer.uniformExtraCondensedMedium
        }
    }

    val tickTextPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorProducer.lightBlackTextColor
            textSize = 64f
            typeface = fontProducer.uniformCondensedMedium
            textAlign = Paint.Align.CENTER

        }
    }
    val tickMultiplierTextPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorProducer.lightBlackTextColor
            textSize = 32f
            typeface = fontProducer.uniformCondensedMedium
            textAlign = Paint.Align.CENTER

        }
    }

}