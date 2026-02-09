package com.example.quanlylichhoc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlylichhoc.model.KyThi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class KyThiDAO {
    private final DBHelper dbHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public KyThiDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long insertKyThi(KyThi kyThi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenKyThi", kyThi.getTenKyThi());
        values.put("NgayGioThi", dateFormat.format(kyThi.getNgayGioThi()));
        values.put("PhongThi", kyThi.getPhongThi());
        values.put("SoBaoDanh", kyThi.getSoBaoDanh());
        values.put("TrangThai", kyThi.getTrangThai());
        values.put("DiemSo", kyThi.getDiemSo());
        values.put("MON_HOC_MaMonHoc", kyThi.getMaMonHoc());
        long result = db.insert("KY_THI", null, values);
        db.close();
        return result;
    }

    public ArrayList<KyThi> getAllKyThi() {
        ArrayList<KyThi> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM KY_THI", null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    Date ngayGioThi = dateFormat.parse(cursor.getString(2));
                    list.add(new KyThi(
                            cursor.getInt(0),
                            cursor.getString(1),
                            ngayGioThi,
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getFloat(6),
                            cursor.getInt(7)
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

    public int updateKyThi(KyThi kyThi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenKyThi", kyThi.getTenKyThi());
        values.put("NgayGioThi", dateFormat.format(kyThi.getNgayGioThi()));
        values.put("PhongThi", kyThi.getPhongThi());
        values.put("SoBaoDanh", kyThi.getSoBaoDanh());
        values.put("TrangThai", kyThi.getTrangThai());
        values.put("DiemSo", kyThi.getDiemSo());
        values.put("MON_HOC_MaMonHoc", kyThi.getMaMonHoc());
        int result = db.update("KY_THI", values, "MaKyThi=?", new String[]{String.valueOf(kyThi.getMaKyThi())});
        db.close();
        return result;
    }

    public int deleteKyThi(int maKyThi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("KY_THI", "MaKyThi=?", new String[]{String.valueOf(maKyThi)});
        db.close();
        return result;
    }
}
