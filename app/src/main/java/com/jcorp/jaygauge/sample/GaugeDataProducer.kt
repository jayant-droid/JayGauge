package com.jcorp.jaygauge.sample


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object GaugeDataProducer {
    val jobs = mutableListOf<Job>()


    fun startFloatDataGeneration(min: Float, max: Float, intervalMs: Long, onEmit:(data:Float)->Unit): Job {
        val floatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val randomValue = AppUtils.getRandomFloat(min, max)
                onEmit(randomValue)
                delay(intervalMs)
            }
        }
        if(!jobs.contains(floatJob))
            jobs.add(floatJob)
        return floatJob
    }
    fun startIntDataGeneration(min: Int, max: Int, intervalMs: Long, onEmit:(data:Int)->Unit): Job {
        val intJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val randomValue = AppUtils.getRandomInt(min, max)
                onEmit(randomValue)
                delay(intervalMs)
            }
        }
        if(!jobs.contains(intJob))
            jobs.add(intJob)
        return intJob
    }
    fun stopDataGeneration(job: Job) {
        job.cancel()
        jobs.remove(job)
    }


}