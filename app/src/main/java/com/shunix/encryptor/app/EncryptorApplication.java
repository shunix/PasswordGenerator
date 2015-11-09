package com.shunix.encryptor.app;

import android.app.Application;
import android.util.Log;
import com.shunix.encryptor.utils.AESEncryptor;
import com.shunix.encryptor.utils.RootPasswordUtil;

/**
 * @author shunix
 * @since 2015/10/27
 */
public class EncryptorApplication extends Application {
    // 用于加密存储在内存中的root key
    private static final String PRE_DEFINED_KEY = "c2h1bml4IA0K";
    protected boolean mIsLocked;
    protected byte[] mEncryptedRootPwd;
    protected AESEncryptor mEncryptor;
    private static final String TAG = EncryptorApplication.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        mIsLocked = false;
        mEncryptedRootPwd = null;
        mEncryptor = new AESEncryptor(PRE_DEFINED_KEY);
    }

    public boolean isLocked() {
        return mIsLocked;
    }

    public void setLocked(boolean isLocked) {
        mIsLocked = isLocked;
    }

    /**
     * 这个方法只设置Application中的root密码，设置前会校验是否正确
     * @param rootPwd
     */
    public boolean setRootPwdInMemory(String rootPwd) {
        boolean result = false;
        try {
            if (RootPasswordUtil.getEncryptedRootPwd(this).equalsIgnoreCase(RootPasswordUtil.encryptRootPwd(rootPwd))) {
                mEncryptedRootPwd = mEncryptor.encrypt(rootPwd).getBytes("UTF-8");
                result = true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }

    /**
     * 这个方法仅用于获取Application对象中的root密码，没有IO操作
     * @return
     */
    public String getRootPwdInMemory() {
        String rootPwd = null;
        try {
            rootPwd = mEncryptor.decrypt(new String(mEncryptedRootPwd, "UTF-8"));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return rootPwd;
    }
}
