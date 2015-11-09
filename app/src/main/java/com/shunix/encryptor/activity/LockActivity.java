package com.shunix.encryptor.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.shunix.encryptor.R;
import com.shunix.encryptor.app.EncryptorApplication;
import com.shunix.encryptor.utils.RootPasswordUtil;

/**
 * @author shunix
 * @since 2015/10/28
 */
public class LockActivity extends Activity {
    private EditText mPwdEditText;
    private TextView mHintTextView;
    private EncryptorApplication mApp;
    private Animation mShakeAnim;
    private boolean mIsSetRootPwd;
    private static final String TAG = LockActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (EncryptorApplication) getApplication();
        setContentView(R.layout.lock_activity_layout);
        mShakeAnim = AnimationUtils.loadAnimation(mApp, R.anim.edit_text_shake);
        mPwdEditText = (EditText) findViewById(R.id.passText);
        mHintTextView = (TextView) findViewById(R.id.textView);
        mIsSetRootPwd = TextUtils.isEmpty(RootPasswordUtil.getEncryptedRootPwd(mApp));
        initViews();
    }

    private void initViews() {
        if (mIsSetRootPwd) {
            mHintTextView.setVisibility(View.VISIBLE);
        } else {
            mHintTextView.setVisibility(View.INVISIBLE);
        }
        mPwdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String text = mPwdEditText.getText().toString();
                    if (mIsSetRootPwd) {
                        if (TextUtils.isEmpty(text)) {
                            mPwdEditText.setText("");
                            mPwdEditText.startAnimation(mShakeAnim);
                        } else {
                            RootPasswordUtil.saveEncryptedRootPwd(mApp, text);
                            mApp.setRootPwdInMemory(text);
                            mApp.setLocked(false);
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else {
                        if (TextUtils.isEmpty(text)) {
                            mPwdEditText.setText("");
                            mPwdEditText.startAnimation(mShakeAnim);
                            return false;
                        }
                        if (RootPasswordUtil.getEncryptedRootPwd(mApp).equals(RootPasswordUtil.encryptRootPwd(text))) {
                            mApp.setRootPwdInMemory(text);
                            mApp.setLocked(false);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            mPwdEditText.setText("");
                            mPwdEditText.startAnimation(mShakeAnim);
                        }
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
