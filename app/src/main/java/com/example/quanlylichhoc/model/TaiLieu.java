package com.example.quanlylichhoc.model;

import java.util.Date;

public class TaiLieu {
    private int maTaiLieu;
    private String tenTaiLieu;
    private String linkTaiLieu;
    private String loaiTaiLieu;
    private String kichThuoc;
    private Date ngayCapNhat;
    private int maMonHoc;

    public TaiLieu(int maTaiLieu, String tenTaiLieu, String linkTaiLieu, String loaiTaiLieu, String kichThuoc, Date ngayCapNhat, int maMonHoc) {
        this.maTaiLieu = maTaiLieu;
        this.tenTaiLieu = tenTaiLieu;
        this.linkTaiLieu = linkTaiLieu;
        this.loaiTaiLieu = loaiTaiLieu;
        this.kichThuoc = kichThuoc;
        this.ngayCapNhat = ngayCapNhat;
        this.maMonHoc = maMonHoc;
    }

    public int getMaTaiLieu() {
        return maTaiLieu;
    }

    public void setMaTaiLieu(int maTaiLieu) {
        this.maTaiLieu = maTaiLieu;
    }

    public String getTenTaiLieu() {
        return tenTaiLieu;
    }

    public void setTenTaiLieu(String tenTaiLieu) {
        this.tenTaiLieu = tenTaiLieu;
    }

    public String getLinkTaiLieu() {
        return linkTaiLieu;
    }

    public void setLinkTaiLieu(String linkTaiLieu) {
        this.linkTaiLieu = linkTaiLieu;
    }

    public String getLoaiTaiLieu() {
        return loaiTaiLieu;
    }

    public void setLoaiTaiLieu(String loaiTaiLieu) {
        this.loaiTaiLieu = loaiTaiLieu;
    }

    public String getKichThuoc() {
        return kichThuoc;
    }

    public void setKichThuoc(String kichThuoc) {
        this.kichThuoc = kichThuoc;
    }

    public Date getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Date ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public int getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(int maMonHoc) {
        this.maMonHoc = maMonHoc;
    }
}
