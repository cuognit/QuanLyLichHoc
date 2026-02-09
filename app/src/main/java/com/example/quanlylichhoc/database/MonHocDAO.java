package com.example.quanlylichhoc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlylichhoc.model.MonHoc;

import java.util.ArrayList;

public class MonHocDAO {
    private final DBHelper dbHelper;

    public MonHocDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long insertMonHoc(MonHoc monHoc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenMonHoc", monHoc.getTenMonHoc());
        values.put("SoTinChi", monHoc.getSoTinChi());
        values.put("Khoa", monHoc.getKhoa());
        values.put("MaMau", monHoc.getMaMau());
        long result = db.insert("MON_HOC", null, values);
        db.close();
        return result;
    }

    public ArrayList<MonHoc> getAllMonHoc() {
        ArrayList<MonHoc> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MON_HOC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new MonHoc(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public int updateMonHoc(MonHoc monHoc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenMonHoc", monHoc.getTenMonHoc());
        values.put("SoTinChi", monHoc.getSoTinChi());
        values.put("Khoa", monHoc.getKhoa());
        values.put("MaMau", monHoc.getMaMau());
        int result = db.update("MON_HOC", values, "MaMonHoc=?", new String[]{String.valueOf(monHoc.getMaMonHoc())});
        db.close();
        return result;
    }

    public int deleteMonHoc(int maMonHoc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("MON_HOC", "MaMonHoc=?", new String[]{String.valueOf(maMonHoc)});
        db.close();
        return result;
    }
}
