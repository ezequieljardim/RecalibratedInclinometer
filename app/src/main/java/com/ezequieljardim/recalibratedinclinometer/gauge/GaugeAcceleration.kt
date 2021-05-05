package com.ezequieljardim.recalibratedinclinometer.gauge

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.hardware.SensorManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GaugeAcceleration : View {
    // holds the cached static part
    private var background: Bitmap? = null
    private var backgroundPaint: Paint? = null
    private var pointPaint: Paint? = null
    private var rimPaint: Paint? = null
    private var textPaint: TextPaint? = null
    private var rimShadowPaint: Paint? = null
    private var faceRect: RectF? = null
    private var rimRect: RectF? = null

    // added by Scott
    private var rimOuterRect: RectF? = null
    private var innerRim: RectF? = null
    private var innerFace: RectF? = null
    private var innerMostDot: RectF? = null
    private var verticalLine: RectF? = null
    private var horizontalLine: RectF? = null

    private var xPos = 0.0f
    private var yPos = 0.0f
    private var distancePx = 0.0f

    private var scaleXPos = 0.0f
    private var scaleYPos = 0.0f
    private val color = Color.parseColor("#2196F3")

    private lateinit var currentBitmap: Bitmap


    private val separator = "/"
    private val imageSavedText = "Photo saved to gallery!"
    private val defaultFolder = "Pictures"

    var isZoomedIn = true

    private val originalPointsList: MutableList<Pair<Float, Float>> = mutableListOf()
    private val scaledPointsList: MutableList<Pair<Float, Float>> = mutableListOf()

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
    private val gravityEarthBoundary = SensorManager.GRAVITY_EARTH

    private fun scalePoint(x: Float, y: Float, scale: Float): Pair<Float, Float> {
        // Enforce a limit of 1g or 9.8 m/s^2 if zoomed out, or g/8 if zoomed in
        var xS = x
        var yS = y

        if (xS > scale) {
            xS = scale
        }
        if (xS < -scale) {
            xS = -scale
        }
        if (yS > scale) {
            yS = scale
        }
        if (yS < -scale) {
            yS = -scale
        }
        this.xPos = scaleXPos * -xS + rimRect!!.centerX()
        this.yPos = scaleYPos * yS + rimRect!!.centerY()

        return Pair(this.xPos, this.yPos)
    }

    /**
     * Update the measurements for the point.
     *
     * @param x
     * the x-axis
     * @param y
     * the y-axis
     */
    fun updatePoint(x: Float, y: Float) {

        originalPointsList.add(Pair(x, y))
        val bound = if (isZoomedIn) zoomedGravityEarthBoundary else gravityEarthBoundary
        val scaled = scalePoint(x, y, bound)
        scaledPointsList.add(scaled)

//        Log.d("Eshe", "Point (${this.xPos}, ${this.yPos}")

        this.invalidate()
    }

    /**
     * Initialize the members of the instance.
     */
    private fun init() {
        var metrics = Resources.getSystem().displayMetrics
        currentBitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.widthPixels, Bitmap.Config.ARGB_8888)
        initDrawingTools()
    }

    /**
     * Initialize the drawing related members of the instance.
     */
    private fun initDrawingTools() {
        rimRect = RectF(0.1f, 0.1f, 0.9f, 0.9f)
        val bound = if (isZoomedIn) zoomedGravityEarthBoundary else gravityEarthBoundary
        scaleXPos = (rimRect!!.right - rimRect!!.left) / (bound * 2)
        scaleYPos = (rimRect!!.bottom - rimRect!!.top) / (bound * 2)

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

        textPaint = TextPaint()
        textPaint!!.color = Color.GRAY
        textPaint!!.textSize = 0.03f
        textPaint!!.textScaleX = 1f
        textPaint!!.isLinearText = true
        textPaint!!.isSubpixelText = true

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

        horizontalLine = RectF(0.1f, 0.498f, 0.9f, 0.502f)

        verticalLine = RectF(0.498f, 0.1f, 0.502f, 0.9f)
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
        canvas.drawOval(rimRect!!, rimPaint!!)

        // draw the rim shadow inside the face
        canvas.drawOval(faceRect!!, rimShadowPaint!!)

        // draw the inner white rim circle
        canvas.drawOval(innerRim!!, rimPaint!!)

        // draw the inner black oval
        canvas.drawOval(innerFace!!, rimShadowPaint!!)

        // draw inner white dot
        canvas.drawOval(innerMostDot!!, rimPaint!!)

        // draw cross
        canvas.drawRect(horizontalLine!!, rimPaint!!)

        canvas.drawRect(verticalLine!!, rimPaint!!)

        val text = if (isZoomedIn) "G/8" else "G"
        canvas.drawText(text, 0.04f, 0.51f, textPaint!!)
        canvas.drawText(text, 0.91f, 0.51f, textPaint!!)

    }

    /**
     * Draw the measurement point.
     *
     * @param canvas
     */
    private fun drawPoint(canvas: Canvas) {
        val bound = if (isZoomedIn) zoomedGravityEarthBoundary else gravityEarthBoundary

        var distance = 0.0f
        val dotRadius = 0.004f

        for ((index, p) in originalPointsList.withIndex()) {
            canvas.save()
            pointPaint!!.color = color

            val scaled = scalePoint(p.first, p.second, bound)
            canvas.drawCircle(scaled.first, scaled.second, dotRadius, pointPaint!!)
            canvas.restore()

            if (index > 0) {
                val prevPoint = scalePoint(originalPointsList[index - 1].first, originalPointsList[index - 1].second, bound)
                distance += abs(sqrt((scaled.first - prevPoint.first).pow(2) + (scaled.second - prevPoint.second).pow(2)))
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
            canvas.drawBitmap(background!!, 0f, 0f, backgroundPaint)
//            Log.d("Eshe", "drawBG: ${background?.width} , ${background?.height}")
        }
    }

    override fun onDraw(canvas: Canvas) {
        val newCanvas = Canvas(currentBitmap)
        newCanvas.drawColor(0, PorterDuff.Mode.CLEAR)

        drawBackground(newCanvas)
        val scale = width.toFloat()
        newCanvas.save()
        newCanvas.scale(scale, scale)
        drawPoint(newCanvas)
        newCanvas.restore()

        canvas.drawBitmap(currentBitmap, Matrix(), null)
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
        val backgroundCanvas = Canvas(background!!)
        val scale = width.toFloat()
        backgroundCanvas.scale(scale, scale)
        drawGauge(backgroundCanvas)
    }

    fun resetPointsList() {
        originalPointsList.clear()
        scaledPointsList.clear()
    }

    fun getDistance(): Float {
        return distancePx
    }

    fun getQ1(): Float {
        val filtered = scaledPointsList.filter { it.first < 0.5f && it.second < 0.5f }
        return (filtered.size / scaledPointsList.size.toFloat()) * 100f
    }

    fun getQ2(): Float {
        val filtered = scaledPointsList.filter { it.first >= 0.5f && it.second < 0.5f }
        return (filtered.size / scaledPointsList.size.toFloat()) * 100f
    }

    fun getQ3(): Float {
        val filtered = scaledPointsList.filter { it.first < 0.5f && it.second >= 0.5f }
        return (filtered.size / scaledPointsList.size.toFloat()) * 100f
    }

    fun getQ4(): Float {
        val filtered = scaledPointsList.filter { it.first >= 0.5f && it.second >= 0.5f }
        return (filtered.size / scaledPointsList.size.toFloat()) * 100f
    }

    fun zoomOut() {
        isZoomedIn = false
        initDrawingTools()
        regenerateBackground()
    }

    fun zoomIn() {
        isZoomedIn = true
        initDrawingTools()
        regenerateBackground()
    }

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        return values
    }

    fun saveImage() {
        val bitmapCopy = Bitmap.createBitmap(currentBitmap)
        val folderName = "Pictures"

        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                val values = contentValues()
                values.put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        defaultFolder + separator + folderName
                )

                values.put(MediaStore.Images.Media.IS_PENDING, true)
                // RELATIVE_PATH and IS_PENDING are introduced in API 29.

                val uri: Uri? =
                        context.contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                        )
                if (uri != null) {
                    saveImageToStream(bitmapCopy, context.contentResolver.openOutputStream(uri))
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val directory = File(Environment.getExternalStorageDirectory(), folderName)
                // getExternalStorageDirectory is deprecated in API 29
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val fileName = System.currentTimeMillis().toString() + ".png"

                Log.d("Eshe", "filename $fileName")
                val file = File(directory, fileName)
                file.createNewFile()

                saveImageToStream(bitmapCopy, FileOutputStream(file))
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        }
    }

    companion object {
        private val tag = GaugeAcceleration::class.java.simpleName

    }
}