package com.example.quanlylichhoc.model;

import java.util.Date;

public class GhiChu {
    private int maGhiChu;
    private String noiDung;
    private Date ngayDangTai;
    private int maMonHoc;

    public GhiChu(int maGhiChu, String noiDung, Date ngayDangTai, int maMonHoc) {
        this.maGhiChu = maGhiChu;
        this.noiDung = noiDung;
        this.ngayDangTai = ngayDangTai;
        this.maMonHoc = maMonHoc;
    }

    public int getMaGhiChu() {
        return maGhiChu;
    }

    public void setMaGhiChu(int maGhiChu) {
        this.maGhiChu = maGhiChu;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public Date getNgayDangTai() {
        return ngayDangTai;
    }

    public void setNgayDangTai(Date ngayDangTai) {
        this.ngayDangTai = ngayDangTai;
    }

    public int getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(int maMonHoc) {
        this.maMonHoc = maMonHoc;
    }
}
