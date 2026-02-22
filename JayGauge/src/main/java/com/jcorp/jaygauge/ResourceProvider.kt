package com.jcorp.jaygauge

import android.content.Context

class ResourceProvider(context: Context) {
    val colorProducer: ColorProducer = ColorProducer(context)
    val fontProducer: FontProducer = FontProducer(context)
    val paintProducer: GaugePaintProducer = GaugePaintProducer(colorProducer,fontProducer)
}

