package com.shunix.encryptor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

/**
 * @author rayewang
 * @since 2016/01/19
 */
public class FloatingService extends Service{
    public final static short SHOW_FLOATING_WINDOW = 0;
    public final static short HIDE_FLOATING_WINDOW = 1;
    public final static String KEY = "action";
    public final static String INTENT_ACTION = "com.shunix.encryptor.action";
    private View mFloatingView;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            short action = intent.getShortExtra(KEY, HIDE_FLOATING_WINDOW);
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
        buildFloatingView();
    }

    private void hideFloatingWindow() {

    }

    private void buildFloatingView() {
        if (mFloatingView == null) {
            mFloatingView = new TextView(getApplicationContext());
        }
    }
}
