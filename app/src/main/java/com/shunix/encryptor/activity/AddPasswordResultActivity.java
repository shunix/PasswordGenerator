package com.shunix.encryptor.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.shunix.encryptor.R;

/**
 * @author shunix
 * @since 2015/11/07
 */
public class AddPasswordResultActivity extends BaseActivity {
    public static final String NAME_KEY = "name";
    public static final String PWD_KEY = "pwd";
    public static final String KEY_DATA = "data";
    private static final String TAG = AddPasswordResultActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_pwd_result_activity_layout);
        initViews();
    }

    private void initViews() {
        final TextView nameText = (TextView) findViewById(R.id.nameText);
        final TextView pwdText = (TextView) findViewById(R.id.pwdText);
        final Button copyBtn = (Button) findViewById(R.id.copyBtn);
        nameText.setText(getIntent().getStringExtra(NAME_KEY));
        pwdText.setText(getIntent().getStringExtra(PWD_KEY));
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(KEY_DATA, pwdText.getText()));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
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
