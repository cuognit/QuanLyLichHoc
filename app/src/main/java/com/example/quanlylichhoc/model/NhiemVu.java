package com.example.quanlylichhoc.model;

import java.util.Date;

public class NhiemVu {
    private int maNhiemVu;
    private String tenNhiemVu;
    private Date ngayPhaiHoanThanh;
    private int doUuTien;
    private String trangThai;
    private int maMonHoc;

    public NhiemVu(int maNhiemVu, String tenNhiemVu, Date ngayPhaiHoanThanh, int doUuTien, String trangThai, int maMonHoc) {
        this.maNhiemVu = maNhiemVu;
        this.tenNhiemVu = tenNhiemVu;
        this.ngayPhaiHoanThanh = ngayPhaiHoanThanh;
        this.doUuTien = doUuTien;
        this.trangThai = trangThai;
        this.maMonHoc = maMonHoc;
    }

    public int getMaNhiemVu() {
        return maNhiemVu;
    }

    public void setMaNhiemVu(int maNhiemVu) {
        this.maNhiemVu = maNhiemVu;
    }

    public String getTenNhiemVu() {
        return tenNhiemVu;
    }

    public void setTenNhiemVu(String tenNhiemVu) {
        this.tenNhiemVu = tenNhiemVu;
    }

    public Date getNgayPhaiHoanThanh() {
        return ngayPhaiHoanThanh;
    }

    public void setNgayPhaiHoanThanh(Date ngayPhaiHoanThanh) {
        this.ngayPhaiHoanThanh = ngayPhaiHoanThanh;
    }

    public int getDoUuTien() {
        return doUuTien;
    }

    public void setDoUuTien(int doUuTien) {
        this.doUuTien = doUuTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(int maMonHoc) {
        this.maMonHoc = maMonHoc;
    }
}
