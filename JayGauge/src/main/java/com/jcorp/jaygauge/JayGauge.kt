package com.jcorp.jaygauge

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.graphics.scale
import com.jcorp.jaygauge.GaugeUtils.computeNeedleJumpDuration
import com.jcorp.jaygauge.GaugeUtils.getAnimValue
import com.jcorp.jaygauge.GaugeUtils.hasDecimal
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class JayGauge @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), JayGaugeController {

    //Colors
    val blackDefaultTextColor: Int by lazy { ResourceProvider.getColors().blackDefaultTextColor }
    val grayTextColor by lazy { ResourceProvider.getColors().grayTextColor }
    val blueThemeColor by lazy { ResourceProvider.getColors().blueThemeColor }
    val lightBlackTextColor by lazy { ResourceProvider.getColors().lightBlackTextColor }
    val moreOpaqueWhite by lazy { ResourceProvider.getColors().more_opaque_white }

    //fonts
    val uniformCondensed: Typeface? by lazy { ResourceProvider.getFonts().uniformCondensed }
    val uniformExtraCondensedMedium: Typeface? by lazy { ResourceProvider.getFonts().uniformExtraCondensedMedium }
    val uniformExtraCondensed: Typeface? by lazy { ResourceProvider.getFonts().uniformExtraCondensed }
    val uniformCondensedMedium: Typeface? by lazy { ResourceProvider.getFonts().uniformCondensedMedium }

    //needle bitmap
    var needleBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_needle)
    private var scaledNeedleBitmap: Bitmap? = null
    private var lastRadiusForNeedle = -1f

    //class level private variables
    private var gaugeTheme: GaugeTheme = GaugeTheme.LIGHT
    private var currentValue: Float = 0f
    private var floatValueAnimator: ValueAnimator? = null
    private var intValueAnimator: ValueAnimator? = null

    private var sweepAnimator: ValueAnimator? = null
    private var isSweeping = false

    //paint objects
    private val bgArcPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 35f
            color = Color.LTGRAY // or any light color you want
        }
    }

    private val progressArcPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 41f
        }
    }

    // 1️⃣ Add a new Paint for the glow
    private val glowArcPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
            strokeWidth = progressArcPaint.strokeWidth * 1.06f  // or tweak 2.0~3.0
            color = Color.TRANSPARENT  // or your base progress color
            alpha = 35// 0-255, 80~120 works well for soft glow
        }
    }

    private val pollInterval: Long = 2000L
    private val needlePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true // smooth when scaling or rotating
            isDither = true       // optional: better color blending
        }
    }
    private val valueTextPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 64f
            textAlign = Paint.Align.CENTER
            typeface = uniformCondensed
        }
    }
    private val unitTextPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 64f
            textAlign = Paint.Align.CENTER
            typeface = uniformExtraCondensedMedium
        }
    }

    private val tickTextPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightBlackTextColor
            textSize = 64f
            typeface = uniformCondensedMedium
            textAlign = Paint.Align.CENTER

        }
    }

    private var minValue = 0f
    private var maxValue = 100f

    // You can adjust these:
    private val startAngle by lazy { 140f }
    private val sweepAngle by lazy { 260f }

    //attributes
    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.JayGauge, 0, 0
        )
    }

    //Gauge Unit
    private var unit: Units = Units.NONE

    //custom unit
    private var customUnit: String? = null

    //arc
    //private val arcColorPositions by lazy { floatArrayOf(0f, 0.5f, 1f)}
    private val arcColorPositions by lazy { floatArrayOf(0f, 0.15f, 0.25f, 0.5f, 0.65f, 0.75f, 1f) }
    var arcColors: IntArray = GaugeArcColorTheme.Default.colors

    //for listening imp callbacks
    var gaugeListener: GaugeListener? = null
    private var prepareAnimator: ValueAnimator? = null
    private var isPreparing = false
    private var isPrepared = false

    //init block
    init {
        ResourceProvider.init(context)
        setupAttributes(typedArray)
    }

    //xml attributes
    private fun setupAttributes(typedArray: TypedArray) {
        //gauge Theme
        setUpTheme()
        setUpUnit()
        setUpMinMax()
        setUpArcTheme()
        setUpDemoMode()

    }

    private fun setUpDemoMode() {
        val isOn = typedArray.getBoolean(R.styleable.JayGauge_demoMode, false)
        demoMode(isOn)
    }

    private fun setUpTheme() {
        val gaugeThemeValue =
            typedArray.getInt(R.styleable.JayGauge_gaugeTheme, GaugeTheme.LIGHT.value)
        gaugeTheme =
            GaugeTheme.entries.firstOrNull { it.value == gaugeThemeValue } ?: GaugeTheme.LIGHT
        setTheme(gaugeTheme)
    }

    private fun setUpUnit() {
        val customUnit = typedArray.getString(R.styleable.JayGauge_customUnit) ?: customUnit
        if (customUnit != null) {
            this.customUnit = customUnit
            invalidate()
            return
        }
        val gaugeUnitId = typedArray.getInt(R.styleable.JayGauge_gaugeUnit, Units.NONE.id)
        unit = Units.entries.firstOrNull { it.id == gaugeUnitId } ?: Units.NONE
        setUnit(unit)
    }

    private fun setUpMinMax() {
        minValue = typedArray.getFloat(R.styleable.JayGauge_minValue, 0f)
        maxValue = typedArray.getFloat(R.styleable.JayGauge_maxValue, 100f)
        setMinMax(minValue, maxValue)
    }

    private fun setUpArcTheme() {
        val themeId =
            typedArray.getInt(R.styleable.JayGauge_arcColorTheme, GaugeArcColorTheme.Default.id)
        val arcTheme: GaugeArcColorTheme =
            GaugeArcColorTheme.entries.firstOrNull { it.id == themeId }
                ?: GaugeArcColorTheme.Default
        setArcTheme(arcTheme)
    }


    //internal functions
    private fun applyTheme() {
        when (gaugeTheme) {
            GaugeTheme.LIGHT -> {
                // Light background
                setBackgroundColor(Color.WHITE)
                // Text
                valueTextPaint.color = blackDefaultTextColor
                unitTextPaint.color = blackDefaultTextColor
                tickTextPaint.color = lightBlackTextColor

                // Arc
                bgArcPaint.color = Color.LTGRAY
            }

            GaugeTheme.DARK -> {
                setBackgroundColor(Color.BLACK)
                valueTextPaint.color = Color.WHITE
                unitTextPaint.color = Color.WHITE
                tickTextPaint.color = Color.LTGRAY

                bgArcPaint.color = Color.DKGRAY
            }

            GaugeTheme.BLUE -> {
                setBackgroundColor(blueThemeColor)
                valueTextPaint.color = Color.WHITE
                unitTextPaint.color = Color.WHITE
                tickTextPaint.color = Color.LTGRAY
                bgArcPaint.color = Color.DKGRAY
            }
        }

        invalidate() // Force redraw
    }

    val valueInterpolator by lazy {
        AccelerateDecelerateInterpolator()
    }

    private fun setValue(targetValue: Float) {
        val clamped = targetValue.coerceIn(minValue, maxValue)
        // Cancel previous animator if running
        floatValueAnimator?.cancel()

        floatValueAnimator = ValueAnimator.ofFloat(currentValue, clamped)
        floatValueAnimator?.duration = computeNeedleJumpDuration(
            currentValue = currentValue,
            clamped = clamped.toFloat(),
            minValue = minValue,
            maxValue = maxValue,

            pollingInterval = pollInterval
        )
        floatValueAnimator?.interpolator = valueInterpolator
        floatValueAnimator?.addUpdateListener {
            currentValue = getAnimValue(it.animatedValue)
            invalidate()
        }
        floatValueAnimator?.start()
    }

    private fun setValue(targetValue: Int) {
        val clamped = targetValue.coerceIn(minValue.toInt(), maxValue.toInt())

        // Cancel previous animator if running
        intValueAnimator?.cancel()

        intValueAnimator = ValueAnimator.ofInt(currentValue.toInt(), clamped)
        intValueAnimator?.duration = computeNeedleJumpDuration(
            currentValue = currentValue,
            clamped = clamped.toFloat(),
            minValue = minValue,
            maxValue = maxValue,
            pollingInterval = pollInterval
        )
        intValueAnimator?.interpolator = valueInterpolator
        intValueAnimator?.addUpdateListener {
            currentValue = getAnimValue(it.animatedValue)
            invalidate()
        }
        intValueAnimator?.start()
    }


    private fun updateDynamicStrokeWidths(radius: Float) {
        // You can tweak these factors to taste
        val baseStrokeWidth = (radius * 0.22f).coerceIn(8f, 100f)  // For progress arc
        val bgStrokeWidth = baseStrokeWidth * 0.83f  // For bg arc
        val glowStrokeWidth = baseStrokeWidth  // For glow

        bgArcPaint.strokeWidth = bgStrokeWidth
        progressArcPaint.strokeWidth = baseStrokeWidth

        glowArcPaint.strokeWidth = glowStrokeWidth

        // Also scale glow blur if needed:
        glowArcPaint.maskFilter =
            BlurMaskFilter(baseStrokeWidth.coerceAtLeast(8f), BlurMaskFilter.Blur.NORMAL)

    }

    private var arcRadius = 0f


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.88f
        arcRadius = radius

        setLayerType(LAYER_TYPE_SOFTWARE, null)

        // ✅ Dynamically update stroke widths!
        updateDynamicStrokeWidths(radius)

        //draw arc
        drawArc(canvas, centerX, centerY, radius)

        //draw tick Text Labels
        drawTickLabels(canvas, centerX, centerY, radius)

        //draw Needle
        drawNeedle(canvas, centerX, centerY, radius)

        //draw Gauge Name, Value  and Unit
        drawGaugeNameUnitAndText(canvas, centerX, centerY, radius)

        drawNeedleAnchor(canvas, centerX, centerY, radius)


    }

    private fun drawNeedleAnchor(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Float
    ) {
        // Size relative to gauge size
        val outerRadius = radius * 0.117f
        val innerRadius = outerRadius * 0.7f

        // Example: use same color as your needle but lighter shades
        val anchorOuterColor = Color.parseColor("#CCCCCC") // lighter ring
        val anchorInnerColor = Color.parseColor("#EEEEEE") // lighter hub

        // Outer ring
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = anchorOuterColor
        }


        // Center hub
        val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = anchorInnerColor
        }

        // 1️⃣ Outer ring
        canvas.drawCircle(centerX, centerY, outerRadius, ringPaint)

        // 2️⃣ Inner hub
        canvas.drawCircle(centerX, centerY, innerRadius, hubPaint)
    }

    private fun drawTickLabels(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Float
    ) {
        val gaugePaddingFraction = 0.05f // tweak if you want margin at edges
        val availableRadius = radius * (1f - gaugePaddingFraction)

        var tickTextSizeFraction = when (unit) {
            Units.TEMPERATURE_C, Units.TEMPERATURE_F -> {
                0.25f
            }

            Units.GHZ -> {
                0.22f
            }

            Units.MHZ -> {
                0.18f
            }

            else -> {
                0.22f
            }
        }
        val labelRadiusFraction = when (unit) {
            Units.TEMPERATURE_C, Units.TEMPERATURE_F -> {
                0.70f
            }

            Units.GHZ -> {
                0.73f
            }

            else -> {
                0.73f
            }
        }
        tickTextPaint.textSize = availableRadius * tickTextSizeFraction

        val numLabels = 9
        val interval = (maxValue - minValue) / (numLabels - 1)

        // ✅ Dynamic label radius based on gauge radius:
        val labelRadius =
            radius * labelRadiusFraction // tweak 0.8 ~ 0.85 for style this affects spacing between
        // gauge arc and text labels

        for (i in 0 until numLabels) {
            val angleDeg = startAngle + i * (sweepAngle / (numLabels - 1))
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val labelValue = minValue + i * interval

            val labelX = centerX + labelRadius * cos(angleRad).toFloat()
            val labelY =
                centerY + labelRadius * sin(angleRad).toFloat() + tickTextPaint.textSize / 3

            val margin = (maxValue - minValue) * 0.02f
            tickTextPaint.color = if (currentValue >= labelValue - margin) {
                getTextColor()
            } else {
                getTickDisabledColor()
            }

            val tickValueText = when (unit) {
                Units.GHZ -> String.format(Locale.US, "%.1f", labelValue)
                else -> labelValue.toInt().toString()
            }

            canvas.drawText(
                tickValueText, labelX, labelY, tickTextPaint
            )
        }
    }


    private fun getTickDisabledColor(): Int = if (gaugeTheme == GaugeTheme.LIGHT) {
        lightBlackTextColor
    } else {
        moreOpaqueWhite
    }


    private fun getTextColor(): Int = when (gaugeTheme) {
        GaugeTheme.LIGHT -> Color.BLACK
        GaugeTheme.DARK, GaugeTheme.BLUE -> Color.WHITE
        // Add a default case or handle other themes if necessary
        // For now, defaulting to black for unhandled themes
    }


    private fun animateNextSweep() {
        if (!isSweeping) return

        val nextValue = minValue + Random.nextFloat() * (maxValue - minValue)
        val nextDuration = (500..1200).random().toLong()

        sweepAnimator?.cancel()
        sweepAnimator = ValueAnimator.ofFloat(currentValue, nextValue).apply {
            duration = nextDuration
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                currentValue = getAnimValue(animation.animatedValue)
                invalidate()
            }

            doOnEnd {
                postDelayed({
                    animateNextSweep() // chain next sweep
                },pollInterval) // delay before next sweep)

            }

            start()
        }
    }


    private fun drawArc(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Float
    ) {
        val rect = RectF(
            centerX - radius, centerY - radius, centerX + radius, centerY + radius
        )

        // 1️⃣ Draw background arc (full sweep)
        canvas.drawArc(rect, startAngle, sweepAngle, false, bgArcPaint)

        // 2️⃣ Calculate current sweep angle for progress
        val ratio = (currentValue - minValue) / (maxValue - minValue)
        val progressSweep = sweepAngle * ratio

        // 3️⃣ Create sweep gradient for progress arc
        val arcGradient = SweepGradient(
            centerX, centerY, arcColors, arcColorPositions
        )

        // Rotate gradient so it aligns with startAngle
        val matrix = Matrix()
        matrix.postRotate(startAngle - 70, centerX, centerY)
        arcGradient.setLocalMatrix(matrix)

        // Rotate matrix as you do for progressArcPaint
        glowArcPaint.shader = arcGradient

        //arc position

        // 1️⃣ Glow arc radius pulled slightly inside
        val glowOffset = glowArcPaint.strokeWidth   // adjust to taste
        val glowRadius = radius - glowOffset

        val glowRect = RectF(
            centerX - glowRadius, centerY - glowRadius, centerX + glowRadius, centerY + glowRadius
        )
        // First: wider, softer glow arc
        canvas.drawArc(glowRect, startAngle, progressSweep, false, glowArcPaint)

        progressArcPaint.shader = arcGradient
        // 4️⃣ Draw progress arc
        canvas.drawArc(rect, startAngle, progressSweep, false, progressArcPaint)
    }

    private fun getScaledNeedleBitmap(radius: Float): Bitmap {
        // Fraction: shaftLengthFraction = (visibleShaftHeight / bitmapHeight)
        val shaftFraction = 0.45f  // if 45% of the bitmap is the visible shaft
        val desiredShaftLength = radius
        val totalBitmapHeight = desiredShaftLength / shaftFraction

        val aspectRatio = needleBitmap.width.toFloat() / needleBitmap.height.toFloat()
        val scaledWidth = totalBitmapHeight * aspectRatio
        val scaledHeight = totalBitmapHeight

        return needleBitmap.scale(scaledWidth.toInt(), scaledHeight.toInt())
    }

    private fun getOrCreateScaledNeedleBitmap(radius: Float): Bitmap {
        if (scaledNeedleBitmap == null || lastRadiusForNeedle != radius) {
            scaledNeedleBitmap = getScaledNeedleBitmap(radius)
            lastRadiusForNeedle = radius
        }
        return scaledNeedleBitmap!!
    }

    private fun drawNeedle(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        //Scale needle size as per arc size
        val scaledNeedleBitmap = getOrCreateScaledNeedleBitmap(radius)

        // Calculate the needle angle
        val ratio = (currentValue - minValue) / (maxValue - minValue)
        val needleAngle = startAngle + ratio * sweepAngle - 140

        // Draw rotated needle image with center pivot
        val matrix = Matrix()

        // 1. First, translate the bitmap so its center aligns with the gauge center
        matrix.postTranslate(
            centerX - scaledNeedleBitmap.width / 2f, centerY - scaledNeedleBitmap.height / 2f
        )

        // 2. Then rotate around the gauge center
        matrix.postRotate(
            needleAngle, centerX, centerY
        )

        // 3. Draw the bitmap
        canvas.drawBitmap(scaledNeedleBitmap, matrix, needlePaint)

    }

    private fun drawGaugeNameUnitAndText(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Float
    ) {
        val isCustomUnit = customUnit != null
        val unitText = if (isCustomUnit) {
            customUnit ?: ""
        } else {
            unit.value
        }
        val maxValuePattern = if (isCustomUnit) {
            "000"
        } else {
            unit.valuePattern
        }
        val valueText = getFormattedValueText()

        val valueTextFraction = if (isCustomUnit) {
            0.38f
        } else {
            when (unit) {
                Units.TEMPERATURE_C, Units.TEMPERATURE_F -> {
                    0.4f
                }

                Units.GHZ -> {
                    0.4f
                }

                Units.MHZ -> {
                    0.4f
                }

                else -> {
                    0.38f
                }
            }
        }
        val valueTextSize = radius * valueTextFraction
        valueTextPaint.textSize = valueTextSize
        valueTextPaint.color = if (isPrepared) getTextColor() else grayTextColor


        val maxValueBounds = Rect()
        valueTextPaint.getTextBounds(maxValuePattern, 0, maxValuePattern.length, maxValueBounds)

        val actualValueBounds = Rect()
        valueTextPaint.getTextBounds(valueText, 0, valueText.length, actualValueBounds)

        // Unit
        val unitTextSizeFraction = if (isCustomUnit) {
            0.45f
        } else {
            when (unit) {
                Units.TEMPERATURE_C, Units.TEMPERATURE_F -> {
                    0.60f
                }

                Units.GHZ -> {
                    0.55f
                }

                Units.MHZ -> {
                    0.45f
                }

                else -> {
                    0.45f
                }
            }
        }
        val unitTextSize = valueTextSize * unitTextSizeFraction
        unitTextPaint.textSize = unitTextSize
        unitTextPaint.color = if (isPrepared) getTextColor() else grayTextColor

        val unitBounds = Rect()
        unitTextPaint.getTextBounds(unitText, 0, unitText.length, unitBounds)

        val unitSpacing = unitTextSize * 0.2f

        val totalWidth = maxValueBounds.width() + unitSpacing + unitBounds.width()

        val opticalShift = unitBounds.width() * 0.25f  // tweak this factor (0.2 ~ 0.3 works well)

        val baseY = centerY + radius * 0.95f

        // Center total block with optical shift
        val blockCenterX = centerX + opticalShift

        val valueX = blockCenterX - totalWidth / 2f + maxValueBounds.width() / 2f
        val unitPadding = 1f
        val unitX =
            unitPadding + valueX + maxValueBounds.width() / 2f + unitSpacing + unitBounds.width() / 2f

        // Draw actual value inside fixed box
        val offset = (maxValueBounds.width() - actualValueBounds.width()) / 2f
        val actualValueX =
            valueX - maxValueBounds.width() / 2f + offset + actualValueBounds.width() / 2f

        canvas.drawText(valueText, actualValueX, baseY, valueTextPaint)
        canvas.drawText(unitText, unitX, baseY, unitTextPaint)
    }

    private fun padLeftToLength(value: String, totalLength: Int): String {
        return value.padStart(totalLength, '0')
    }

    private fun getFormattedValueText(): String {
        val isCustomUnit = customUnit != null

        val pattern = if (isCustomUnit) {
            "000"
        } else {
            unit.valuePattern
        }

        return when {
            pattern.contains(".") -> {
                // Decimal pattern, e.g., "00.0" or "0.00"
                val decimals = pattern.substringAfter(".").length
                val intPart = pattern.substringBefore(".").length

                if (currentValue > 0) {
                    val formatted = String.format(Locale.US, "%.${decimals}f", currentValue)
                    padLeftToLength(formatted, intPart + decimals + 1) // +1 for the dot
                } else {
                    pattern
                }
            }

            pattern.isNotEmpty() -> {
                // Integer pattern, e.g., "0000"
                if (currentValue > 0) {
                    val valueInt = currentValue.toInt()
                    valueInt.toString().padStart(pattern.length, '0')
                } else {
                    pattern
                }
            }

            else -> {
                // Fallback for Units.NONE
                if (currentValue > 0) {
                    currentValue.toInt().toString()
                } else {
                    ""
                }
            }
        }
    }


    interface GaugeListener {
        fun onGaugePreparing()
        fun onGaugePrepared()
    }

    private fun prepareGaugeSweep(
        sweepDuration: Long = 1000L, pauseAfterSweep: Long = 1000L, startDelay: Long = 1000L
    ) {
        isPrepared = false
        prepareAnimator?.cancel()

        // Forward sweep: 0 -> max
        val forwardSweep = ValueAnimator.ofFloat(minValue, maxValue).apply {
            duration = sweepDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                currentValue = getAnimValue(animation.animatedValue)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    if (!isPreparing) {
                        isPreparing = true
                        gaugeListener?.onGaugePreparing()
                    }
                }
            })
        }

        // Reverse sweep: max -> 0
        val reverseSweep = ValueAnimator.ofFloat(maxValue, minValue).apply {
            duration = sweepDuration
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                currentValue = getAnimValue(animation.animatedValue)
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isPreparing = false
                    isPrepared = true

                    // ✅ Add a short pause before signaling "ready"
                    postDelayed({
                        invalidate()
                        gaugeListener?.onGaugePrepared()
                        if (isSweeping) {
                            animateNextSweep() //start demo Mode
                        }
                    }, pauseAfterSweep)
                }
            })
        }

        forwardSweep.doOnEnd {
            postDelayed({
                reverseSweep.start()
            }, 300)
        }

        prepareAnimator = forwardSweep

        postDelayed({
            prepareAnimator = forwardSweep
            forwardSweep.start()
        }, startDelay)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isPreparing) {
            prepareGaugeSweep()
        }
    }

    //exposed functions to control JayGauge

    override fun demoMode(isOn: Boolean) {
        if(isOn) {
            if (isSweeping) return // already running
            isSweeping = true
            if (!isPreparing && isPrepared) {
                animateNextSweep()
            }
        }else{
            isSweeping = false
            if (sweepAnimator?.isStarted == true) sweepAnimator?.cancel()
        }
    }

    override fun setTheme(theme: GaugeTheme) {
        this.gaugeTheme = theme
        applyTheme()
    }

    override fun setProgress(progress: Float) {
        if(isSweeping) return
        if (progress.hasDecimal()) {
            setValue(progress)
        } else {
            setValue(progress.toInt())
        }
    }

    override fun setMinMax(min: Float, max: Float) {
        this.minValue = min
        this.maxValue = max
        invalidate()
    }


    override fun setUnit(unit: Units) {
        this.unit = unit
        invalidate()
    }

    override fun setArcTheme(theme: GaugeArcColorTheme) {
        arcColors = theme.colors
        invalidate()
    }

    fun setCustomUnit(cUnit: String) {
        customUnit = cUnit
        invalidate()
    }

}