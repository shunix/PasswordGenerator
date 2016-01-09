package com.shunix.encryptor.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import com.shunix.encryptor.aidl.IFloatingInterface;
import com.shunix.encryptor.view.FloatingView;

/**
 * @author shunix
 * @since 2015/1/6
 */
public class FloatingService extends Service {
    private WindowManager mWindowManager;
    private FloatingView mFloatingView;
    IFloatingInterface.Stub mBinder = new IFloatingInterface.Stub() {
        @Override
        public void showFloatingView() throws RemoteException {
            DisplayMetrics metrics = new DisplayMetrics();
            if (mWindowManager == null) {
                return;
            }
            mWindowManager.getDefaultDisplay().getMetrics(metrics);
            if (mFloatingView == null) {
                mFloatingView = new FloatingView(getApplicationContext());
            }
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            params.format = PixelFormat.RGBA_8888;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.width = dpToPx(120);
            params.height = dpToPx(30);
            params.x = metrics.widthPixels;
            params.y = metrics.heightPixels / 2;
            mFloatingView.setLayoutParams(params);
            mWindowManager.addView(mFloatingView, params);
        }

        @Override
        public void removeFloatingView() throws RemoteException {
            if (mFloatingView != null && mWindowManager != null) {
                mWindowManager.removeView(mFloatingView);
                mFloatingView = null;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    public int dpToPx(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
