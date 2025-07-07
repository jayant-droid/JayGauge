package com.jcorp.jaygauge

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
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
import androidx.core.graphics.toColorInt
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
    private val blackDefaultTextColor: Int by lazy { ResourceProvider.getColors().blackDefaultTextColor }
    private val grayTextColor by lazy { ResourceProvider.getColors().grayTextColor }
    private val lightBlackTextColor by lazy { ResourceProvider.getColors().lightBlackTextColor }
    private val moreOpaqueWhite by lazy { ResourceProvider.getColors().more_opaque_white }

    //fonts
    private val uniformCondensed: Typeface? by lazy { ResourceProvider.getFonts().uniformCondensed }
    private val uniformExtraCondensedMedium: Typeface? by lazy { ResourceProvider.getFonts().uniformExtraCondensedMedium }
    private val uniformExtraCondensed: Typeface? by lazy { ResourceProvider.getFonts().uniformExtraCondensed }
    private val uniformCondensedMedium: Typeface? by lazy { ResourceProvider.getFonts().uniformCondensedMedium }

    //needle bitmap
    private var needleBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_needle)
    private var scaledNeedleBitmap: Bitmap? = null
    private var lastRadiusForNeedle = -1f

    //class level private variables
    private var gaugeTheme: GaugeTheme = GaugeTheme.LIGHT
    private var minProgress = 0f
    private var maxProgress = 100f
    private var currentValue: Float = minProgress
    private var floatValueAnimator: ValueAnimator? = null
    private var intValueAnimator: ValueAnimator? = null

    private var sweepAnimator: ValueAnimator? = null
    private var isSweeping = false

    private var numOfLabels = 9


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
    private var arcColors: IntArray = GaugeArcColorTheme.Default.colors

    //for listening imp callbacks
    private var gaugeListener: GaugeListener? = null
    private var prepareAnimator: ValueAnimator? = null
    private var isPreparing = false
    private var isPrepared = false


    //init block
    init {
        ResourceProvider.init(context)
        setupAttributes()
    }

    //xml attributes
    private fun setupAttributes() {
        //gauge Theme
        setUpTheme()
        setUpNumOfTicks()
        setUpUnit()
        setUpMinMax()
        setUpProgress()
        setUpArcTheme()
        setUpDemoMode()
    }

    private fun setUpProgress() {
        val value = typedArray.getFloat(R.styleable.JayGauge_progress, minProgress)
        currentValue= value.coerceIn(minProgress,maxProgress)
        invalidate()
    }

    private fun setUpNumOfTicks() {
        numOfLabels =
            typedArray.getInt(R.styleable.JayGauge_numOfTicks, numOfLabels)
        isTickLabelPrepared=false
        invalidate()
    }

    private fun setUpDemoMode() {
        val isOn = typedArray.getBoolean(R.styleable.JayGauge_demoMode, false)
        isDemoMode(isOn)
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
        this.unit = Units.entries.firstOrNull { it.id == gaugeUnitId } ?: Units.NONE
        invalidate()
    }

    private fun setUpMinMax() {
        minProgress = typedArray.getFloat(R.styleable.JayGauge_minProgress, getDefaultMin())
        maxProgress = typedArray.getFloat(R.styleable.JayGauge_maxProgress, getDefaultMax())
        invalidate()
    }
    private fun getDefaultMin():Float{
        return 0f
    }
    private fun getDefaultMax():Float{
        when(unit){
            Units.TEMPERATURE_C ->{
                return 100f
            }
            Units.TEMPERATURE_F ->{
                return 212f
            }
            Units.GHZ -> {
                return 5.4f
            }
            Units.MHZ -> {
                return 2000f
            }
            Units.KPH -> {
                return 200f
            }
            Units.KPH_KM_H -> {
                return 200f
            }
            Units.MPH_MI_H -> {
                return 200f
            }
            Units.MPH -> {
                return 200f
            }
            Units.PERCENTAGE -> {
                return 100f
            }
            Units.GB -> {
                return 100f
            }
            Units.MB -> {
                return 100f
            }
            Units.NONE -> {
                return 100f
            }

            else ->{
                return 100f
            }
        }
    }

    private fun setUpArcTheme() {
        val themeId =
            typedArray.getInt(R.styleable.JayGauge_arcColorTheme, GaugeArcColorTheme.Default.id)
        val arcTheme: GaugeArcColorTheme =
            GaugeArcColorTheme.entries.firstOrNull { it.id == themeId }
                ?: GaugeArcColorTheme.Default
        arcColors = arcTheme.colors
        invalidate()
    }


    //internal functions
    private fun applyTheme() {
        when (gaugeTheme) {
            GaugeTheme.LIGHT -> {
                // Light background
                setBackgroundColor(Color.TRANSPARENT)
                // Text
                valueTextPaint.color = blackDefaultTextColor
                unitTextPaint.color = blackDefaultTextColor
                tickTextPaint.color = lightBlackTextColor

                // Arc
                bgArcPaint.color = Color.LTGRAY
            }

            GaugeTheme.DARK -> {
                setBackgroundColor(Color.TRANSPARENT)
                valueTextPaint.color = Color.WHITE
                unitTextPaint.color = Color.WHITE
                tickTextPaint.color = Color.LTGRAY
                bgArcPaint.color = Color.DKGRAY
            }
        }

        invalidate() // Force redraw
    }

    private val valueInterpolator by lazy {
        AccelerateDecelerateInterpolator()
    }


    private fun setValue(targetValue: Float) {
        var isInit=false
        var progress=targetValue
        if(targetValue < minProgress){
            progress=minProgress
        }
        val clamped = progress.coerceIn(minProgress, maxProgress)
        // Cancel previous animator if running
        floatValueAnimator?.cancel()

        if(floatValueAnimator == null){
            isInit=true
            floatValueAnimator = ValueAnimator.ofFloat(currentValue, clamped)
        }else{
            floatValueAnimator?.setFloatValues(currentValue,clamped)
        }
        floatValueAnimator?.duration = computeNeedleJumpDuration(
            currentValue = currentValue,
            clamped = clamped.toFloat(),
            minValue = minProgress,
            maxValue = maxProgress,

            pollingInterval = pollInterval
        )
        if(isInit || floatValueAnimator?.interpolator ==null) {
            floatValueAnimator?.interpolator = valueInterpolator
        }
        if(isInit) {
            floatValueAnimator?.addUpdateListener {
                currentValue = getAnimValue(it.animatedValue)
                invalidate()
            }
        }
        floatValueAnimator?.start()
    }

    private fun setValue(targetValue: Int) {
        var isInit=false
        var progress=targetValue
        if(targetValue < minProgress){
            progress=minProgress.toInt()
        }
        val clamped = progress.coerceIn(minProgress.toInt(), maxProgress.toInt())

        // Cancel previous animator if running
        intValueAnimator?.cancel()

        if(intValueAnimator == null){
            isInit=true
            intValueAnimator = ValueAnimator.ofInt(currentValue.toInt(), clamped)
        }else{
            intValueAnimator?.setIntValues(currentValue.toInt(),clamped)
        }
        intValueAnimator?.duration = computeNeedleJumpDuration(
            currentValue = currentValue,
            clamped = clamped.toFloat(),
            minValue = minProgress,
            maxValue = maxProgress,
            pollingInterval = pollInterval
        )
        if(isInit || intValueAnimator?.interpolator ==null) {
            intValueAnimator?.interpolator = valueInterpolator
        }
        if(isInit) {
            intValueAnimator?.addUpdateListener {
                currentValue = getAnimValue(it.animatedValue)
                invalidate()
            }
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
    private var centerX = 0f
    private var centerY = 0f


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //need to call these once only
        if (arcRadius == 0f) {
            setMainArcRadius()
            setLayerType(LAYER_TYPE_SOFTWARE, null)
            // ✅ Dynamically update stroke widths!
            updateDynamicStrokeWidths(arcRadius)
        }

        //draw Gauge Name, Value  and Unit
        drawGaugeUnitAndValue(canvas, centerX, centerY, arcRadius)

        //draw tick Text Labels
        drawTickLabels(canvas, centerX, centerY, arcRadius)

        //draw arc
        drawArc(canvas, centerX, centerY, arcRadius)

        //draw Needle
        drawNeedle(canvas, centerX, centerY, arcRadius)

        //center circle
        drawNeedleAnchor(canvas, centerX, centerY, arcRadius)
    }

    private fun setMainArcRadius() {
        centerX = width / 2f
        centerY = height / 2f
        arcRadius = min(centerX, centerY) * 0.88f
    }

    // needle anchor
    private data class NeedleAnchor(
        val outerRadius: Float,
        val innerRadius: Float, val outX: Float, val outY: Float,
        val inX: Float, val inY: Float
    )

    private var needleAnchor: NeedleAnchor? = null

    // Example: use same color as your needle but lighter shades
    private val anchorOuterColor by lazy { "#CCCCCC".toColorInt() }// lighter ring
    private val anchorInnerColor by lazy { "#EEEEEE".toColorInt() }// lighter hub

    // Outer ring
    private val ringPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = anchorOuterColor
        }
    }


    // Center hub
    private val hubPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = anchorInnerColor
        }
    }

    private fun drawNeedleAnchor(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Float
    ) {
        if (needleAnchor != null) {
            needleAnchor?.let {
                // 1️⃣ Outer ring
                canvas.drawCircle(it.outX, it.outY, it.outerRadius, ringPaint)

                // 2️⃣ Inner hub
                canvas.drawCircle(it.inX, it.inY, it.innerRadius, hubPaint)
            }
            return
        }
        // Size relative to gauge size
        val outerRadius = radius * 0.117f
        val innerRadius = outerRadius * 0.7f

        // 1️⃣ Outer ring
        canvas.drawCircle(centerX, centerY, outerRadius, ringPaint)

        // 2️⃣ Inner hub
        canvas.drawCircle(centerX, centerY, innerRadius, hubPaint)
        needleAnchor = NeedleAnchor(outerRadius, innerRadius, centerX, centerY, centerX, centerY)
    }

    //tick labels
    private data class TickLabel(
        val labelX: Float, val labelY: Float, val tickValueText: String,
        val labelValue: Float
    )

    private var isTickLabelPrepared = false
    private val tickLabels = mutableListOf<TickLabel>()

    //private val labelHighlightOffset = (maxValue - minValue) * 0.02f
    //this makes highlight to appear before arc reaches it.
    // for error correction, not needed with curr logic
    private val labelHighlightOffset = 0f
        val defaultNumOfLabels=9
    private fun drawTickLabels(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Float
    ) {
        if (isTickLabelPrepared) {
            tickLabels.forEach {
                //only calculate if needle reaches the label
                tickTextPaint.color = if (currentValue >= it.labelValue - labelHighlightOffset) {
                    getTextColor()
                } else {
                    getTickDisabledColor()
                }
                canvas.drawText(
                    it.tickValueText, it.labelX, it.labelY, tickTextPaint
                )
            }
            return
        }
        tickLabels.clear()
        val gaugePaddingFraction = 0.05f // tweak if you want margin at edges
        val availableRadius = radius * (1f - gaugePaddingFraction)
        var percentageDelta=0f
        var inCreaseTextSize=false
        if(defaultNumOfLabels != numOfLabels){
            val labelSizeDiff=if(numOfLabels > defaultNumOfLabels){
                inCreaseTextSize = false
                numOfLabels-defaultNumOfLabels
            }else {
                inCreaseTextSize = true
                defaultNumOfLabels-numOfLabels
            }
            percentageDelta=(labelSizeDiff.toFloat()/defaultNumOfLabels)*100
            percentageDelta=percentageDelta

        }

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
        if(percentageDelta > 0f) {
            val changeInTextSize=(((tickTextSizeFraction * (percentageDelta/1.8f)) / 100))
            tickTextSizeFraction = if(inCreaseTextSize) {
                tickTextSizeFraction + changeInTextSize
            }else{
                tickTextSizeFraction - changeInTextSize
            }
        }
        var labelRadiusFraction = when (unit) {
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
        if(percentageDelta > 0f) {
            val changeInLabelRadius=(((labelRadiusFraction * (percentageDelta/10)) / 100))
            labelRadiusFraction = if(inCreaseTextSize) {
                labelRadiusFraction - changeInLabelRadius
            }else{
                labelRadiusFraction + changeInLabelRadius
            }
        }
        tickTextPaint.textSize = (availableRadius * tickTextSizeFraction).coerceIn(42f,100f)

        val interval = (maxProgress - minProgress) / (numOfLabels - 1)

        // ✅ Dynamic label radius based on gauge radius:
        val labelRadius =
            (radius * labelRadiusFraction).coerceAtMost(radius-(progressArcPaint.strokeWidth * 0.90f)) // tweak 0.8 ~ 0.85 for style this affects spacing between
        // gauge arc and text labels

        for (i in 0 until numOfLabels) {
            val angleDeg = startAngle + i * (sweepAngle / (numOfLabels - 1))
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val labelValue = minProgress + i * interval

            val labelX = centerX + labelRadius * cos(angleRad).toFloat()
            val labelY =
                centerY + labelRadius * sin(angleRad).toFloat() + tickTextPaint.textSize / 3

            tickTextPaint.color = if (currentValue >= labelValue - labelHighlightOffset) {
                getTextColor()
            } else {
                getTickDisabledColor()
            }

            val tickValueText = when (unit) {
                Units.GHZ -> String.format(Locale.US, "%.1f", labelValue)
                else -> labelValue.toInt().toString()
            }
            val tickLabel = TickLabel(labelX, labelY, tickValueText, labelValue)
            tickLabels.add(tickLabel)

            canvas.drawText(
                tickValueText, labelX, labelY, tickTextPaint
            )
        }
        isTickLabelPrepared = true
    }


    private fun getTickDisabledColor(): Int = if (gaugeTheme == GaugeTheme.LIGHT) {
        lightBlackTextColor
    } else {
        moreOpaqueWhite
    }


    private fun getTextColor(): Int = when (gaugeTheme) {
        GaugeTheme.LIGHT -> Color.BLACK
        GaugeTheme.DARK  -> Color.WHITE
        // Add a default case or handle other themes if necessary
        // For now, defaulting to black for unhandled themes
    }


    private val sweepAnimatorUpdateListener: ValueAnimator.AnimatorUpdateListener by lazy {
        ValueAnimator.AnimatorUpdateListener { animation ->
            currentValue = getAnimValue(animation.animatedValue)
            invalidate()
        }
    }
    private val sweepInterpolator by lazy{ AccelerateDecelerateInterpolator() }
    private val sweepRunnable: Runnable by lazy {
        Runnable {
            animateNextSweep()
        }
    }

    private val onEnd: (Animator) -> Unit by lazy {
        {
            postDelayed(sweepRunnable, pollInterval) // delay before next sweep
        }
    }
    private fun animateNextSweep() {
        if (!isSweeping) return

        val nextValue = minProgress + Random.nextFloat() * (maxProgress - minProgress)
        val nextDuration = (500..1200).random().toLong()
        if (sweepAnimator != null) {
            sweepAnimator?.cancel()
            sweepAnimator?.apply {
                setFloatValues(currentValue, nextValue)
                duration = nextDuration
                start()
            }
        } else {
            sweepAnimator = ValueAnimator.ofFloat(currentValue, nextValue).apply {
                duration = nextDuration
                interpolator = sweepInterpolator
                addUpdateListener(sweepAnimatorUpdateListener)
                doOnEnd(onEnd)
                start()
            }
        }
    }


    private data class Arc(val rect: RectF)
    private data class MainArc(val bgArc: Arc, val glowArc: Arc, val progArc: Arc)

    private var mainArc: MainArc? = null
    private fun drawArc(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Float
    ) {
        if (mainArc != null) {
            mainArc?.bgArc?.let { arc ->
                // 1️⃣ Draw background arc (full sweep)
                canvas.drawArc(arc.rect, startAngle, sweepAngle, false, bgArcPaint)
            }
            // 2️⃣ Calculate current sweep angle for progress
            val ratio = (currentValue - minProgress) / (maxProgress - minProgress)
            val progressSweep = (sweepAngle * ratio)
            mainArc?.glowArc?.let { arc ->
                // First: wider, softer glow arc
                canvas.drawArc(arc.rect, startAngle, progressSweep, false, glowArcPaint)
            }
            mainArc?.progArc?.let { arc ->
                // 4️⃣ Draw progress arc
                canvas.drawArc(arc.rect, startAngle, progressSweep, false, progressArcPaint)
            }
            return
        }
        val rect = RectF(
            centerX - radius, centerY - radius, centerX + radius, centerY + radius
        )

        // 1️⃣ Draw background arc (full sweep)
        canvas.drawArc(rect, startAngle, sweepAngle, false, bgArcPaint)

        // 2️⃣ Calculate current sweep angle for progress
        val safeCurrent = currentValue.coerceIn(minProgress, maxProgress)
        val ratio = (safeCurrent - minProgress) / (maxProgress - minProgress)
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
        mainArc = MainArc(bgArc = Arc(rect), glowArc = Arc(glowRect), progArc = Arc(rect))
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
        val ratio = (currentValue - minProgress) / (maxProgress - minProgress)
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

    private data class GaugeReading(
        val unitText: String,
        val unitX: Float,
        val unitY: Float,
        val valueX: Float,
        val valueY: Float
    )

    private var gaugeUnitAndValue: GaugeReading? = null
    private var onPrepareGaugeValueColorSet: Boolean = false

    private fun drawGaugeUnitAndValue(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Float
    ) {
        if (gaugeUnitAndValue != null) {
            gaugeUnitAndValue?.let {
                if (isPrepared && !onPrepareGaugeValueColorSet) {
                    unitTextPaint.color = if (isPrepared) getTextColor() else grayTextColor
                    valueTextPaint.color = if (isPrepared) getTextColor() else grayTextColor
                    onPrepareGaugeValueColorSet = true
                }
                val valueText = getFormattedValueText()
                canvas.drawText(valueText, it.valueX, it.valueY, valueTextPaint)
                canvas.drawText(it.unitText, it.unitX, it.unitY, unitTextPaint)
            }
            return
        }
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

        gaugeUnitAndValue = GaugeReading(
            unitText = unitText,
            unitX = unitX,
            unitY = baseY,
            valueX = actualValueX,
            valueY = baseY
        )
        canvas.drawText(valueText, actualValueX, baseY, valueTextPaint)
        canvas.drawText(unitText, unitX, baseY, unitTextPaint)
    }

    private fun padLeftToLength(value: String, totalLength: Int): String {
        return value.padStart(totalLength, '0')
    }

    private fun getFormattedValueText(): String {
        val isCustomUnit = customUnit != null

        val pattern = if (isCustomUnit) {
            if(minProgress !=0f){minProgress.toString()}else{"000"}
        } else {
            if(minProgress !=0f){minProgress.toString()}else{unit.valuePattern}
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
    private val sweepDuration: Long = 1000L
    private val pauseAfterSweep: Long = 1000L
    private val startDelay: Long = 1000L
    private val reverseSweepListener by lazy {
        object : AnimatorListenerAdapter() {
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
        }
    }
    private val fwdSweepListener by lazy{
        object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                if (!isPreparing) {
                    isPreparing = true
                    gaugeListener?.onGaugePreparing()
                }
            }
        }
    }
    private val fwdReverseAnimUpdateListener by lazy {
        object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                currentValue = getAnimValue(animation.animatedValue)
                invalidate()
            }
        }
    }
    private val fwdReverseInterpolator by lazy{AccelerateDecelerateInterpolator()}
    var fwdSweepAnimator: ValueAnimator? = null
    var reverseSweepAnimator: ValueAnimator?=null
    private val reverseRunnable:Runnable by lazy {
        Runnable{
            prepareAnimator = fwdSweepAnimator
            fwdSweepAnimator?.start()
        }
    }
    private fun prepareGaugeSweep(
    ) {
        isPrepared = false
        prepareAnimator?.cancel()

        // Forward sweep: 0 -> max
        if(fwdSweepAnimator == null) {
            fwdSweepAnimator = ValueAnimator.ofFloat(minProgress, maxProgress).apply {
                duration = sweepDuration
                interpolator = fwdReverseInterpolator
                addUpdateListener(fwdReverseAnimUpdateListener)
                addListener(fwdSweepListener)
            }
        }

        // Reverse sweep: max -> 0
        if(reverseSweepAnimator==null) {
            reverseSweepAnimator = ValueAnimator.ofFloat(maxProgress, minProgress).apply {
                duration = sweepDuration
                interpolator = AccelerateDecelerateInterpolator()

                addUpdateListener(fwdReverseAnimUpdateListener)

                addListener(reverseSweepListener)
            }

            fwdSweepAnimator?.doOnEnd {
                postDelayed({
                    reverseSweepAnimator?.start()
                }, 500)
            }
        }
        prepareAnimator = fwdSweepAnimator

        postDelayed(reverseRunnable, startDelay)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isPreparing) {
            prepareGaugeSweep()
        }
    }

    //exposed functions to control JayGauge

    override fun isDemoMode(isOn: Boolean) {
        if (isOn) {
            if (isSweeping) return // already running
            isSweeping = true
            if (!isPreparing && isPrepared) {
                animateNextSweep()
            }
        } else {
            if(isSweeping) {
                isSweeping = false
                sweepAnimator?.cancel()
                removeCallbacks(sweepRunnable)
                setProgress(minProgress)
                invalidate()
            }
        }
    }

    override fun setTheme(theme: GaugeTheme) {
        this.gaugeTheme = theme
        applyTheme()
    }

    override fun setProgress(progress: Float) {
        if (isSweeping) return
        if (progress.hasDecimal()) {
            setValue(progress)
        } else {
            setValue(progress.toInt())
        }
    }

    override fun setMinProgress(min: Float) {
        this.minProgress = min
        currentValue=min
        isTickLabelPrepared=false
        invalidate()
    }

    override fun setMaxProgress(max: Float) {
        this.maxProgress = max
        isTickLabelPrepared=false
        invalidate()
    }



    override fun setUnit(unit: Units) {
        this.gaugeUnitAndValue = null
        this.unit = unit
        invalidate()
    }

    override fun setArcTheme(theme: GaugeArcColorTheme) {
        mainArc = null //re-draw main arc
        arcColors = theme.colors
        invalidate()
    }

    override fun setGaugeListener(gaugeListener: GaugeListener) {
        this.gaugeListener = gaugeListener
    }

    override fun setNumOfTicks(numOfTicks: Int) {
        numOfLabels=numOfTicks
        isTickLabelPrepared=false
        invalidate()
    }

    override fun setCustomUnit(cUnit: String) {
        customUnit = cUnit
        invalidate()
    }
    override fun getMinProgress():Float=minProgress
    override fun getMaxProgress():Float=maxProgress

}