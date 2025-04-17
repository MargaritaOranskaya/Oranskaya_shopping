package com.mirea.kt.oranskaya_shopping;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private ListView lvShoppingLists;
    private Button btnAddList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> shoppingLists;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvShoppingLists = findViewById(R.id.lvShoppingLists);
        btnAddList = findViewById(R.id.btnAddList);
        dbHelper = new DBHelper(this);

        shoppingLists = getAllLists();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shoppingLists);
        lvShoppingLists.setAdapter(adapter);

        btnAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddListDialog();
            }
        });

        lvShoppingLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String listName = shoppingLists.get(position);
                showListOptionsDialog(listName, position);
            }
        });
    }

    private void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Новый список покупок");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newListName = input.getText().toString().trim();
                if (!newListName.isEmpty()) {
                    addList(newListName);
                    shoppingLists.clear();
                    shoppingLists.addAll(getAllLists());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Название не может быть пустым", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showListOptionsDialog(final String listName, final int position) {
        final String[] options = {"Открыть", "Редактировать", "Удалить", "Поделиться"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите действие для: " + listName);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Запуск активности для редактирования списка покупок
                    Intent intent = new Intent(MainActivity.this, ShoppingListActivity.class);
                    intent.putExtra("listName", listName);
                    startActivity(intent);
                } else if (which == 1) {
                    showEditListDialog(listName, position);
                } else if (which == 2) {
                    deleteList(listName);
                    shoppingLists.clear();
                    shoppingLists.addAll(getAllLists());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Список удалён", Toast.LENGTH_SHORT).show();
                } else if (which == 3) {
                    shareList(listName);
                }
            }
        });
        builder.show();
    }

    private void showEditListDialog(final String oldName, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать название");

        final EditText input = new EditText(this);
        input.setText(oldName);
        builder.setView(input);

        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    updateList(oldName, newName);
                    shoppingLists.clear();
                    shoppingLists.addAll(getAllLists());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Список обновлён", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Название не может быть пустым", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void shareList(String listName) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Список покупок: " + listName);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "Поделиться списком"));
    }

    // Методы для работы с базой данных для таблицы shopping_lists

    private void addList(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db.insert("shopping_lists", null, cv);
        db.close();
    }

    private ArrayList<String> getAllLists() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("shopping_lists", new String[]{"name"}, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    private void deleteList(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("shopping_lists", "name=?", new String[]{name});
        db.close();
    }

    private void updateList(String oldName, String newName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", newName);
        db.update("shopping_lists", cv, "name=?", new String[]{oldName});
        db.close();
    }
}
