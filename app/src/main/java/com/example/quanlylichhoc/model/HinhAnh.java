package com.example.quanlylichhoc.model;

public class HinhAnh {
    private int maHinhAnh;
    private String hinhAnh;

    public HinhAnh(int maHinhAnh, String hinhAnh) {
        this.maHinhAnh = maHinhAnh;
        this.hinhAnh = hinhAnh;
    }

    public int getMaHinhAnh() {
        return maHinhAnh;
    }

    public void setMaHinhAnh(int maHinhAnh) {
        this.maHinhAnh = maHinhAnh;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
}
