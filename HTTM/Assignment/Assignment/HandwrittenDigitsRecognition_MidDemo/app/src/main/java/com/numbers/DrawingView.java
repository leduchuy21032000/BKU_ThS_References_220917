package com.numbers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class DrawingView extends ImageView {
    private Paint paint = new Paint();
    private Path path = new Path();

    public DrawingView(Context context, AttributeSet attrs) {

        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(70f);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(DrawingView.class.getName(), "onSizeChanged: Creating imageBitmap of w and h = " + w + ", " + h);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(DrawingView.class.getName(), "on Touch Down: Creating imageBitmap of w and h = "
                        + getWidth() + ", " + getHeight());

                path.reset();
                path.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(touchX, touchY);
                path.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void clear() {
        path.reset();
        invalidate();
    }
}
