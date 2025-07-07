package com.jcorp.jaygauge


import android.content.Context
import androidx.core.content.ContextCompat

class ColorProducer(context: Context) {
    val grayTextColor by lazy { ContextCompat.getColor(context, R.color.gray_text) }
    val blackDefaultTextColor by lazy {
        ContextCompat.getColor(
            context,
            R.color.black_default_text
        )
    }
    val lightBlackTextColor by lazy { ContextCompat.getColor(context, R.color.light_black_text) }
    val semi_white by lazy { ContextCompat.getColor(context, R.color.semi_white) }
    val more_opaque_white by lazy { ContextCompat.getColor(context, R.color.more_opaque_white) }
}