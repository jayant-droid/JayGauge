package com.jcorp.jaygauge.sample

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jcorp.jaygauge.GaugeArcColorTheme
import com.jcorp.jaygauge.GaugeTheme

import com.jcorp.jaygauge.JayGauge.GaugeListener
import com.jcorp.jaygauge.Units
import com.jcorp.jaygauge.sample.databinding.ActivityMainBinding
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MyVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSpinner()
        setGauges()
        observeLiveData()
    }

    private fun observeLiveData() {
        //gauge one has demo mode on, not data needed

        viewModel.clockSpeed.observe(this@MainActivity){
            binding.gaugeTwo.setProgress(it)
        }
        viewModel.temperature.observe(this@MainActivity){
            binding.gaugeThree.setProgress(it)
        }
    }

    private fun setGauges() {
        //can set from xml and code as well

        binding.gaugeThree.apply {
            setTheme(GaugeTheme.DARK)
            setArcTheme(GaugeArcColorTheme.LavenderMist)
            setMinMax(0f,100f)
            setUnit(Units.TEMPERATURE_C)
            demoMode(true)
        }
        //gauge one has demo mode on
        //will show progress changes automatically

        //set gaugeListener to listen for callbacks
        //wait till gauge has warmed up, then start setting progress
        binding.gaugeTwo.gaugeListener=object : GaugeListener{
            override fun onGaugePreparing() {
                //wait for  the warm up animation
            }

            override fun onGaugePrepared() {
                //warm up animation ends now gauge is ready to use
                //start setting Progress now
                viewModel.startCpuGauge(0f,5.45f)


            }
        }

        binding.gaugeThree.gaugeListener=object : GaugeListener{
            override fun onGaugePreparing() {
                //wait for  the warm up animation
            }

            override fun onGaugePrepared() {
                //warm up animation ends now gauge is ready to use
                //start setting Progress now
                viewModel.startTempGauge(0f,100f)
            }

        }

    }

    private fun setSpinner() {
        val spinner=binding.spinner
        val themes = GaugeArcColorTheme.entries.toTypedArray()

        val adapter = GaugeThemeSpinnerAdapter(this, themes)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTheme = themes[position]
                binding.gaugeOne.setArcTheme(selectedTheme)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

}
