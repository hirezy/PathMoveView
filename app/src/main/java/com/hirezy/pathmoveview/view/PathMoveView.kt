package com.hirezy.pathmoveview.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import com.hirezy.pathmoveview.R
import kotlin.math.atan2
import kotlin.math.min

class PathMoveView : View {
    private var mBikeBitmap: Bitmap? = null

    // 圆路径
    private var mPath: Path? = null

    // 路径测量
    private var mPathMeasure: PathMeasure? = null

    // 当前移动值
    private var fraction = 0f
    private var mBitmapMatrix: Matrix? = null
    private var animator: ValueAnimator? = null

    // PathMeasure 测量过程中的坐标
    private val position = FloatArray(2)

    // PathMeasure 测量过程中矢量方向与x轴夹角的的正切值
    private val tan = FloatArray(2)
    private val rectHolder = RectF()
    private var mDrawerPaint: Paint? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    protected fun init(context: Context) {
        // 初始化 画笔 [抗锯齿、不填充、红色、线条2px]
        mDrawerPaint = Paint()
        mDrawerPaint!!.isAntiAlias = true
        mDrawerPaint!!.style = Paint.Style.STROKE
        mDrawerPaint!!.color = Color.BLACK
        mDrawerPaint!!.strokeWidth = 2f

        // 获取图片
        mBikeBitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_bike, null)
        // 初始化矩阵
        mBitmapMatrix = Matrix()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = 0
        height = if (heightMode == MeasureSpec.UNSPECIFIED) {
            dp2px(120f).toInt()
        } else if (heightMode == MeasureSpec.AT_MOST) {
            min(measuredHeight.toDouble(), measuredWidth.toDouble()).toInt()
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }

        setMeasuredDimension(measuredWidth, height)
    }

    override fun onDraw(canvas: Canvas) {
        val width = width
        val height = height
        if (width <= 1 || height <= 1) {
            return
        }

        if (mPath == null) {
            mPath = Path()
        } else {
            mPath!!.reset()
        }
        rectHolder[-100f, -100f, 100f] = 100f

        mPath!!.moveTo(-getWidth() / 2f, 0f)
        mPath!!.lineTo(-(getWidth() / 2f + 200) / 2f, -400f)
        mPath!!.lineTo(-200f, 0f)
        mPath!!.arcTo(rectHolder, 180f, 180f, false)
        mPath!!.quadTo(300f, -200f, 400f, 0f)
        mPath!!.lineTo(500f, 0f)

        if (mPathMeasure == null) {
            mPathMeasure = PathMeasure()
            mPathMeasure!!.setPath(mPath, false)
        }

        val saveCount = canvas.save()
        // 移动坐标矩阵到View中间
        canvas.translate(getWidth() / 2f, getHeight() / 2f)

        // 获取 position(坐标) 和 tan(正切斜率)，注意矢量方向与x轴的夹角
        mPathMeasure!!.getPosTan(mPathMeasure!!.length * fraction, position, tan)

        // 计算角度（斜率），注意矢量方向与x轴的夹角
        val degree = Math.toDegrees(atan2(tan[1].toDouble(), tan[0].toDouble())).toFloat()
        val bmpWidth = mBikeBitmap!!.width
        val bmpHeight = mBikeBitmap!!.height
        // 重置为单位矩阵
        mBitmapMatrix!!.reset()
        // 旋转单位举证，中心点为图片中心
        mBitmapMatrix!!.postRotate(degree, (bmpWidth / 2).toFloat(), (bmpHeight / 2).toFloat())
        // 将图片中心和移动位置对齐
        mBitmapMatrix!!.postTranslate(
            position[0] - bmpWidth / 2,
            position[1] - bmpHeight / 2
        )


        // 画圆路径
        canvas.drawPath(mPath!!, mDrawerPaint!!)
        // 画自行车，使用矩阵旋转方向
        canvas.drawBitmap(mBikeBitmap!!, mBitmapMatrix!!, mDrawerPaint)
        canvas.restoreToCount(saveCount)
    }

    fun start() {
        if (animator != null) {
            animator!!.cancel()
        }
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.setDuration(6000)
        // 匀速增长
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.addUpdateListener { animation -> // 第一种做法：通过自己控制，是箭头在原来的位置继续运行
            fraction = animation.animatedValue as Float
            postInvalidate()
        }
        valueAnimator.start()
        this.animator = valueAnimator
    }

    fun stop() {
        if (animator == null) return
        animator!!.cancel()
    }

    fun dp2px(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}
