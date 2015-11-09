package com.shunix.encryptor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.shunix.encryptor.app.EncryptorApplication;

/**
 * @author shunix
 * @since 2015/10/27
 */
public class BaseActivity extends Activity{
    protected static final String JUMP_WITHIN_APP = "jump_within_app";
    protected static final int REQUEST_LOCK_CODE = 0;
    protected EncryptorApplication mApp;
    private static final String TAG = BaseActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (EncryptorApplication) getApplication();
        mApp.setLocked(true);
        if (getIntent().getBooleanExtra(JUMP_WITHIN_APP, false)) {
            mApp.setLocked(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (getIntent().getBooleanExtra(JUMP_WITHIN_APP, false)) {
            mApp.setLocked(false);
        }
    }

    @Override
    protected void onResume() {
        if (mApp.isLocked()) {
            Intent intent = new Intent(this, LockActivity.class);
            startActivityForResult(intent, REQUEST_LOCK_CODE);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        mApp.setLocked(true);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCK_CODE) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }
    }
}
