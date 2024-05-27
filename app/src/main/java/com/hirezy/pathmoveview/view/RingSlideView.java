package com.hirezy.pathmoveview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class RingSlideView extends View {

    private final DisplayMetrics mDM;
    private TextPaint mArcPaint;
    private TextPaint mDrawerPaint;
    private int maxRadius;
    private double rotateDegree = 90;
    private float offsetDegree = 3;

    public RingSlideView(Context context) {
        this(context, null);
    }

    public RingSlideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingSlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDM = getResources().getDisplayMetrics();
        initPaint();
    }
    private void initPaint() {
        // 实例化画笔并打开抗锯齿
        mArcPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mDrawerPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mDrawerPaint.setAntiAlias(true);
        mDrawerPaint.setStyle(Paint.Style.FILL);
        mDrawerPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawerPaint.setStrokeWidth(5);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = mDM.widthPixels / 2;
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = widthSize / 2;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    private Path path = new Path();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0 ) {
            return;
        }
        maxRadius = (int) (Math.min(width / 2, height / 2) - dipsToPixels(10) - mDrawerPaint.getStrokeWidth());
        int saveCount = canvas.save();
        canvas.translate(width*1F/2,height*1F/2); //平移坐标轴到view中心点
        canvas.drawCircle(0,0,maxRadius,mArcPaint);
        int length = dipsToPixels(10);

        path.reset();

        for (int i=0;i<3;i++) {
            double radians = Math.toRadians(rotateDegree - offsetDegree + i*offsetDegree );
            if(i==0) {
                float sx = (float) ((maxRadius - length) * Math.cos(radians));
                float sy = (float) ((maxRadius - length) * Math.sin(radians));
                float tx = (float) ((maxRadius + length) * Math.cos(radians));
                float ty = (float) ((maxRadius + length) * Math.sin(radians));
                path.moveTo(sx,sy);
                path.lineTo(tx,ty);
            }else if(i==1){
                float x = (float) (maxRadius * Math.cos(radians));
                float y = (float) (maxRadius * Math.sin(radians));
                canvas.drawLine(0, 0, x, y, mDrawerPaint);
            }else if(i==2){
                float x = (float) (maxRadius * Math.cos(radians));
                float y = (float) (maxRadius * Math.sin(radians));
                path.lineTo(x,y);
                path.close();
            }
        }

        canvas.drawPath(path,mDrawerPaint);
        canvas.restoreToCount(saveCount);
    }
    final int dipsToPixels(int dips) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dips * scale + 0.5f);
    }
}
