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

public class Joystick {
    public static final int STICK_NONE = 0;
    public static final int DIR_N = 1;
    public static final int DIR_NE = 2;
    public static final int DIR_E = 3;
    public static final int DIR_SE = 4;
    public static final int DIR_S = 5;
    public static final int DIR_SW = 6;
    public static final int DIR_W = 7;
    public static final int DIR_NW = 8;

    private int STICK_ALPHA = 200;
    private int LAYOUT_ALPHA = 200;
    private int OFFSET = 0;

    private ViewGroup mLayout;
    private LayoutParams params;
    private int stick_width, stick_height;

    private int position_x = 0, position_y = 0, deadzone = 0;
    private float distance = 0, angle = 0;

    private DrawCanvas stick;
    private Paint paint;
    private Bitmap stickImg;
    private OnMoveHandler moveHandler;

    private boolean touch_state = false;

    public interface OnMoveHandler {
        void onMove(View v, MotionEvent event);
    }

    public Joystick (Context context, ViewGroup layout, int stick_res_id) {

        stickImg = BitmapFactory.decodeResource(context.getResources(),
                stick_res_id);

        stick_width = stickImg.getWidth();
        stick_height = stickImg.getHeight();
        OFFSET = stick_width/2;
        deadzone = 100;

        stick = new DrawCanvas(context);
        paint = new Paint();
        mLayout = layout;
        params = mLayout.getLayoutParams();

        setStickSize(params.width / 4, params.height / 4);

        layout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent e) {
                drawStick(e);
                if (moveHandler != null) {
                    moveHandler.onMove(view, e); }
                return true;
            }
        });

        stick.setPos(params.width / 2, params.height / 2);
        draw();
    }

    private int limit(int num, int min, int max) {
        if(num > max){return max;}
        if(num < min){return min;}
        return num;
    }

    private float limit(float num, float min, float max) {
        if(num > max){return max;}
        if(num < min){return min;}
        return num;
    }

    private void drawStick(MotionEvent e) {
        position_x = (int) (e.getX() - (params.width / 2));
        position_y = (int) (e.getY() - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(position_x, 2) + Math.pow(position_y, 2));
        angle = (float) cal_angle(position_x, position_y);


        if(e.getAction() == MotionEvent.ACTION_DOWN) {
            if(distance <= (params.width / 2)) { // - OFFSET
                float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y))) * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y))) * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                stick.setPos(x, y);
                touch_state = true;
            }
            if(distance < deadzone) {
                stick.setPos(params.width/2, params.height/2);
            }
        } else if(e.getAction() == MotionEvent.ACTION_MOVE && touch_state) {
            if(distance <= (params.width / 2) - OFFSET) {
                stick.setPos(e.getX(), e.getY());
            } else if(distance > (params.width / 2) - OFFSET) {
                float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y))) * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y))) * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                stick.setPos(x, y);
            }
            if(distance < deadzone) {
                stick.setPos(params.width/2, params.height/2);
            }
        } else if(e.getAction() == MotionEvent.ACTION_UP) {
            stick.setPos(params.width / 2, params.height / 2);
            touch_state = false;
        }
        draw();
    }

    public void zeroStick() {
        stick.setPos(params.width / 2, params.height / 2);
        touch_state = false;
    }

    public int[] getPosition() {
        if(distance > deadzone && touch_state) {
            return new int[] { limit(position_x, -stick_width, stick_width), limit(position_y, -stick_height, stick_height) };
        }
        return new int[] { 0, 0 };
    }

    public float getX() {
        if(distance > deadzone && touch_state) {
            float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y))) * limit(distance, 0, params.width/2)); //((params.width / 2) - OFFSET)
            x = x / (params.width/2);
            return x;
        }
        return 0;
    }

    public float getY() {
        if(distance > deadzone && touch_state) {
            float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y))) * limit(distance, 0, params.height/2)); //((params.height / 2) - OFFSET)
            y = y / (params.height/2);
            return -y;
        }
        return 0;
    }

    public float getAngle() {
        if(distance > deadzone && touch_state) {
            return angle;
        }
        return 0;
    }

    public float getDistance() {
        if(distance > deadzone && touch_state) {
            return distance;
        }
        return 0;
    }

    public void setDeadzone(int d) {
        deadzone = d;
    }

    public int getDeadzone() {
        return deadzone;
    }

    public int get8Direction() {
        if(distance > deadzone && touch_state) {
            if(angle >= 247.5 && angle < 292.5 ) {
                return DIR_N;
            } else if(angle >= 292.5 && angle < 337.5 ) {
                return DIR_NE;
            } else if(angle >= 337.5 || angle < 22.5 ) {
                return DIR_E;
            } else if(angle >= 22.5 && angle < 67.5 ) {
                return DIR_SE;
            } else if(angle >= 67.5 && angle < 112.5 ) {
                return DIR_S;
            } else if(angle >= 112.5 && angle < 157.5 ) {
                return DIR_SW;
            } else if(angle >= 157.5 && angle < 202.5 ) {
                return DIR_W;
            } else if(angle >= 202.5 && angle < 247.5 ) {
                return DIR_NW;
            }
        } else if(distance <= deadzone && touch_state) {
            return STICK_NONE;
        }
        return 0;
    }

    public int get4Direction() {
        if(distance > deadzone && touch_state) {
            if(angle >= 225 && angle < 315 ) {
                return DIR_N;
            } else if(angle >= 315 || angle < 45 ) {
                return DIR_E;
            } else if(angle >= 45 && angle < 135 ) {
                return DIR_S;
            } else if(angle >= 135 && angle < 225 ) {
                return DIR_W;
            }
        } else if(distance <= deadzone && touch_state) {
            return STICK_NONE;
        }
        return 0;
    }

    public void setOnMoveHandler(OnMoveHandler h) { moveHandler = h; }

    public void setOffset(int offset) {
        OFFSET = offset;
    }

    public int getOffset() {
        return OFFSET;
    }

    public void setStickAlpha(int alpha) {
        STICK_ALPHA = alpha;
        paint.setAlpha(alpha);
    }

    public int getStickAlpha() {
        return STICK_ALPHA;
    }

    public void setLayoutAlpha(int alpha) {
        LAYOUT_ALPHA = alpha;
        mLayout.getBackground().setAlpha(alpha);
    }

    public int getLayoutAlpha() {
        return LAYOUT_ALPHA;
    }

    public void setStickSize(int width, int height) {
        stickImg = Bitmap.createScaledBitmap(stickImg, width, height, false);
        stick_width = stickImg.getWidth();
        stick_height = stickImg.getHeight();
    }

    public void setStickWidth(int width) {
        stickImg = Bitmap.createScaledBitmap(stickImg, width, stick_height, false);
        stick_width = stickImg.getWidth();
    }

    public void setStickHeight(int height) {
        stickImg = Bitmap.createScaledBitmap(stickImg, stick_width, height, false);
        stick_height = stickImg.getHeight();
    }

    public int getStickWidth() {
        return stick_width;
    }

    public int getStickHeight() {
        return stick_height;
    }

    public void setLayoutSize(int width, int height) {
        params.width = width;
        params.height = height;
    }

    public int getLayoutWidth() {
        return params.width;
    }

    public int getLayoutHeight() {
        return params.height;
    }

    private double cal_angle(float x, float y) {
        if(x >= 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x));
        else if(x < 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if(x < 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if(x >= 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 360;
        return 0;
    }

    private void draw() {
        try {
            mLayout.removeView(stick);
        } catch (Exception ignored) { }
        mLayout.addView(stick);
    }

    private class DrawCanvas extends View{
        float x, y;

        private DrawCanvas(Context mContext) {
            super(mContext);
        }

        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(stickImg, x, y, paint);
        }

        private void setPos(float pos_x, float pos_y) {
            x = pos_x - (stick_width / 2);
            y = pos_y - (stick_height / 2);
        }
    }
}
