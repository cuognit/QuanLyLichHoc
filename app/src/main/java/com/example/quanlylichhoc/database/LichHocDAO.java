package com.example.quanlylichhoc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlylichhoc.model.LichHoc;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LichHocDAO {
    private final DBHelper dbHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public LichHocDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long insertLichHoc(LichHoc lichHoc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NgayHoc", dateFormat.format(lichHoc.getNgayHoc()));
        values.put("ThoiGianBatDau", timeFormat.format(lichHoc.getThoiGianBatDau()));
        values.put("ThoiGianKetThuc", timeFormat.format(lichHoc.getThoiGianKetThuc()));
        values.put("PhongHoc", lichHoc.getPhongHoc());
        values.put("GiangVien", lichHoc.getGiangVien());
        values.put("KieuHoc", lichHoc.getKieuHoc());
        values.put("MON_HOC_MaMonHoc", lichHoc.getMaMonHoc());
        long result = db.insert("LICH_HOC", null, values);
        db.close();
        return result;
    }

    public ArrayList<LichHoc> getAllLichHoc() {
        ArrayList<LichHoc> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LICH_HOC", null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    Date ngayHoc = dateFormat.parse(cursor.getString(1));
                    Time thoiGianBatDau = new Time(timeFormat.parse(cursor.getString(2)).getTime());
                    Time thoiGianKetThuc = new Time(timeFormat.parse(cursor.getString(3)).getTime());
                    list.add(new LichHoc(
                            cursor.getInt(0),
                            ngayHoc,
                            thoiGianBatDau,
                            thoiGianKetThuc,
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6),
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

    public int updateLichHoc(LichHoc lichHoc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NgayHoc", dateFormat.format(lichHoc.getNgayHoc()));
        values.put("ThoiGianBatDau", timeFormat.format(lichHoc.getThoiGianBatDau()));
        values.put("ThoiGianKetThuc", timeFormat.format(lichHoc.getThoiGianKetThuc()));
        values.put("PhongHoc", lichHoc.getPhongHoc());
        values.put("GiangVien", lichHoc.getGiangVien());
        values.put("KieuHoc", lichHoc.getKieuHoc());
        values.put("MON_HOC_MaMonHoc", lichHoc.getMaMonHoc());
        int result = db.update("LICH_HOC", values, "MaLichHoc=?", new String[]{String.valueOf(lichHoc.getMaLichHoc())});
        db.close();
        return result;
    }

    public int deleteLichHoc(int maLichHoc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("LICH_HOC", "MaLichHoc=?", new String[]{String.valueOf(maLichHoc)});
        db.close();
        return result;
    }
}
