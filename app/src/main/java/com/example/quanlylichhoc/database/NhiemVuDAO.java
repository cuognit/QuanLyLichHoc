package com.example.quanlylichhoc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlylichhoc.model.NhiemVu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NhiemVuDAO {
    private final DBHelper dbHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public NhiemVuDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long insertNhiemVu(NhiemVu nhiemVu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenNhiemVu", nhiemVu.getTenNhiemVu());
        values.put("NgayPhaiHoanThanh", dateFormat.format(nhiemVu.getNgayPhaiHoanThanh()));
        values.put("DoUuTien", nhiemVu.getDoUuTien());
        values.put("TrangThai", nhiemVu.getTrangThai());
        values.put("MON_HOC_MaMonHoc", nhiemVu.getMaMonHoc());
        long result = db.insert("NHIEM_VU", null, values);
        db.close();
        return result;
    }

    public ArrayList<NhiemVu> getAllNhiemVu() {
        ArrayList<NhiemVu> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM NHIEM_VU", null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    Date ngayPhaiHoanThanh = dateFormat.parse(cursor.getString(2));
                    list.add(new NhiemVu(
                            cursor.getInt(0),
                            cursor.getString(1),
                            ngayPhaiHoanThanh,
                            cursor.getInt(3),
                            cursor.getString(4),
                            cursor.getInt(5)
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

    public int updateNhiemVu(NhiemVu nhiemVu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenNhiemVu", nhiemVu.getTenNhiemVu());
        values.put("NgayPhaiHoanThanh", dateFormat.format(nhiemVu.getNgayPhaiHoanThanh()));
        values.put("DoUuTien", nhiemVu.getDoUuTien());
        values.put("TrangThai", nhiemVu.getTrangThai());
        values.put("MON_HOC_MaMonHoc", nhiemVu.getMaMonHoc());
        int result = db.update("NHIEM_VU", values, "MaNhiemVu=?", new String[]{String.valueOf(nhiemVu.getMaNhiemVu())});
        db.close();
        return result;
    }

    public int deleteNhiemVu(int maNhiemVu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("NHIEM_VU", "MaNhiemVu=?", new String[]{String.valueOf(maNhiemVu)});
        db.close();
        return result;
    }
}
