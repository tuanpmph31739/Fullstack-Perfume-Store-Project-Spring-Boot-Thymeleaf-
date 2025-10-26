USE master;
GO

-- Xóa database cũ nếu có
IF DB_ID('PerfumeStore') IS NOT NULL
BEGIN
    ALTER DATABASE PerfumeStore SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PerfumeStore;
    PRINT 'Da xoa database PerfumeStore cu.';
END
GO

-- Tạo database mới
CREATE DATABASE PerfumeStore;
PRINT 'Da tao moi database PerfumeStore.';
GO

-- Sử dụng database
USE PerfumeStore;
GO

PRINT 'Bat dau tao cac bang...';

-- Bảng độc lập (Không phụ thuộc)
CREATE TABLE ChucVu (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(50)
);

CREATE TABLE CuaHang (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100),
    DiaChi NVARCHAR(255)
);

CREATE TABLE KhachHang (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100),
    NgaySinh DATE,
    Sdt NVARCHAR(20),
    DiaChi NVARCHAR(255),
    MatKhau NVARCHAR(100),
    Email NVARCHAR(100),       
    TrangThai BIT DEFAULT 1     
);

CREATE TABLE ThuongHieu (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100)
);

CREATE TABLE XuatXu (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100)
);

CREATE TABLE DungTich (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    SoMl INT
);

CREATE TABLE LoaiNuocHoa (
    Id INT PRIMARY KEY IDENTITY(1,1),
    TenLoai NVARCHAR(50) NOT NULL,  -- Nam / Nữ / Unisex
    MoTa NVARCHAR(255)
);

CREATE TABLE MuaThichHop (
    Id BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaMua NVARCHAR(20) UNIQUE,
    TenMua NVARCHAR(50),
    MoTa NVARCHAR(255)
);

-- Bảng phụ thuộc (Cấp 1)
CREATE TABLE NhanVien (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100),
    GioiTinh INT,
    NgaySinh DATE,
    DiaChi NVARCHAR(255),
    Sdt NVARCHAR(20),
    MatKhau NVARCHAR(100),
    IdCH INT FOREIGN KEY REFERENCES CuaHang(Id),
    IdCV INT FOREIGN KEY REFERENCES ChucVu(Id),
    TrangThai INT,
    Email NVARCHAR(100) NULL 
);

CREATE TABLE SanPham (
    Id INT PRIMARY KEY IDENTITY(1,1),
    TenNuocHoa NVARCHAR(255),
    IdThuongHieu INT FOREIGN KEY REFERENCES ThuongHieu(Id),
    IdXuatXu INT FOREIGN KEY REFERENCES XuatXu(Id),
    IdDungTich INT FOREIGN KEY REFERENCES DungTich(Id),
    SoLuongTon INT,
    GiaNhap DECIMAL(20, 0),
    GiaBan DECIMAL(20, 0),
    MoTa NVARCHAR(MAX),
    IdLoai INT FOREIGN KEY REFERENCES LoaiNuocHoa(Id),        
    TrangThai BIT DEFAULT 1,                                  
    HinhAnh NVARCHAR(255) NULL,                               
    IdMuaThichHop BIGINT FOREIGN KEY REFERENCES MuaThichHop(Id) 
);

CREATE TABLE GiamGia (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE NOT NULL,
    Ten NVARCHAR(100),
    LoaiGiam NVARCHAR(20),
    GiaTri DECIMAL(10,2),
    NgayBatDau DATE,
    NgayKetThuc DATE,
    TrangThai INT DEFAULT 1,
    IdSanPham INT NULL FOREIGN KEY REFERENCES SanPham(Id)
);

CREATE TABLE HoaDon (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdKH INT FOREIGN KEY REFERENCES KhachHang(Id),
    IdNV INT FOREIGN KEY REFERENCES NhanVien(Id),
    Ma NVARCHAR(20) UNIQUE,
    NgayTao DATE,
    NgayThanhToan DATE,
    TenNguoiNhan NVARCHAR(100),
    DiaChi NVARCHAR(255),
    Sdt NVARCHAR(20),
    TinhTrang INT,
    IdGiamGia INT NULL, -- Sẽ thêm FK sau khi bảng GiamGia được tạo
    PhuongThucThanhToan NVARCHAR(50) NULL, 
    NgayGiaoHang DATE NULL              
);

