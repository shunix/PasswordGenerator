package com.shunix.encryptor.database;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.shunix.encryptor.app.EncryptorApplication;
import com.shunix.encryptor.utils.AESEncryptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author shunix
 * @since 2015/11/10
 */
public class PasswordSerializer {
    private String mRootPwd;
    private Context mContext;
    private AESEncryptor mEncryptor;
    private static final String TAG = PasswordSerializer.class.getName();
    // �ֶηָ���
    private static final String COL_DIVIDER = "\001";
    // ��¼�ָ���
    private static final String RECORD_DIVIDER = "\002";

    /**
     * ����context��Ҫ����ApplicationContext
     * @param context
     */
    public PasswordSerializer(Context context) {
        try {
            mRootPwd = ((EncryptorApplication) context).getRootPwdInMemory();
            mContext = context;
            mEncryptor = new AESEncryptor(mRootPwd);
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * ���л�DB�е����ݣ�Ĭ�ϱ��浽���ô洢��Ӧ�ó���Ŀ¼���ļ�����ʱ����
     */
    public boolean serialize() {
        DatabaseManager databaseManager = new DatabaseManager(mContext);
        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.e(TAG, "SD card not mounted");
                return false;
            }
            File rootDir = mContext.getExternalFilesDir(null);
            if (!rootDir.exists()) {
                rootDir.mkdir();
            }
            File data = new File(rootDir.getAbsolutePath() + File.separator + "data.dat");
            if (data.exists()) {
                data.delete();
            }
            if (data.createNewFile()) {
                PrintWriter writer = new PrintWriter(data);
                List<DatabaseManager.PasswordEntity> entities = databaseManager.getAllPasswords();
                for (DatabaseManager.PasswordEntity entity : entities) {
                    writer.print(convertEntityToString(entity));
                }
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        } finally {
            databaseManager.close();
        }
        return true;
    }

    /**
     * �����л���DB��Ĭ�ϴ����ô洢��Ӧ�ó���Ŀ¼��ȡ
     *
     * @return
     */
    public boolean deserialize() {
        DatabaseManager databaseManager = new DatabaseManager(mContext);
        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.e(TAG, "SD card not mounted");
                return false;
            }
            File rootDir = mContext.getExternalFilesDir(null);
            if (!rootDir.exists()) {
                Log.e(TAG, "File not exist");
                return false;
            }
            File data = new File(rootDir.getAbsolutePath() + File.separator + "data.dat");
            if (!data.exists()) {
                Log.e(TAG, "File not exist");
                return false;
            }
            BufferedReader reader = new BufferedReader(new FileReader(data));
            StringBuffer sb = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
            reader.close();
            String encryptedText = sb.toString();
            String[] splitedText = encryptedText.split(RECORD_DIVIDER);
            for (String text : splitedText) {
                DatabaseManager.PasswordEntity entity = convertStringToEntity(text);
                insertEntityToDB(entity);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        } finally {
            databaseManager.close();
        }
        return true;
    }

    private String convertEntityToString(DatabaseManager.PasswordEntity entity) {
        StringBuffer sb = new StringBuffer();
        String name = mEncryptor.encrypt(entity.name);
        sb.append(TextUtils.isEmpty(name) ? "" : name);
        sb.append(COL_DIVIDER);
        String password = mEncryptor.encrypt(entity.password);
        sb.append(TextUtils.isEmpty(password) ? "" : password);
        sb.append(COL_DIVIDER);
        String timestamp = mEncryptor.encrypt(String.valueOf(entity.timestamp));
        sb.append(TextUtils.isEmpty(timestamp) ? "" : timestamp);
        sb.append(RECORD_DIVIDER);
        return sb.toString();
    }

    private DatabaseManager.PasswordEntity convertStringToEntity(String str) {
        String[] strings = str.split(COL_DIVIDER);
        if (strings.length != 3) {
            return null;
        }
        DatabaseManager.PasswordEntity entity = new DatabaseManager.PasswordEntity();
        String name = mEncryptor.decrypt(strings[0]);
        entity.name = TextUtils.isEmpty(name) ? "" : name;
        String password = mEncryptor.decrypt(strings[1]);
        entity.password = TextUtils.isEmpty(password) ? "" : password;
        String timestamp = mEncryptor.decrypt(strings[2]);
        entity.timestamp = Long.parseLong(timestamp);
        return entity;
    }

    private boolean insertEntityToDB(DatabaseManager.PasswordEntity entity) {
        DatabaseManager databaseManager = new DatabaseManager(mContext);
        try {
            return databaseManager.insertPassword(entity.name, entity.password, entity.timestamp);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            databaseManager.close();
        }
        return false;
    }
}