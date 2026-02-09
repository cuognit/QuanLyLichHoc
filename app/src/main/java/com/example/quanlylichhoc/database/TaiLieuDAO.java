package com.example.quanlylichhoc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlylichhoc.model.TaiLieu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TaiLieuDAO {
    private final DBHelper dbHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public TaiLieuDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long insertTaiLieu(TaiLieu taiLieu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenTaiLieu", taiLieu.getTenTaiLieu());
        values.put("LinkTaiLieu", taiLieu.getLinkTaiLieu());
        values.put("LoaiTaiLieu", taiLieu.getLoaiTaiLieu());
        values.put("KichThuoc", taiLieu.getKichThuoc());
        values.put("NgayCapNhat", dateFormat.format(taiLieu.getNgayCapNhat()));
        values.put("MON_HOC_MaMonHoc", taiLieu.getMaMonHoc());
        long result = db.insert("TAI_LIEU", null, values);
        db.close();
        return result;
    }

    public ArrayList<TaiLieu> getAllTaiLieu() {
        ArrayList<TaiLieu> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TAI_LIEU", null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    Date ngayCapNhat = dateFormat.parse(cursor.getString(5));
                    list.add(new TaiLieu(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            ngayCapNhat,
                            cursor.getInt(6)
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

    public int updateTaiLieu(TaiLieu taiLieu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenTaiLieu", taiLieu.getTenTaiLieu());
        values.put("LinkTaiLieu", taiLieu.getLinkTaiLieu());
        values.put("LoaiTaiLieu", taiLieu.getLoaiTaiLieu());
        values.put("KichThuoc", taiLieu.getKichThuoc());
        values.put("NgayCapNhat", dateFormat.format(taiLieu.getNgayCapNhat()));
        values.put("MON_HOC_MaMonHoc", taiLieu.getMaMonHoc());
        int result = db.update("TAI_LIEU", values, "MaTaiLieu=?", new String[]{String.valueOf(taiLieu.getMaTaiLieu())});
        db.close();
        return result;
    }

    public int deleteTaiLieu(int maTaiLieu) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("TAI_LIEU", "MaTaiLieu=?", new String[]{String.valueOf(maTaiLieu)});
        db.close();
        return result;
    }
}