-- Thêm khóa ngoại cho HoaDon (sau khi GiamGia đã được tạo)
ALTER TABLE HoaDon
ADD FOREIGN KEY (IdGiamGia) REFERENCES GiamGia(Id);
GO

CREATE TABLE GioHang (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdKH INT FOREIGN KEY REFERENCES KhachHang(Id),
    IdNV INT FOREIGN KEY REFERENCES NhanVien(Id),
    Ma NVARCHAR(20) UNIQUE,
    NgayTao DATE,
    NgayThanhToan DATE,
    TenNguoiNhan NVARCHAR(100),
    DiaChi NVARCHAR(255),
    Sdt NVARCHAR(20),
    TinhTrang INT
);

-- Bảng phụ thuộc (Cấp 2 - Chi tiết)
CREATE TABLE HoaDonChiTiet (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdHoaDon INT NOT NULL FOREIGN KEY REFERENCES HoaDon(Id),
    IdSanPham INT NOT NULL FOREIGN KEY REFERENCES SanPham(Id),
    SoLuong INT NOT NULL CHECK (SoLuong > 0),
    DonGia DECIMAL(20, 0) NOT NULL CHECK (DonGia >= 0),
    ThanhTien AS (SoLuong * DonGia) PERSISTED, -- Tự động tính tổng tiền
    GhiChu NVARCHAR(255) NULL,
    TrangThai INT DEFAULT 1, -- 1: bình thường, 0: hủy, 2: đổi trả
    IdGiamGia INT NULL FOREIGN KEY REFERENCES GiamGia(Id)
);

CREATE TABLE GioHangChiTiet (
    IdGioHang INT FOREIGN KEY REFERENCES GioHang(Id),
    IdChiTietSP INT FOREIGN KEY REFERENCES SanPham(Id), 
    SoLuong INT,
    DonGia DECIMAL(20, 0),
    PRIMARY KEY (IdGioHang, IdChiTietSP)
);

PRINT 'Da tao xong tat ca cac bang.';
GO

-- PHẦN 2: CHÈN DỮ LIỆU MẪU 

USE PerfumeStore;
GO

PRINT 'Bat dau chen du lieu mau...';

-- 1. Chức Vụ
INSERT INTO ChucVu (Ma, Ten) VALUES
('CV01', N'Quản lý'),
('CV02', N'Nhân viên bán hàng');

-- 2. Cửa Hàng
INSERT INTO CuaHang (Ma, Ten, DiaChi) VALUES
('CHHN', N'PerfumeStore Hà Nội', N'74 Bà Triệu, Hoàn Kiếm'),
('CHHCM', N'PerfumeStore Sài Gòn', N'120 Nguyễn Trãi, Quận 1');

-- 3. Nhân Viên (Phụ thuộc CuaHang, ChucVu)
INSERT INTO NhanVien (Ma, Ten, GioiTinh, NgaySinh, DiaChi, Sdt, MatKhau, IdCH, IdCV, TrangThai) VALUES
('NV001', N'Nguyễn Thu An', 0, '1995-08-15', N'123 Đê La Thành, Đống Đa, Hà Nội', '0987654321', 'password123',
 (SELECT Id FROM CuaHang WHERE Ma = 'CHHN'), (SELECT Id FROM ChucVu WHERE Ma = 'CV01'), 1),
('NV002', N'Trần Minh Bảo', 1, '1998-04-20', N'456 Lê Lợi, Quận 1, TP.HCM', '0912345678', 'password456',
 (SELECT Id FROM CuaHang WHERE Ma = 'CHHCM'), (SELECT Id FROM ChucVu WHERE Ma = 'CV02'), 1);

