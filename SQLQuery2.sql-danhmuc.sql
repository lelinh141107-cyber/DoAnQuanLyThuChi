USE QuanLyThuChi;
GO

-- 1. Them nguoi dung
INSERT INTO NguoiDung (HoTen, Email)
VALUES
(N'Nguyen Van A', 'a@gmail.com');

-- 2. Them danh muc
INSERT INTO DanhMuc (TenDanhMuc, Loai)
VALUES
(N'Luong', N'Thu'),
(N'Thuong', N'Thu'),
(N'An uong', N'Chi'),
(N'Di lai', N'Chi'),
(N'Mua sam', N'Chi'),
(N'Hoc tap', N'Chi');

-- 3. Them phuong thuc
INSERT INTO PhuongThuc (TenPhuongThuc, MoTa)
VALUES
(N'Tien mat', N'Thanh toan bang tien mat'),
(N'Chuyen khoan', N'Thanh toan qua ngan hang'),
(N'Vi dien tu', N'Thanh toan qua vi dien tu');

-- 4. Them giao dich
INSERT INTO GiaoDich
(MaNguoiDung, MaDanhMuc, MaPhuongThuc,
 SoTien, NgayGiaoDich, NoiDung)
VALUES
(1, 1, 1, 10000000, GETDATE(), N'Nhan luong'),
(1, 3, 1, 50000, GETDATE(), N'An sang'),
(1, 4, 2, 30000, GETDATE(), N'Tien xe'),
(1, 5, 3, 200000, GETDATE(), N'Mua do dung');
GO