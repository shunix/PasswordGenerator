package com.shunix.encryptor.service;

import android.app.Service;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.shunix.encryptor.view.FloatingView;

/**
 * @author rayewang
 * @since 2016/01/19
 */
public class FloatingService extends Service {
    public final static short SHOW_FLOATING_WINDOW = 0;
    public final static short HIDE_FLOATING_WINDOW = 1;
    public final static String KEY = "action";
    public final static String DATA = "data";
    public final static String INTENT_ACTION = "com.shunix.encryptor.action";
    private boolean mIsShow = false;
    private String mData;
    private View mFloatingView;
    private WindowManager mWindowManager;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            short action = intent.getShortExtra(KEY, HIDE_FLOATING_WINDOW);
            mData = intent.getStringExtra(DATA);
            switch (action) {
                case SHOW_FLOATING_WINDOW:
                    showFloatingWindow();
                    break;
                case HIDE_FLOATING_WINDOW:
                    hideFloatingWindow();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mReceiver, new IntentFilter(INTENT_ACTION));
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void showFloatingWindow() {
        if (mIsShow) {
            if (!TextUtils.isEmpty(mData) && !mData.equals(((FloatingView) mFloatingView).getText())) {
                ((FloatingView) mFloatingView).setText(mData);
            }
            return;
        }
        buildFloatingView();
        DisplayMetrics metrics = new DisplayMetrics();
        if (mWindowManager == null) {
            return;
        }
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.width = dpToPx(160);
        params.height = dpToPx(40);
        params.x = metrics.widthPixels;
        params.y = metrics.heightPixels / 2;
        mFloatingView.setLayoutParams(params);
        mWindowManager.addView(mFloatingView, params);
        mIsShow = true;
    }

    private void hideFloatingWindow() {
        if (mFloatingView != null && mWindowManager != null) {
            mWindowManager.removeView(mFloatingView);
            mFloatingView = null;
            mIsShow = false;
        }
    }

    private void buildFloatingView() {
        if (mFloatingView == null) {
            mFloatingView = new FloatingView(getApplicationContext());
            if (!TextUtils.isEmpty(mData) && !mData.equals(((FloatingView) mFloatingView).getText())) {
                ((FloatingView) mFloatingView).setText(mData);
            }
        }
    }

    private int dpToPx(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
