package com.jcorp.jaygauge

import android.content.Context

object ResourceProvider {
    lateinit var colorProducer: ColorProducer
    lateinit var fontProducer: FontProducer

    lateinit var paintProducer: GaugePaintProducer


    fun init(context: Context) {
        colorProducer = ColorProducer(context)
        fontProducer = FontProducer(context)
        paintProducer = GaugePaintProducer(colorProducer,fontProducer)
    }
}

