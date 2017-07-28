package com.github.infoshare.imageblur.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.github.infoshare.imageblur.model.DrawInfo;
import com.github.infoshare.imageblur.model.DrawMode;

import java.util.ArrayList;
import java.util.List;


public class DrawableView extends ImageView {

    private static final float TOLERANCE = 5;

    private List<DrawInfo> mDraws = new ArrayList<>();
    private float mX, mY;

    private DrawMode drawMode;
    private int currentSize = 10;

    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDrawMode(DrawMode drawMode) {
        this.drawMode = drawMode;
    }

    public void setSize(int size) {
       this.currentSize = size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPaths(canvas);
    }

    private void drawPaths(Canvas canvas) {
        for(DrawInfo info : mDraws) {
            canvas.drawPath(info.getPath(), info.getPaint());
        }
    }

    private void startTouch(float x, float y) {
        DrawInfo info = createNew();
        info.getPath().moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            getPath().quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearImage() {
        for(DrawInfo info : mDraws){
            info.getPath().reset();
        }
        invalidate();
    }

    private void upTouch() {
        getPath().lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }

    private DrawInfo createNew(){
        DrawInfo info = new DrawInfo(drawMode, currentSize);
        mDraws.add(info);
        return info;
    }

    private DrawInfo getCurrentDrawInfo(){
        return mDraws.get(mDraws.size()-1);
    }

    private Path getPath(){
       return getCurrentDrawInfo().getPath();
    }

    public void undoDraw() {
        if (!mDraws.isEmpty()){
            mDraws.remove(getCurrentDrawInfo());
            invalidate();
        }
    }
}