-- 4. Khách Hàng
INSERT INTO KhachHang (Ma, Ten, NgaySinh, Sdt, DiaChi, MatKhau) VALUES
('KH001', N'Lê Thị Mai', '1999-12-01', '0369852147', N'10 Lý Thường Kiệt, Hoàn Kiếm', 'khachhang1'),
('KH002', N'Vũ Hoàng Anh', '2001-07-22', '0321456987', N'50 Võ Văn Tần, Quận 3',  'khachhang2');

-- 5. Thương Hiệu
INSERT INTO ThuongHieu (Ma, Ten) VALUES
('TH01', N'Chanel'),
('TH02', N'Dior'),
('TH03', N'Gucci');

-- 6. Xuất Xứ
INSERT INTO XuatXu (Ma, Ten) VALUES
('XX01', N'Pháp'),
('XX02', N'Ý');

-- 7. Dung Tích
INSERT INTO DungTich (Ma, SoMl) VALUES
('DT50', 50),
('DT100', 100);

-- 8. Loại Nước Hoa 
INSERT INTO LoaiNuocHoa (TenLoai, MoTa) VALUES
(N'Nam', N'Hương mạnh mẽ, trầm ấm và cá tính dành cho nam giới'),
(N'Nữ', N'Hương ngọt ngào, quyến rũ và nhẹ nhàng dành cho nữ giới'),
(N'Unisex', N'Hương trung tính phù hợp cho cả nam và nữ');

-- 9. Mùa Thích Hợp 
INSERT INTO MuaThichHop (MaMua, TenMua, MoTa) VALUES
('XUAN', N'Mùa Xuân', N'Hương hoa cỏ, nhẹ nhàng, tươi mới – phù hợp với tiết trời dịu mát.'),
('HE', N'Mùa Hè', N'Hương cam chanh, biển, thanh mát – giúp sảng khoái trong thời tiết nóng bức.'),
('THU', N'Mùa Thu', N'Hương gỗ, trầm, ấm áp – mang lại cảm giác thư giãn, tinh tế.'),
('DONG', N'Mùa Đông', N'Hương ngọt, cay nồng và sâu lắng – tạo cảm giác ấm áp trong không khí lạnh.');

