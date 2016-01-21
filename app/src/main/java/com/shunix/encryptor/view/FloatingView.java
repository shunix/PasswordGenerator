package com.shunix.encryptor.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.shunix.encryptor.R;
import com.shunix.encryptor.service.FloatingService;

/**
 * @author shunix
 * @since 2016/01/20
 */
public class FloatingView extends TextView {
    private final static String TAG = FloatingView.class.getName();
    private long mLastTapTime;
    private WindowManager mWindowManager;
    public FloatingView(Context context) {
        super(context);
        setBackgroundResource(R.drawable.lock_edit_text_shape_normal);
        setGravity(Gravity.CENTER);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.floating));
        setTextColor(getResources().getColor(R.color.matrix_green));
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundResource(R.drawable.lock_edit_text_shape_pressed);
                long currentTime = System.currentTimeMillis();
                if (currentTime - mLastTapTime <= 300) {
                    Intent intent = new Intent(FloatingService.INTENT_ACTION);
                    intent.putExtra(FloatingService.KEY, FloatingService.HIDE_FLOATING_WINDOW);
                    getContext().sendBroadcast(intent);
                }
                mLastTapTime = currentTime;
                Log.d(TAG, "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                setBackgroundResource(R.drawable.lock_edit_text_shape_normal);
                Log.d(TAG, "ACTION_UP");
                break;
            case MotionEvent.ACTION_MOVE:
                setBackgroundResource(R.drawable.lock_edit_text_shape_pressed);
                float positionX = event.getRawX() - (getWidth() / 2);
                float positionY = event.getRawY() - getHeight();
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
                params.x = (int) positionX;
                params.y = (int) positionY;
                mWindowManager.updateViewLayout(this, params);
                Log.d(TAG, "ACTION_MOVE");
                break;
            default:
                break;
        }
        return true;
    }
}
