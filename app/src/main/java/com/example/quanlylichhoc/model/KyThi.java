package com.example.quanlylichhoc.model;

import java.util.Date;

public class KyThi {
    private int maKyThi;
    private String tenKyThi;
    private Date ngayGioThi;
    private String phongThi;
    private String soBaoDanh;
    private String trangThai;
    private float diemSo;
    private int maMonHoc;

    public KyThi(int maKyThi, String tenKyThi, Date ngayGioThi, String phongThi, String soBaoDanh, String trangThai, float diemSo, int maMonHoc) {
        this.maKyThi = maKyThi;
        this.tenKyThi = tenKyThi;
        this.ngayGioThi = ngayGioThi;
        this.phongThi = phongThi;
        this.soBaoDanh = soBaoDanh;
        this.trangThai = trangThai;
        this.diemSo = diemSo;
        this.maMonHoc = maMonHoc;
    }

    public int getMaKyThi() {
        return maKyThi;
    }

    public void setMaKyThi(int maKyThi) {
        this.maKyThi = maKyThi;
    }

    public String getTenKyThi() {
        return tenKyThi;
    }

    public void setTenKyThi(String tenKyThi) {
        this.tenKyThi = tenKyThi;
    }

    public Date getNgayGioThi() {
        return ngayGioThi;
    }

    public void setNgayGioThi(Date ngayGioThi) {
        this.ngayGioThi = ngayGioThi;
    }

    public String getPhongThi() {
        return phongThi;
    }

    public void setPhongThi(String phongThi) {
        this.phongThi = phongThi;
    }

    public String getSoBaoDanh() {
        return soBaoDanh;
    }

    public void setSoBaoDanh(String soBaoDanh) {
        this.soBaoDanh = soBaoDanh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public float getDiemSo() {
        return diemSo;
    }

    public void setDiemSo(float diemSo) {
        this.diemSo = diemSo;
    }

    public int getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(int maMonHoc) {
        this.maMonHoc = maMonHoc;
    }
}
