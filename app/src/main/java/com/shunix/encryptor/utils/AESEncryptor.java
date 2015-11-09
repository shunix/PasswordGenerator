package com.shunix.encryptor.utils;

import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * @author shunix
 * @since 2015/10/20
 */
public class AESEncryptor {
    private byte[] mKeyBytes;
    private static final String TAG = AESEncryptor.class.getName();

    public AESEncryptor(String key) {
        KeyGenerator generator = null;
        try {
            generator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                secureRandom = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            } else {
                secureRandom = SecureRandom.getInstance("SHA1PRNG");
            }
            secureRandom.setSeed(key.getBytes());
            generator.init(128, secureRandom);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        if (generator != null) {
            mKeyBytes = generator.generateKey().getEncoded();
        }
    }

    private byte[] encrypt(byte[] srcBuffer) {
        if (mKeyBytes == null || mKeyBytes.length == 0) {
            return null;
        }
        try {
            SecretKeySpec spec = new SecretKeySpec(mKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            return cipher.doFinal(srcBuffer);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private byte[] decrypt(byte[] srcBuffer) {
        if (mKeyBytes == null || mKeyBytes.length == 0) {
            return null;
        }
        try {
            SecretKeySpec spec = new SecretKeySpec(mKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, spec);
            return cipher.doFinal(srcBuffer);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public String encrypt(String src) {
        if (TextUtils.isEmpty(src)) {
            return null;
        }
        try {
            byte[] srcBuffer = src.getBytes("UTF-8");
            byte[] dstBuffer = encrypt(srcBuffer);
            if (dstBuffer != null && dstBuffer.length > 0) {
                return Base64.encodeToString(dstBuffer, Base64.NO_WRAP);
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public String decrypt(String src) {
        if (TextUtils.isEmpty(src)) {
            return null;
        }
        try {
            byte[] srcBuffer = Base64.decode(src, Base64.NO_WRAP);
            byte[] dstBuffer = decrypt(srcBuffer);
            if (dstBuffer != null && dstBuffer.length > 0) {
                return new String(dstBuffer, "UTF-8");
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
}
