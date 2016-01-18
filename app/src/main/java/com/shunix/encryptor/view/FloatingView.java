package com.shunix.encryptor.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.shunix.encryptor.R;

/**
 * @author shunix
 * @since 2015/11/26
 */
public class FloatingView extends LinearLayout {
    private float mPreviousX;
    private float mPreviousY;
    private float mCurrentX;
    private float mCurrentY;
    private WindowManager mWindowManager;
    private ClipboardManager mClipboardManager;
    private TextView mPwdText;

    private static float DELTA = 5.0f;
    private static final String KEY_DATA = "data";
    private static final String TAG = FloatingView.class.getName();

    public FloatingView(Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.floating_view_layout, this);
        mPwdText = (TextView) findViewById(R.id.pwdText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mPreviousX = event.getRawX();
                mPreviousY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                mCurrentX = event.getRawX();
                mCurrentY = event.getRawY();
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int) mCurrentX, (int) mCurrentY);
                mWindowManager.updateViewLayout(this, params);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (Math.abs(mCurrentX - mPreviousX) < DELTA && Math.abs(mCurrentY - mPreviousY) < DELTA) {
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int) mPreviousX, (int) mPreviousY);
                    mWindowManager.updateViewLayout(this, params);
                    try {
                        mClipboardManager.setPrimaryClip(ClipData.newPlainText(KEY_DATA, mPwdText.getText()));
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                break;
            }
            default:
                break;
        }
        return true;
    }
}
