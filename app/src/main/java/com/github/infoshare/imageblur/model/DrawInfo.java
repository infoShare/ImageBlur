package com.github.infoshare.imageblur.model;

import android.graphics.Paint;
import android.graphics.Path;

public class DrawInfo {

    private Path mPath = new Path();
    private Paint mPaint;

    public DrawInfo(DrawMode drawMode, int size) {
        initPaint(drawMode, size);
    }

    private void initPaint(DrawMode drawMode, int size) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(size);
        mPaint.setColor(drawMode.getColor());
        if (drawMode == DrawMode.Shade) {
            mPaint.setAlpha(95);
        }
    }

    public Paint getPaint() {
        return mPaint;
    }

    public Path getPath() {
        return mPath;
    }
}
