package com.shunix.encryptor.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.shunix.encryptor.R;
import com.shunix.encryptor.database.PasswordSerializer;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author shunix
 * @since 2015/11/12
 */
public class RestoreActivity extends BaseActivity {
    private ImageView mStatusIcon;
    private TextView mFileText;
    private Button mRestoreBtn;
    private static final String TAG = RestoreActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_activity_layout);
        mStatusIcon = (ImageView) findViewById(R.id.statusIcon);
        mFileText = (TextView) findViewById(R.id.fileText);
        mRestoreBtn = (Button) findViewById(R.id.restoreBtn);
        mRestoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RestoreTask restoreTask = new RestoreTask(RestoreActivity.this);
                restoreTask.execute();
            }
        });
    }

    private void updateUI() {
        String restoreFilePath = PasswordSerializer.getRestoreFilePath();
        if (restoreFilePath != null) {
            File file = new File(restoreFilePath);
            if (file.exists()) {
                mStatusIcon.setImageResource(R.drawable.checkmark);
                mFileText.setText(restoreFilePath);
                mRestoreBtn.setBackgroundResource(R.drawable.lock_edit_text_shape);
                mRestoreBtn.setClickable(true);
                return;
            }
        }
        mStatusIcon.setImageResource(R.drawable.error_alert);
        mFileText.setText(R.string.file_not_ready);
        mRestoreBtn.setBackgroundResource(R.drawable.lock_edit_text_shape_pressed);
        mRestoreBtn.setClickable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    void updateResult(boolean isSuccess) {
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onBackPressed();
            }
        };
        if (isSuccess) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.restore_result))
                    .setMessage(getString(R.string.restore_result_success))
                    .setPositiveButton(getString(R.string.ok), onClickListener).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.restore_result))
                    .setMessage(getString(R.string.restore_result_failed))
                    .setPositiveButton(getString(R.string.ok), onClickListener).show();
        }
    }

    static class RestoreTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<RestoreActivity> mActivity;

        public RestoreTask(RestoreActivity activity) {
            this.mActivity = new WeakReference<RestoreActivity>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (mActivity.get() != null) {
                PasswordSerializer serializer = new PasswordSerializer(mActivity.get().mApp);
                return serializer.deserialize();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (mActivity.get() != null) {
                mActivity.get().updateResult(aBoolean);
            }
        }
    }

    private void startPasswordListActivity() {
        Intent intent = new Intent(this, PasswordListActivity.class);
        intent.putExtra(JUMP_WITHIN_APP, true);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startPasswordListActivity();
        finish();
    }
}
