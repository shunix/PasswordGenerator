package com.shunix.encryptor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.shunix.encryptor.R;

/**
 * @author shunix
 * @since 2015/11/16
 */
public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity_layout);
        getFragmentManager().beginTransaction().add(R.id.container, new SettingsFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, PasswordListActivity.class);
        intent.putExtra(JUMP_WITHIN_APP, true);
        startActivity(intent);
        finish();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            final ListPreference mPref = (ListPreference) findPreference(getString(R.string.pref_enc_key));
            if (mPref.getValue() == null) {
                mPref.setValueIndex(0);
            }
            mPref.setSummary(mPref.getEntry());
            mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    mPref.setValue(o.toString());
                    preference.setSummary(mPref.getEntry());
                    return true;
                }
            });
        }
    }
}
