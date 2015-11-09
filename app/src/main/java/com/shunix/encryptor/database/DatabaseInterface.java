package com.shunix.encryptor.database;

/**
 * @author shunix
 * @since 2015/10/26
 */
public interface DatabaseInterface {
    String DB_NAME = "pass_gen.db";
    String PWD_TABLE_NAME = "password";
    String PRIMARY_KEY = "id";
    String NAME_COL = "name";
    String PWD_COL = "password";
    String TS_COL = "timestamp";
    int DB_VERSION = 1;
}
