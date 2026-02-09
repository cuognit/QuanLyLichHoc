package com.example.quanlylichhoc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlylichhoc.model.HinhAnh;

import java.util.ArrayList;

public class HinhAnhDAO {
    private final DBHelper dbHelper;

    public HinhAnhDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long insertHinhAnh(HinhAnh hinhAnh) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("HinhAnh", hinhAnh.getHinhAnh());
        long result = db.insert("HINH_ANH", null, values);
        db.close();
        return result;
    }

    public ArrayList<HinhAnh> getAllHinhAnh() {
        ArrayList<HinhAnh> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM HINH_ANH", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new HinhAnh(
                        cursor.getInt(0),
                        cursor.getString(1)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public int updateHinhAnh(HinhAnh hinhAnh) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("HinhAnh", hinhAnh.getHinhAnh());
        int result = db.update("HINH_ANH", values, "MaHinhAnh=?", new String[]{String.valueOf(hinhAnh.getMaHinhAnh())});
        db.close();
        return result;
    }

    public int deleteHinhAnh(int maHinhAnh) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("HINH_ANH", "MaHinhAnh=?", new String[]{String.valueOf(maHinhAnh)});
        db.close();
        return result;
    }
}
