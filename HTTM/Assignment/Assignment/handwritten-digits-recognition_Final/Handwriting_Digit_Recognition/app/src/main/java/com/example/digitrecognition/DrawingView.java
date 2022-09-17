package com.example.digitrecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class DrawingView extends View{

    public static int BRUSH_SIZE = 50;
    public static final int DEFAULT_COLOR = Color.WHITE;
    public static final int DEFAULT_BG_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int currentColor;
    private int strokeWidth;
    private float X, Y;
    private Path PATH;
    private Paint PAINT;
    private Bitmap BITMAP;
    private Canvas CANVAS;
    private Paint BitmapPaint = new Paint(Paint.DITHER_FLAG);

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PAINT = new Paint();
        PAINT.setAntiAlias(true);
        PAINT.setDither(true);
        PAINT.setColor(DEFAULT_COLOR);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeJoin(Paint.Join.ROUND);
        PAINT.setStrokeCap(Paint.Cap.ROUND);
        PAINT.setXfermode(null);
        PAINT.setAlpha(0xff);
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BITMAP = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        CANVAS = new Canvas(BITMAP);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        CANVAS.drawColor(backgroundColor);

        for (FingerPath fp : paths) {
            PAINT.setColor(fp.color);
            PAINT.setStrokeWidth(fp.strokeWidth);
            PAINT.setMaskFilter(null);
            CANVAS.drawPath(fp.path, PAINT);
        }

        canvas.drawBitmap(BITMAP, 0, 0, BitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        PATH = new Path();
        FingerPath fp = new FingerPath(currentColor, strokeWidth, PATH);
        paths.add(fp);

        PATH.reset();
        PATH.moveTo(x, y);
        X = x;
        Y = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - X);
        float dy = Math.abs(y - Y);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            PATH.quadTo(X, Y, (x + X) / 2, (y + Y) / 2);
            X = x;
            Y = y;
        }
    }

    private void touchUp() {
        PATH.lineTo(X, Y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                break;
        }

        return true;
    }

    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        invalidate();
    }
}