-- 10. Sản Phẩm 
INSERT INTO SanPham (
    TenNuocHoa, IdLoai, IdThuongHieu, IdXuatXu, IdDungTich, 
    IdMuaThichHop, 
    SoLuongTon, GiaNhap, GiaBan, MoTa
) 
VALUES
(
    N'Bleu de Chanel',
    (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
    (SELECT Id FROM ThuongHieu WHERE Ma = 'TH01'),
    (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
    (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
    (SELECT Id FROM MuaThichHop WHERE MaMua = 'THU'), -- <-- Gán Mùa Thu
    50, 2500000, 3500000,
    N'Mùi hương gỗ thơm nam tính, mạnh mẽ.'
),
(
    N'Sauvage Dior',
    (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
    (SELECT Id FROM ThuongHieu WHERE Ma = 'TH02'),
    (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
    (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
    (SELECT Id FROM MuaThichHop WHERE MaMua = 'HE'), -- <-- Gán Mùa Hè
    40, 2200000, 3200000,
    N'Hương thơm tươi mát, hoang dã và đầy lôi cuốn.'
),
(
    N'Gucci Bloom',
    (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'),
    (SELECT Id FROM ThuongHieu WHERE Ma = 'TH03'),
    (SELECT Id FROM XuatXu WHERE Ma = 'XX02'),
    (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
    (SELECT Id FROM MuaThichHop WHERE MaMua = 'XUAN'), -- <-- Gán Mùa Xuân
    60, 1800000, 2800000,
    N'Một vườn hoa trắng phong phú với hoa huệ, hoa nhài và kim ngân.'
);
GO

-- 11. Hóa Đơn (Phụ thuộc KhachHang, NhanVien)
INSERT INTO HoaDon (IdKH, IdNV, Ma, NgayTao, NgayThanhToan, TenNguoiNhan, DiaChi, Sdt, TinhTrang) VALUES
((SELECT Id FROM KhachHang WHERE Ma = 'KH001'), (SELECT Id FROM NhanVien WHERE Ma = 'NV001'), 'HD001', '2024-05-10', '2024-05-10', N'Lê Thị Mai', N'10 Lý Thường Kiệt, Hoàn Kiếm', '0369852147', 1);

-- 12. Giảm Giá
INSERT INTO GiamGia (Ma, Ten, LoaiGiam, GiaTri, NgayBatDau, NgayKetThuc, TrangThai)
VALUES 
('GG10', N'Giảm 10% toàn bộ đơn hàng', 'PERCENT', 10, '2024-05-01', '2024-12-31', 1),
('GG100K', N'Giảm 100.000đ cho đơn từ 1 triệu', 'AMOUNT', 100000, '2024-06-01', '2024-12-31', 1);
GO

-- 13. Hóa Đơn Chi Tiết (Phụ thuộc HoaDon, SanPham, GiamGia)
INSERT INTO HoaDonChiTiet (IdHoaDon, IdSanPham, SoLuong, DonGia, GhiChu, IdGiamGia)
VALUES
((SELECT Id FROM HoaDon WHERE Ma = 'HD001'),
 (SELECT Id FROM SanPham WHERE TenNuocHoa = N'Bleu de Chanel'),
 1, 3500000, N'Hàng chính hãng - tặng hộp quà', 
 (SELECT TOP 1 Id FROM GiamGia WHERE Ma = 'GG10')),

((SELECT Id FROM HoaDon WHERE Ma = 'HD001'),
 (SELECT Id FROM SanPham WHERE TenNuocHoa = N'Gucci Bloom'),
 1, 2800000, N'Hàng mới về - không khuyến mãi', 
 NULL);
GO

-- 14. Giỏ Hàng (Phụ thuộc KhachHang)
INSERT INTO GioHang (IdKH, IdNV, Ma, NgayTao, NgayThanhToan, TenNguoiNhan, DiaChi, Sdt, TinhTrang) VALUES
((SELECT Id FROM KhachHang WHERE Ma = 'KH002'), NULL, 'GH001', '2024-05-12', NULL, N'Vũ Hoàng Anh', N'50 Võ Văn Tần, Quận 3', '0321456987', 0);

-- 15. Giỏ Hàng Chi Tiết (Phụ thuộc GioHang, SanPham)
INSERT INTO GioHangChiTiet (IdGioHang, IdChiTietSP, SoLuong, DonGia) VALUES
((SELECT Id FROM GioHang WHERE Ma = 'GH001'), (SELECT Id FROM SanPham WHERE TenNuocHoa = N'Sauvage Dior' AND IdDungTich = (SELECT Id FROM DungTich WHERE Ma = 'DT100')), 1, 3200000);

PRINT 'Da chen xong du lieu mau.';
GO

-- PHẦN 3: CÁC CẬP NHẬT PHỤ 

-- Cập nhật hóa đơn HD001 để áp dụng mã giảm giá
UPDATE HoaDon
SET IdGiamGia = (SELECT TOP 1 Id FROM GiamGia WHERE Ma = 'GG10')
WHERE Ma = 'HD001';
GO

-- PHẦN 4: KIỂM TRA DỮ LIỆU

PRINT 'Hoan tat script. Kiem tra du lieu:';
GO

SELECT * FROM CuaHang;
SELECT * FROM ChucVu;
SELECT * FROM DungTich;
SELECT * FROM GioHang;
SELECT * FROM GioHangChiTiet;
SELECT * FROM HoaDon;
SELECT * FROM HoaDonChiTiet;
SELECT * FROM KhachHang;
SELECT * FROM NhanVien;
SELECT * FROM SanPham;
SELECT * FROM ThuongHieu;
SELECT * FROM XuatXu;
SELECT * FROM LoaiNuocHoa;
SELECT * FROM MuaThichHop;
SELECT * FROM GiamGia;
GO