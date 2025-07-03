package com.jcorp.jaygauge

import android.content.Context

object ResourceProvider {
    private var colorProducer: ColorProducer?=null
    private var fontProducer: FontProducer?=null

    fun init(context: Context) {
        colorProducer = ColorProducer(context)
        fontProducer = FontProducer(context)
    }
    fun getColors(): ColorProducer {
        if (colorProducer == null) {
            throw UninitializedPropertyAccessException("ColorProducer has not been initialized. Call init() first.")
        }
        return colorProducer!!
    }
    fun getFonts(): FontProducer{
        if (fontProducer == null) {
            throw UninitializedPropertyAccessException("FontProducer has not been initialized. Call init() first.")
        }
        return fontProducer!!
    }
}