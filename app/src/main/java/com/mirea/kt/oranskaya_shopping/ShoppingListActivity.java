package com.mirea.kt.oranskaya_shopping;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import java.util.ArrayList;

public class ShoppingListActivity extends Activity {

    private String listName;
    private ListView lvItems;
    private Button btnAddItem;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> itemList;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Получаем имя списка из Intent
        listName = getIntent().getStringExtra("listName");
        if (listName == null || listName.isEmpty()) {
            Toast.makeText(this, "Ошибка: имя списка не передано", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setTitle(listName);
        Log.d("ShoppingListActivity", "Открытие списка: " + listName);

        lvItems = findViewById(R.id.lvItems);
        btnAddItem = findViewById(R.id.btnAddItem);
        dbHelper = new DBHelper(this);

        itemList = getItems(listName);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, itemList);
        lvItems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvItems.setAdapter(adapter);

        // Отмечаем купленные товары
        for (int i = 0; i < itemList.size(); i++) {
            if (isBought(listName, itemList.get(i))) {
                lvItems.setItemChecked(i, true);
            }
        }

        // При клике меняем состояние "куплен / не куплен"
        lvItems.setOnItemClickListener((parent, view, position, id) -> {
            String item = itemList.get(position);
            boolean isChecked = lvItems.isItemChecked(position);
            setBought(listName, item, isChecked);
        });

        // Удаление товара по долгому нажатию
        lvItems.setOnItemLongClickListener((parent, view, position, id) -> {
            String item = itemList.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Удалить товар")
                    .setMessage("Удалить \"" + item + "\"?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        deleteItem(listName, item);
                        itemList.clear();
                        itemList.addAll(getItems(listName));
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });

        // Добавление нового товара
        btnAddItem.setOnClickListener(view -> {
            final EditText input = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("Новый товар")
                    .setView(input)
                    .setPositiveButton("Добавить", (dialog, which) -> {
                        String newItem = input.getText().toString().trim();
                        if (!newItem.isEmpty()) {
                            addItem(listName, newItem);
                            itemList.clear();
                            itemList.addAll(getItems(listName));
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    // Методы для работы с таблицей items

    private void addItem(String listName, String itemName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("list_name", listName);
        cv.put("name", itemName);
        cv.put("is_bought", 0);
        db.insert("items", null, cv);
        db.close();
    }

    private ArrayList<String> getItems(String listName) {
        ArrayList<String> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("items", new String[]{"name"}, "list_name=?", new String[]{listName}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                items.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            }
            cursor.close();
        }
        db.close();
        return items;
    }

    private void deleteItem(String listName, String itemName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("items", "list_name=? AND name=?", new String[]{listName, itemName});
        db.close();
    }

    private void setBought(String listName, String itemName, boolean bought) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_bought", bought ? 1 : 0);
        db.update("items", cv, "list_name=? AND name=?", new String[]{listName, itemName});
        db.close();
    }

    private boolean isBought(String listName, String itemName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("items", new String[]{"is_bought"}, "list_name=? AND name=?", new String[]{listName, itemName}, null, null, null);
        boolean result = false;
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndexOrThrow("is_bought")) == 1;
            cursor.close();
        }
        db.close();
        return result;
    }
}
