package com.example.quanlylichhoc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlylichhoc.model.GhiChu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GhiChuDAO {
    private final DBHelper dbHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public GhiChuDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long insertGhiChu(GhiChu ghiChu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NoiDung", ghiChu.getNoiDung());
        values.put("NgayDangTai", dateFormat.format(ghiChu.getNgayDangTai()));
        values.put("MON_HOC_MaMonHoc", ghiChu.getMaMonHoc());
        long result = db.insert("GHI_CHU", null, values);
        db.close();
        return result;
    }

    public ArrayList<GhiChu> getAllGhiChu() {
        ArrayList<GhiChu> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM GHI_CHU", null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    Date ngayDangTai = dateFormat.parse(cursor.getString(2));
                    list.add(new GhiChu(
                            cursor.getInt(0),
                            cursor.getString(1),
                            ngayDangTai,
                            cursor.getInt(3)
                    ));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public int updateGhiChu(GhiChu ghiChu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NoiDung", ghiChu.getNoiDung());
        values.put("NgayDangTai", dateFormat.format(ghiChu.getNgayDangTai()));
        values.put("MON_HOC_MaMonHoc", ghiChu.getMaMonHoc());
        int result = db.update("GHI_CHU", values, "MaGhiChu=?", new String[]{String.valueOf(ghiChu.getMaGhiChu())});
        db.close();
        return result;
    }

    public int deleteGhiChu(int maGhiChu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("GHI_CHU", "MaGhiChu=?", new String[]{String.valueOf(maGhiChu)});
        db.close();
        return result;
    }
}
