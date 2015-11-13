package com.shunix.encryptor.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.shunix.encryptor.R;
import com.shunix.encryptor.database.PasswordSerializer;

import java.lang.ref.WeakReference;

/**
 * @author shunix
 * @since 2015/11/11
 */
public class BackupActivity extends BaseActivity {
    private EditText mEmailText;
    private ImageView mStatusIcon;
    private TextView mFileText;
    private Button mSendButton;
    private Animation mShakeAnim;
    private final static String TAG = BackupActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_activity_layout);
        mShakeAnim = AnimationUtils.loadAnimation(mApp, R.anim.edit_text_shake);
        mEmailText = (EditText) findViewById(R.id.emailText);
        mStatusIcon = (ImageView) findViewById(R.id.statusIcon);
        mFileText = (TextView) findViewById(R.id.fileText);
        mSendButton = (Button) findViewById(R.id.sendBtn);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddr = mEmailText.getText().toString();
                if (TextUtils.isEmpty(emailAddr)) {
                    mEmailText.setText("");
                    mEmailText.startAnimation(mShakeAnim);
                } else {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddr});
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.backup));
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.backup));
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + PasswordSerializer.getBackupFilePath(mApp)));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(false);
        BackupTask task = new BackupTask(this);
        task.execute();
    }

    void updateUI(boolean isReady) {
        if (isReady) {
            String filePath = PasswordSerializer.getBackupFilePath(mApp);
            mFileText.setText(filePath);
            mStatusIcon.setImageResource(R.drawable.checkmark);
            mSendButton.setBackgroundResource(R.drawable.lock_edit_text_shape);
            mSendButton.setClickable(true);
        } else {
            mFileText.setText(getString(R.string.file_not_ready));
            mStatusIcon.setImageResource(R.drawable.error_alert);
            mSendButton.setBackgroundResource(R.drawable.lock_edit_text_shape_pressed);
            mSendButton.setClickable(false);
        }

    }

    static class BackupTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<BackupActivity> mActivity;

        public BackupTask(BackupActivity activity) {
            this.mActivity = new WeakReference<BackupActivity>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (mActivity.get() != null) {
                    PasswordSerializer serializer = new PasswordSerializer(mActivity.get().mApp);
                    return serializer.serialize();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (mActivity.get() != null) {
                mActivity.get().updateUI(aBoolean);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, PasswordListActivity.class);
        intent.putExtra(JUMP_WITHIN_APP, true);
        startActivity(intent);
        finish();
    }
}
