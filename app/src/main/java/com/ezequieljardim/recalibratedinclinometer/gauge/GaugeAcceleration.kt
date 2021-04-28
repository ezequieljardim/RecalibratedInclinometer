package com.ezequieljardim.recalibratedinclinometer.gauge

import android.content.Context
import android.graphics.*
import android.hardware.SensorManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GaugeAcceleration : View {
    // holds the cached static part
    private var background: Bitmap? = null
    private var backgroundPaint: Paint? = null
    private var pointPaint: Paint? = null
    private var rimPaint: Paint? = null
    private var rimShadowPaint: Paint? = null
    private var faceRect: RectF? = null
    private var rimRect: RectF? = null

    // added by Scott
    private var rimOuterRect: RectF? = null
    private var innerRim: RectF? = null
    private var innerFace: RectF? = null
    private var innerMostDot: RectF? = null

    private var xPos = 0.0f
    private var yPos = 0.0f
    private var distancePx = 0.0f

    private var scaleXPos = 0.0f
    private var scaleYPos = 0.0f
    private val color = Color.parseColor("#2196F3")

    private val pointsList: MutableList<Pair<Float, Float>> = mutableListOf()

    /**
     * Create a new instance.
     *
     * @param context
     */
    constructor(context: Context?) : super(context) {
        init()
    }

    /**
     * Create a new instance.
     *
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * Create a new instance.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private val zoomedGravityEarthBoundary = SensorManager.GRAVITY_EARTH / 8 // Focus in small acceleration changes

    /**
     * Update the measurements for the point.
     *
     * @param x
     * the x-axis
     * @param y
     * the y-axis
     */
    fun updatePoint(x: Float, y: Float) {
        // Enforce a limit of 1g or 9.8 m/s^2

        var xS = x
        var yS = y

        if (xS > zoomedGravityEarthBoundary) {
            xS = zoomedGravityEarthBoundary
        }
        if (xS < -zoomedGravityEarthBoundary) {
            xS = -zoomedGravityEarthBoundary
        }
        if (yS > zoomedGravityEarthBoundary) {
            yS = zoomedGravityEarthBoundary
        }
        if (yS < -zoomedGravityEarthBoundary) {
            yS = -zoomedGravityEarthBoundary
        }
        this.xPos = scaleXPos * -xS + rimRect!!.centerX()
        this.yPos = scaleYPos * yS + rimRect!!.centerY()

        pointsList.add(Pair(this.xPos, this.yPos))

        // Log.d("Eshe", "Point (${this.xPos}, ${this.yPos}")

        this.invalidate()
    }

    /**
     * Initialize the members of the instance.
     */
    private fun init() {
        initDrawingTools()
    }

    /**
     * Initialize the drawing related members of the instance.
     */
    private fun initDrawingTools() {
        rimRect = RectF(0.1f, 0.1f, 0.9f, 0.9f)
        scaleXPos = (rimRect!!.right - rimRect!!.left) / (zoomedGravityEarthBoundary * 2)
        scaleYPos = (rimRect!!.bottom - rimRect!!.top) / (zoomedGravityEarthBoundary * 2)

        // inner rim oval
        innerRim = RectF(0.25f, 0.25f, 0.75f, 0.75f)

        // inner most white dot
        innerMostDot = RectF(0.49f, 0.49f, 0.51f, 0.51f)

        // the linear gradient is a bit skewed for realism
        rimPaint = Paint()
        rimPaint!!.flags = Paint.ANTI_ALIAS_FLAG
        rimPaint!!.color = Color.GRAY
        val rimSize = 0.01f
        faceRect = RectF()
        faceRect!![rimRect!!.left + rimSize, rimRect!!.top + rimSize, rimRect!!.right - rimSize] = rimRect!!.bottom - rimSize

        rimShadowPaint = Paint()
        rimShadowPaint!!.style = Paint.Style.FILL
        rimShadowPaint!!.isAntiAlias = true
        rimShadowPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        // set the size of the outside white with the rectangles.
        // a 'bigger' negative will increase the size.
        val rimOuterSize = -0.04f
        rimOuterRect = RectF()
        rimOuterRect!![rimRect!!.left + rimOuterSize, rimRect!!.top
                + rimOuterSize, rimRect!!.right - rimOuterSize] = (rimRect!!.bottom
                - rimOuterSize)

        // inner rim declarations the black oval/rect
        val rimInnerSize = 0.01f
        innerFace = RectF()
        innerFace!![innerRim!!.left + rimInnerSize, innerRim!!.top + rimInnerSize, innerRim!!.right - rimInnerSize] = innerRim!!.bottom - rimInnerSize
        pointPaint = Paint()
        pointPaint!!.isAntiAlias = true
        pointPaint!!.color = Color.RED //Color.parseColor("#2196F3")
        pointPaint!!.style = Paint.Style.FILL_AND_STROKE

        backgroundPaint = Paint()
        backgroundPaint!!.isFilterBitmap = true
    }

    /**
     * Measure the device screen size to scale the canvas correctly.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)
        val chosenDimension = Math.min(chosenWidth, chosenHeight)
        setMeasuredDimension(chosenDimension, chosenDimension)
    }

    /**
     * Indicate the desired canvas dimension.
     *
     * @param mode
     * @param size
     * @return
     */
    private fun chooseDimension(mode: Int, size: Int): Int {
        return if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            size
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            preferredSize
        }
    }

    /**
     * In case there is no size specified.
     *
     * @return default preferred size.
     */
    private val preferredSize: Int
        private get() = 300

    /**
     * Draw the gauge.
     *
     * @param canvas
     */
    private fun drawGauge(canvas: Canvas) {

        // first, draw the metallic body
        canvas.drawOval(rimRect, rimPaint)

        // draw the rim shadow inside the face
        canvas.drawOval(faceRect, rimShadowPaint)

        // draw the inner white rim circle
        canvas.drawOval(innerRim, rimPaint)

        // draw the inner black oval
        canvas.drawOval(innerFace, rimShadowPaint)

        // draw inner white dot
        canvas.drawOval(innerMostDot, rimPaint)
    }

    /**
     * Draw the measurement point.
     *
     * @param canvas
     */
    private fun drawPoint(canvas: Canvas) {

        var distance = 0.0f
        val dotRadius = 0.004f
//        canvas.drawCircle(xPos, yPos, 0.025f, pointPaint)
        for ((index, p) in pointsList.withIndex()) {
            canvas.save()
            pointPaint!!.color = color
            canvas.drawCircle(p.first, p.second, dotRadius, pointPaint)
            canvas.restore()

            if (index > 0) {
                distance = (distance + abs(sqrt((pointsList[index].first - pointsList[index-1].first).pow(2) + (pointsList[index].second - pointsList[index-1].second).pow(2)))).toFloat()
            }
        }

        val dotPxValue = (background?.width ?: 0) * dotRadius // The square is 1.0f
        distancePx = distance / dotRadius * dotPxValue

        // Log.d("Eshe", "Distance: $distance, dotPxValue: $dotPxValue, distance in px: ${distance / dotRadius * dotPxValue}")

    }

    /**
     * Draw the background of the canvas.
     *
     * @param canvas
     */
    private fun drawBackground(canvas: Canvas) {
        // Use the cached background bitmap.
        if (background == null) {
            Log.w(Companion.tag, "Background not created")
        } else {
            canvas.drawBitmap(background, 0f, 0f, backgroundPaint)
//            Log.d("Eshe", "drawBG: ${background?.width} , ${background?.height}")
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawBackground(canvas)
        val scale = width.toFloat()
        canvas.save()
        canvas.scale(scale, scale)
        drawPoint(canvas)
        canvas.restore()
    }

    /**
     * Indicate the desired size of the canvas has changed.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d(Companion.tag, "Size changed to " + w + "x" + h)
        regenerateBackground()
    }

    /**
     * Regenerate the background image. This should only be called when the size
     * of the screen has changed. The background will be cached and can be
     * reused without needing to redraw it.
     */
    private fun regenerateBackground() {
        // free the old bitmap
        if (background != null) {
            background!!.recycle()
        }
        background = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        val backgroundCanvas = Canvas(background)
        val scale = width.toFloat()
        backgroundCanvas.scale(scale, scale)
        drawGauge(backgroundCanvas)
    }

    fun resetPointsList() {
        pointsList.clear()
    }

    fun getDistance(): Float {
        return distancePx
    }

    companion object {
        private val tag = GaugeAcceleration::class.java.simpleName
    }
}