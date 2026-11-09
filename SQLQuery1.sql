
CREATE DATABASE QuanLyThuChi;
GO

USE QuanLyThuChi;
GO

-- BANG 1: NGUOI DUNG
CREATE TABLE NguoiDung (
    MaNguoiDung INT IDENTITY(1,1) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE
);
GO

-- BANG 2: DANH MUC THU CHI
CREATE TABLE DanhMuc (
    MaDanhMuc INT IDENTITY(1,1) PRIMARY KEY,
    TenDanhMuc NVARCHAR(100) NOT NULL,
    Loai NVARCHAR(10) NOT NULL,
    CONSTRAINT CK_DanhMuc_Loai
        CHECK (Loai IN (N'Thu', N'Chi'))
);
GO

-- BANG 3: PHUONG THUC THANH TOAN
CREATE TABLE PhuongThuc (
    MaPhuongThuc INT IDENTITY(1,1) PRIMARY KEY,
    TenPhuongThuc NVARCHAR(50) NOT NULL UNIQUE,
    MoTa NVARCHAR(200)
);
GO

-- BANG 4: GIAO DICH
CREATE TABLE GiaoDich (
    MaGiaoDich INT IDENTITY(1,1) PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    MaDanhMuc INT NOT NULL,
    MaPhuongThuc INT NOT NULL,
    SoTien DECIMAL(18,2) NOT NULL,
    NgayGiaoDich DATE NOT NULL,
    NoiDung NVARCHAR(200) NOT NULL,

    CONSTRAINT CK_GiaoDich_SoTien
        CHECK (SoTien > 0),

    CONSTRAINT FK_GiaoDich_NguoiDung
        FOREIGN KEY (MaNguoiDung)
        REFERENCES NguoiDung(MaNguoiDung),

    CONSTRAINT FK_GiaoDich_DanhMuc
        FOREIGN KEY (MaDanhMuc)
        REFERENCES DanhMuc(MaDanhMuc),

    CONSTRAINT FK_GiaoDich_PhuongThuc
        FOREIGN KEY (MaPhuongThuc)
        REFERENCES PhuongThuc(MaPhuongThuc)
);
GO
