package com.shunix.encryptor.activity;

import android.app.AlertDialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.shunix.encryptor.R;
import com.shunix.encryptor.database.DatabaseManager;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author shunix
 * @since 2015/11/3
 */
public class PasswordListActivity extends BaseActivity {
    private ListView mListView;
    private Button mAddButton;
    private PasswordListAdapter mAdapter;
    private static final String TAG = PasswordListActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pwd_list_activity_layout);
        mAdapter = new PasswordListAdapter(PasswordListActivity.this);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);
        mAddButton = (Button) findViewById(R.id.addButton);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddPwdActivity();
            }
        });
        updateUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                startAddPwdActivity();
                return true;
            case R.id.menu_backup:
                startBackupActivity();
                return true;
            case R.id.menu_restore:
                startRestoreActivity();
                return true;
            case R.id.menu_setting:
                startSettingsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void updateUI() {
        mAdapter.notifyDataSetChanged();
        if (mAdapter.getCount() == 0) {
            mAddButton.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mAddButton.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    final int pos = position;
                    AlertDialog.Builder builder = new AlertDialog.Builder(PasswordListActivity.this);
                    DialogInterface.OnClickListener onConfirmListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                DeletePasswordTask task = new DeletePasswordTask(PasswordListActivity.this);
                                task.execute(mAdapter.getItem(pos).name);
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    };
                    DialogInterface.OnClickListener onCancelListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    };
                    builder.setTitle(getString(R.string.warning))
                            .setMessage(getString(R.string.confirm_hint))
                            .setPositiveButton(R.string.yes, onConfirmListener)
                            .setNegativeButton(R.string.no, onCancelListener)
                            .show();
                    return true;
                }
            });
        }
    }

    private void startAddPwdActivity() {
        Intent intent = new Intent(PasswordListActivity.this, AddPasswordActivity.class);
        intent.putExtra(JUMP_WITHIN_APP, true);
        startActivity(intent);
    }

    private void startBackupActivity() {
        Intent intent = new Intent(PasswordListActivity.this, BackupActivity.class);
        intent.putExtra(JUMP_WITHIN_APP, true);
        startActivity(intent);
    }

    private void startRestoreActivity() {
        Intent intent = new Intent(PasswordListActivity.this, RestoreActivity.class);
        intent.putExtra(JUMP_WITHIN_APP, true);
        startActivity(intent);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(PasswordListActivity.this, SettingsActivity.class);
        intent.putExtra(JUMP_WITHIN_APP, true);
        startActivity(intent);
    }

    private static class DeletePasswordTask extends AsyncTask<String, Void, Boolean> {
        private WeakReference<Context> mContext;

        public DeletePasswordTask(Context context) {
            mContext = new WeakReference<Context>(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params.length != 1) {
                cancel(true);
                return Boolean.FALSE;
            }
            if (mContext.get() == null) {
                cancel(true);
                return Boolean.FALSE;
            }
            DatabaseManager databaseManager = new DatabaseManager(mContext.get());
            try {
                if (databaseManager.deletePassword(params[0])) {
                    return Boolean.TRUE;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                databaseManager.close();
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Context context = mContext.get();
            if (context != null) {
                if (aBoolean) {
                    if (context instanceof PasswordListActivity) {
                        ((PasswordListActivity) context).updateUI();
                    }
                } else {
                    Toast.makeText(context, mContext.get().getString(R.string.delete_pwd_failed), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static class PasswordListAdapter extends BaseAdapter {
        private Context mContext;
        private List<DatabaseManager.PasswordEntity> mDataList;
        public static final String KEY_DATA = "data";

        public PasswordListAdapter(Context context) {
            mContext = context;
            DatabaseManager databaseManager = new DatabaseManager(context);
            try {
                mDataList = databaseManager.getAllPasswords();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                databaseManager.close();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = generateRowView();
                holder = initViewHolder(convertView);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            initData(holder, position);
            convertView.setTag(holder);
            return convertView;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public DatabaseManager.PasswordEntity getItem(int position) {
            return mDataList.get(position);
        }

        private View generateRowView() {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.pwd_row_layout, null);
        }

        private ViewHolder initViewHolder(View convertView) {
            ViewHolder holder = new ViewHolder();
            if (!(convertView instanceof LinearLayout)) {
                return null;
            }
            holder.nameText = (TextView) convertView.findViewById(R.id.nameText);
            holder.pwdText = (TextView) convertView.findViewById(R.id.pwdText);
            holder.copyBtn = (Button) convertView.findViewById(R.id.copyBtn);
            return holder;
        }

        private void initData(final ViewHolder holder, int position) {
            DatabaseManager.PasswordEntity entity = mDataList.get(position);
            if (holder == null || entity == null) {
                return;
            }
            holder.nameText.setText(entity.name);
            holder.pwdText.setText(entity.password);
            holder.copyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(KEY_DATA, holder.pwdText.getText()));
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }

        static class ViewHolder {
            public TextView nameText;
            public TextView pwdText;
            public Button copyBtn;
        }

        @Override
        public void notifyDataSetChanged() {
            DatabaseManager databaseManager = new DatabaseManager(mContext);
            try {
                mDataList = databaseManager.getAllPasswords();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                databaseManager.close();
            }
            super.notifyDataSetChanged();
        }
    }
}
