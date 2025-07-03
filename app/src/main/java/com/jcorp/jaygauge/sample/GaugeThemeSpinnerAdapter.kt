package com.jcorp.jaygauge.sample
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jcorp.jaygauge.GaugeArcColorTheme


class GaugeThemeSpinnerAdapter(
    context: Context,
    private val themes: Array<GaugeArcColorTheme>
) : ArrayAdapter<GaugeArcColorTheme>(context, 0, themes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createThemeView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createThemeView(position, convertView, parent)
    }

    private fun createThemeView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_gauge_theme_spinner,
            parent,
            false
        )

        val theme = themes[position]

        val gradientView = view.findViewById<ImageView>(R.id.gradientView)
        val nameView = view.findViewById<TextView>(R.id.nameView)

        // Create a horizontal gradient drawable using the theme colors
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            theme.colors
        ).apply {
            cornerRadius = 16f
        }

        gradientView.setImageDrawable(gradientDrawable)
        nameView.text = theme.themeName

        return view
    }
}
