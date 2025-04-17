package com.mirea.kt.oranskaya_shopping;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "shopping.db";
    // Повышение версии гарантирует вызов onUpgrade(), если база уже была создана ранее.
    private static final int DB_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы списков покупок
        db.execSQL("CREATE TABLE IF NOT EXISTS shopping_lists (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT)");
        // Создание таблицы товаров
        db.execSQL("CREATE TABLE IF NOT EXISTS items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "list_name TEXT, " +
                "name TEXT, " +
                "is_bought INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Если база обновляется с версии ниже 2, создаём таблицу items
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "list_name TEXT, " +
                    "name TEXT, " +
                    "is_bought INTEGER)");
        }
    }
}
