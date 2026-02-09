package com.example.quanlylichhoc.model;

import java.sql.Time;
import java.util.Date;

public class LichHoc {
    private int maLichHoc;
    private Date ngayHoc;
    private Time thoiGianBatDau;
    private Time thoiGianKetThuc;
    private String phongHoc;
    private String giangVien;
    private String kieuHoc;
    private int maMonHoc;

    public LichHoc(int maLichHoc, Date ngayHoc, Time thoiGianBatDau, Time thoiGianKetThuc, String phongHoc, String giangVien, String kieuHoc, int maMonHoc) {
        this.maLichHoc = maLichHoc;
        this.ngayHoc = ngayHoc;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.phongHoc = phongHoc;
        this.giangVien = giangVien;
        this.kieuHoc = kieuHoc;
        this.maMonHoc = maMonHoc;
    }

    public int getMaLichHoc() {
        return maLichHoc;
    }

    public void setMaLichHoc(int maLichHoc) {
        this.maLichHoc = maLichHoc;
    }

    public Date getNgayHoc() {
        return ngayHoc;
    }

    public void setNgayHoc(Date ngayHoc) {
        this.ngayHoc = ngayHoc;
    }

    public Time getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Time thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public Time getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(Time thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public String getPhongHoc() {
        return phongHoc;
    }

    public void setPhongHoc(String phongHoc) {
        this.phongHoc = phongHoc;
    }

    public String getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(String giangVien) {
        this.giangVien = giangVien;
    }

    public String getKieuHoc() {
        return kieuHoc;
    }

    public void setKieuHoc(String kieuHoc) {
        this.kieuHoc = kieuHoc;
    }

    public int getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(int maMonHoc) {
        this.maMonHoc = maMonHoc;
    }
}
