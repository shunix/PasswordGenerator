package com.shunix.encryptor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

/**
 * @author shunix
 * @since 2015/10/27
 */
public class RootPasswordUtil {
    private static final String SHARED_PREF_NAME = "root_pwd";
    private static final String PWD = "pwd";
    private static final String TAG = RootPasswordUtil.class.getName();

    public static String getEncryptedRootPwd(Context context) {
        String rootPwd = null;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            rootPwd = sharedPreferences.getString(PWD, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return rootPwd;
    }

    public static void saveEncryptedRootPwd(Context context, String rootPwd) {
        String encryptedRootPwd = encryptRootPwd(rootPwd);
        if (!TextUtils.isEmpty(encryptedRootPwd)) {
            try {
                SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PWD, encryptedRootPwd);
                editor.apply();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public static String encryptRootPwd(String rootPwd) {
        String result = null;
        try {
            byte[] srcBytes = rootPwd.getBytes("UTF-8");
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] dstBytes = digest.digest(srcBytes);
            result = Base64.encodeToString(dstBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }
}
