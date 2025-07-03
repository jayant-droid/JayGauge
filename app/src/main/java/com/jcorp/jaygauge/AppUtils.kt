package com.jcorp.jaygauge

import kotlin.random.Random

object AppUtils {
     fun getRandomFloat(min: Float, max: Float): Float {
        return Random.nextDouble(min.toDouble(), max.toDouble()).toFloat()
    }
     fun getRandomInt(min: Int, max: Int): Int {
        return Random.nextInt(min, max)
    }

}