package com.shunix.encryptor.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.shunix.encryptor.R;
import com.shunix.encryptor.database.DatabaseManager;
import com.shunix.encryptor.utils.AESEncryptor;

/**
 * @author shunix
 * @since 2015/11/05
 */
public class AddPasswordActivity extends BaseActivity {
    private EditText mEditText;
    private ProgressBar mProgressBar;
    private Animation mShakeAnim;
    private DatabaseManager mDatabaseManager;
    private static final String TAG = AddPasswordActivity.class.getName();

    TextView.OnEditorActionListener mListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i == EditorInfo.IME_ACTION_DONE
                    || i == EditorInfo.IME_ACTION_SEND
                    || (keyEvent != null && KeyEvent.KEYCODE_ENTER == keyEvent.getKeyCode() && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                String text = mEditText.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    mEditText.startAnimation(mShakeAnim);
                } else {
                    try {
                        PasswordTask task = new PasswordTask();
                        task.execute(text);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
            return false;
        }
    };

    class PasswordTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... strings) {
            String[] params = strings.clone();
            String encryptedPwd = null;
            if (params.length != 1) {
                cancel(true);
            }
            if (!TextUtils.isEmpty(params[0])) {
                String result = mDatabaseManager.queryPassword(params[0]);
                if (!TextUtils.isEmpty(result)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mApp, getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                        }
                    });
                    cancel(true);
                } else {
                    String rootPwd = mApp.getRootPwdInMemory();
                    if (!TextUtils.isEmpty(rootPwd)) {
                        long timestamp = System.currentTimeMillis();
                        AESEncryptor encryptor = new AESEncryptor(rootPwd);
                        encryptedPwd = encryptor.encrypt(params[0] + timestamp);
                        if (encryptedPwd.length() > 16) {
                            encryptedPwd = encryptedPwd.substring(0, 16);
                        }
                        boolean dbResult = mDatabaseManager.insertPassword(params[0], encryptedPwd, timestamp);
                        if (!dbResult) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mApp, getString(R.string.db_op_failed), Toast.LENGTH_SHORT).show();
                                }
                            });
                            cancel(true);
                        }
                    }
                }
            }
            return new String[] {params[0], encryptedPwd};
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            mProgressBar.setVisibility(View.GONE);
            startResultActivity(s[0], s[1]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        private void startResultActivity(String name, String pwd) {
            Intent intent = new Intent(AddPasswordActivity.this, AddPasswordResultActivity.class);
            intent.putExtra(AddPasswordResultActivity.NAME_KEY, name);
            intent.putExtra(AddPasswordResultActivity.PWD_KEY, pwd);
            intent.putExtra(JUMP_WITHIN_APP, true);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_pwd_activity_layout);
        mShakeAnim = AnimationUtils.loadAnimation(mApp, R.anim.edit_text_shake);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mEditText = (EditText) findViewById(R.id.passText);
        mEditText.setOnEditorActionListener(mListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseManager = new DatabaseManager(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabaseManager.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AddPasswordActivity.this, PasswordListActivity.class);
        intent.putExtra(JUMP_WITHIN_APP, true);
        startActivity(intent);
        finish();
    }
}
