package com.quantum.qtech.qtechfrcdriverstation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class Slider {

    private int SLIDE_ALPHA = 200;
    private int LAYOUT_ALPHA = 200;

    private ViewGroup mLayout;
    private LayoutParams params;
    private int slide_width, slide_height;

    private float deadzone = 15;
    private float value = 0;

    private DrawCanvas slide;
    private Paint paint;
    private Bitmap slideImg;
    private OnMoveHandler moveHandler;

    private boolean touch_state = false;

    public interface OnMoveHandler {
        void onMove(View v, MotionEvent event);
    }

    public Slider(Context context, ViewGroup layout, int slideImg_res_id) {

        slideImg = BitmapFactory.decodeResource(context.getResources(),
                slideImg_res_id);

        slide_width = slideImg.getWidth();
        slide_height = slideImg.getHeight();

        slide = new DrawCanvas(context);
        paint = new Paint();
        mLayout = layout;
        params = mLayout.getLayoutParams();

        setSlideSize(params.height / 4, params.width / 4);

        layout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent e) {
                drawSlide(e);
                if (moveHandler != null) {
                    moveHandler.onMove(view, e); }
                return true;
            }
        });

        slide.setPos(params.width / 2, params.height / 2);
        draw();
    }

    private void drawSlide(MotionEvent e) {
        if(e.getAction() == MotionEvent.ACTION_DOWN) {
            if(inRange(e)) {
                slide.setPos(params.width/2, e.getY());
                touch_state = true;
            }
        } else if(e.getAction() == MotionEvent.ACTION_MOVE && touch_state) {
            if(inRange(e)) {
                float y = e.getY() - (params.height/2);
                y = y / (params.height / 2);
                if(Math.abs(y) > (deadzone/100)) {
                    value = y;
                    slide.setPos(params.width/2, e.getY());
                }
                else
                {
                    value = 0;
                    slide.setPos(params.width/2, params.height/2);
                }

            }
        } else if(e.getAction() == MotionEvent.ACTION_UP) {
            touch_state = false;
        }
        draw();
    }

    public void zeroSlide() {
        value = 0;
        slide.setPos(params.width/2, params.height/2);
    }

    public float get() {
        return value;
    }

    public void setDeadzone(float d) {
        deadzone = d;
    }

    public float getDeadzone() {
        return deadzone;
    }

    public void setOnMoveHandler(OnMoveHandler h) { moveHandler = h; }

    public void setSlideAlpha(int alpha) {
        SLIDE_ALPHA = alpha;
        paint.setAlpha(alpha);
    }

    public int getSlideAlpha() {
        return SLIDE_ALPHA;
    }

    public void setAlpha(int alpha) {
        LAYOUT_ALPHA = alpha;
        mLayout.getBackground().setAlpha(alpha);
    }

    public int getAlpha() {
        return LAYOUT_ALPHA;
    }

    public void setSlideSize(int width, int height) {
        slideImg = Bitmap.createScaledBitmap(slideImg, width, height, false);
        slide_width = slideImg.getWidth();
        slide_height = slideImg.getHeight();
    }

    public void setSlideWidth(int width) {
        slideImg = Bitmap.createScaledBitmap(slideImg, width, slide_height, false);
        slide_width = slideImg.getWidth();
    }

    public void setSlideHeight(int height) {
        slideImg = Bitmap.createScaledBitmap(slideImg, slide_width, height, false);
        slide_height = slideImg.getHeight();
    }

    public int getSlideWidth() {
        return slide_width;
    }

    public int getSlideHeight() {
        return slide_height;
    }

    public void setSize(int width, int height) {
        params.width = width;
        params.height = height;
    }

    public int getWidth() {
        return params.width;
    }

    public int getHeight() {
        return params.height;
    }

    private boolean inRange(MotionEvent e)
    {
        float y = e.getY();
        float layoutH = mLayout.getHeight();

        return (y > 0) && (y < layoutH);
    }

    private void draw() {
        try {
            mLayout.removeView(slide);
        } catch (Exception ignored) { }
        mLayout.addView(slide);
    }

    private class DrawCanvas extends View{
        float x, y;

        private DrawCanvas(Context mContext) {
            super(mContext);
        }

        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(slideImg, x, y, paint);
        }

        private void setPos(float pos_x, float pos_y) {
            x = pos_x - (slide_width / 2);
            y = pos_y - (slide_height / 2);
        }
    }
}
