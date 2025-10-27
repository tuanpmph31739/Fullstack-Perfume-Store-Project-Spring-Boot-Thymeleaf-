-- Chuyển về database master
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

-- =================================================================================
-- PHẦN 1: TẠO CẤU TRÚC BẢNG (Đã tối ưu)
-- =================================================================================
PRINT 'Bat dau tao cac bang...';

CREATE TABLE NguoiDung (
    Id BIGINT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE, 
    HoTen NVARCHAR(100),
    Email NVARCHAR(100) NOT NULL UNIQUE,
    MatKhau NVARCHAR(255) NOT NULL, 
    GioiTinh INT NULL, 
    NgaySinh DATE NULL,
    DiaChi NVARCHAR(255) NULL,
    Sdt NVARCHAR(20) NULL,
    VaiTro NVARCHAR(20) NOT NULL DEFAULT N'KHACHHANG', 
    TrangThai BIT NOT NULL DEFAULT 1,
    CONSTRAINT CHK_VaiTro CHECK (VaiTro IN (N'ADMIN', N'NHANVIEN', N'KHACHHANG'))
);
PRINT N'Bảng NguoiDung (hợp nhất) đã được tạo.';
GO

CREATE TABLE ThuongHieu (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100),
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Sử dụng DATETIME2
    NgaySua DATETIME2 DEFAULT GETDATE()  -- Sử dụng DATETIME2
);

CREATE TABLE XuatXu (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100),
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Sử dụng DATETIME2
    NgaySua DATETIME2 DEFAULT GETDATE()  -- Sử dụng DATETIME2
);

CREATE TABLE DungTich (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    SoMl INT,
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Sử dụng DATETIME2
    NgaySua DATETIME2 DEFAULT GETDATE()  -- Sử dụng DATETIME2
);

CREATE TABLE LoaiNuocHoa (
    Id INT PRIMARY KEY IDENTITY(1,1),
    TenLoai NVARCHAR(50) NOT NULL, 
    MoTa NVARCHAR(255),
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Sử dụng DATETIME2
    NgaySua DATETIME2 DEFAULT GETDATE()  -- Sử dụng DATETIME2
);

CREATE TABLE MuaThichHop (
    Id BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaMua NVARCHAR(20) UNIQUE,
    TenMua NVARCHAR(50),
    MoTa NVARCHAR(255),
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Sử dụng DATETIME2
    NgaySua DATETIME2 DEFAULT GETDATE()  -- Sử dụng DATETIME2
);

CREATE TABLE NongDo (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(10) UNIQUE,
    Ten NVARCHAR(100),
    MoTa NVARCHAR(255),
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Sử dụng DATETIME2
    NgaySua DATETIME2 DEFAULT GETDATE()  -- Sử dụng DATETIME2
);

CREATE TABLE NhomHuong (
    Id BIGINT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    TenNhomHuong NVARCHAR(100),
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Sử dụng DATETIME2
    NgaySua DATETIME2 DEFAULT GETDATE()  -- Sử dụng DATETIME2
);
PRINT N'Đã cập nhật các bảng thuộc tính với NgayTao/NgaySua (DATETIME2).';
GO

CREATE TABLE SanPham (
    Id INT PRIMARY KEY IDENTITY(1,1),
    TenNuocHoa NVARCHAR(255),
    IdThuongHieu INT FOREIGN KEY REFERENCES ThuongHieu(Id),
    IdXuatXu INT FOREIGN KEY REFERENCES XuatXu(Id),
    IdLoai INT FOREIGN KEY REFERENCES LoaiNuocHoa(Id),
    IdMuaThichHop BIGINT FOREIGN KEY REFERENCES MuaThichHop(Id),
    IdNhomHuong BIGINT FOREIGN KEY REFERENCES NhomHuong(Id),
    MoTa NVARCHAR(MAX)
);

CREATE TABLE SanPhamChiTiet (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdSanPham INT NOT NULL FOREIGN KEY REFERENCES SanPham(Id), 
    IdDungTich INT FOREIGN KEY REFERENCES DungTich(Id),
    IdNongDo INT FOREIGN KEY REFERENCES NongDo(Id), 
    MaSKU NVARCHAR(50) UNIQUE, 
    SoLuongTon INT,
    GiaNhap DECIMAL(20, 0),
    GiaBan DECIMAL(20, 0),
    HinhAnh NVARCHAR(255) NULL,
    TrangThai BIT DEFAULT 1
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
    IdKH BIGINT NOT NULL FOREIGN KEY REFERENCES NguoiDung(Id), 
    IdNV BIGINT NULL FOREIGN KEY REFERENCES NguoiDung(Id), 
    Ma NVARCHAR(20) UNIQUE,
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Sử dụng DATETIME2
    NgayThanhToan DATETIME2 NULL,       -- Sử dụng DATETIME2
    TenNguoiNhan NVARCHAR(100),
    DiaChi NVARCHAR(255),
    Sdt NVARCHAR(20),
    TinhTrang INT,
    IdGiamGia INT NULL, 
    PhuongThucThanhToan NVARCHAR(50) NULL, 
    NgayGiaoHang DATETIME2 NULL,        -- Sử dụng DATETIME2
    PhiShip DECIMAL(20, 0) DEFAULT 30000 
);
GO

ALTER TABLE HoaDon
ADD FOREIGN KEY (IdGiamGia) REFERENCES GiamGia(Id);
GO

-- ==========================================
-- BẢNG GIOHANG (Đã sửa kiểu dữ liệu ngày)
-- ==========================================
CREATE TABLE GioHang (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdKH BIGINT NOT NULL FOREIGN KEY REFERENCES NguoiDung(Id), 
    NgayTao DATETIME2 DEFAULT GETDATE(), -- ĐÃ SỬA: Kiểu DATETIME2
    NgaySua DATETIME2 DEFAULT GETDATE(), -- ĐÃ SỬA: Kiểu DATETIME2, bỏ DEFAULT logic sẽ tự cập nhật
    IdGiamGia INT NULL FOREIGN KEY REFERENCES GiamGia(Id) 
);
PRINT N'Bảng GioHang đã được cập nhật kiểu dữ liệu ngày.';
GO

-- Bảng phụ thuộc (Cấp 2 - Chi tiết)
CREATE TABLE HoaDonChiTiet (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdHoaDon INT NOT NULL FOREIGN KEY REFERENCES HoaDon(Id),
    IdSanPhamChiTiet INT NOT NULL FOREIGN KEY REFERENCES SanPhamChiTiet(Id),
    SoLuong INT NOT NULL CHECK (SoLuong > 0),
    DonGia DECIMAL(20, 0) NOT NULL CHECK (DonGia >= 0),
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    GhiChu NVARCHAR(255) NULL,
    TrangThai INT DEFAULT 1,
    IdGiamGia INT NULL FOREIGN KEY REFERENCES GiamGia(Id)
);

CREATE TABLE GioHangChiTiet (
    IdGioHang INT FOREIGN KEY REFERENCES GioHang(Id),
    IdSanPhamChiTiet INT FOREIGN KEY REFERENCES SanPhamChiTiet(Id), 
    SoLuong INT,
    PRIMARY KEY (IdGioHang, IdSanPhamChiTiet) 
);

PRINT 'Da tao xong tat ca cac bang.';
GO

-- =================================================================================
-- PHẦN 2: CHÈN DỮ LIỆU MẪU (Đã cập nhật Mùa Thích Hợp)
-- =================================================================================

USE PerfumeStore;
GO

PRINT 'Bat dau chen du lieu mau...';

-- CHÈN DỮ LIỆU NGUOIDUNG
INSERT INTO NguoiDung (Ma, HoTen, Email, MatKhau, GioiTinh, NgaySinh, DiaChi, Sdt, VaiTro, TrangThai)
VALUES
(N'ADMIN001', N'Nguyễn Thu An', N'admin@fperfume.com', N'$2a$10$...', 0, '1995-08-15', N'123 Đê La Thành, Hà Nội', '0987654321', N'ADMIN', 1),
(N'NV001', N'Trần Minh Bảo', N'nhanvien@fperfume.com', N'$2a$10$...', 1, '1998-04-20', N'456 Lê Lợi, TP.HCM', '0912345678', N'NHANVIEN', 1),
(N'KH001', N'Lê Thị Mai', N'mai.le@email.com', N'$2a$10$...', NULL, '1999-12-01', N'10 Lý Thường Kiệt, Hoàn Kiếm', '0369852147', N'KHACHHANG', 1),
(N'KH002', N'Vũ Hoàng Anh', N'anh.vu@email.com', N'$2a$10$...', NULL, '2001-07-22', N'50 Võ Văn Tần, Quận 3', '0321456987', N'KHACHHANG', 1);
GO 

-- Chèn các bảng thuộc tính sản phẩm
INSERT INTO ThuongHieu (Ma, Ten) VALUES ('TH01', N'Chanel'), ('TH02', N'Dior'), ('TH03', N'Gucci');
INSERT INTO XuatXu (Ma, Ten) VALUES ('XX01', N'Pháp'), ('XX02', N'Ý');
INSERT INTO DungTich (Ma, SoMl) VALUES ('DT50', 50), ('DT100', 100);
INSERT INTO LoaiNuocHoa (TenLoai, MoTa) VALUES (N'Nam', N'Hương nam tính'), (N'Nữ', N'Hương nữ tính'), (N'Unisex', N'Hương trung tính');

-- ==========================================
-- SỬA CHÈN DỮ LIỆU MuaThichHop
-- ==========================================
INSERT INTO MuaThichHop (MaMua, TenMua, MoTa) VALUES 
('NONG', N'Mùa Nóng', N'Phù hợp cho thời tiết ấm áp, nóng bức (Xuân, Hè)'), 
('LANH', N'Mùa Lạnh', N'Phù hợp cho thời tiết mát mẻ, se lạnh (Thu, Đông)');
PRINT N'Đã cập nhật dữ liệu mẫu cho MuaThichHop.';
GO

INSERT INTO NongDo (Ma, Ten, MoTa) VALUES ('EDP', N'Eau de Parfum', N'Nồng độ 15-20% tinh dầu'), ('EDT', N'Eau de Toilette', N'Nồng độ 5-15% tinh dầu');
INSERT INTO NhomHuong (Ma, TenNhomHuong) VALUES ('WOODY', N'Hương Gỗ (Woody)'), ('CITRUS', N'Hương Cam Chanh (Citrus)'), ('FLORAL', N'Hương Hoa Cỏ (Floral)');

-- ==========================================
-- SỬA CHÈN DỮ LIỆU SanPham (Cập nhật IdMuaThichHop)
-- ==========================================
PRINT 'Chen du lieu SanPham (Goc)...';
INSERT INTO SanPham (TenNuocHoa, IdThuongHieu, IdXuatXu, IdLoai, IdMuaThichHop, IdNhomHuong, MoTa)
VALUES
(
    N'Bleu de Chanel', 
    (SELECT Id FROM ThuongHieu WHERE Ma = 'TH01'), 
    (SELECT Id FROM XuatXu WHERE Ma = 'XX01'), 
    (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'), 
    (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'), -- Sửa: Mùa Lạnh (thay cho THU)
    (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'), 
    N'Mùi hương gỗ thơm nam tính, mạnh mẽ.'
),
(
    N'Sauvage Dior', 
    (SELECT Id FROM ThuongHieu WHERE Ma = 'TH02'), 
    (SELECT Id FROM XuatXu WHERE Ma = 'XX01'), 
    (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'), 
    (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'), -- Sửa: Mùa Nóng (thay cho HE)
    (SELECT Id FROM NhomHuong WHERE Ma = 'CITRUS'), 
    N'Hương thơm tươi mát, hoang dã và đầy lôi cuốn.'
),
(
    N'Gucci Bloom', 
    (SELECT Id FROM ThuongHieu WHERE Ma = 'TH03'), 
    (SELECT Id FROM XuatXu WHERE Ma = 'XX02'), 
    (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'), 
    (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'), -- Sửa: Mùa Nóng (thay cho XUAN)
    (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'), 
    N'Một vườn hoa trắng phong phú với hoa huệ, hoa nhài.'
);
GO 
PRINT N'Đã cập nhật IdMuaThichHop cho SanPham mẫu.';
GO

-- Chèn SanPhamChiTiet (Biến thể) - Giữ nguyên
PRINT 'Chen du lieu SanPhamChiTiet (Bien the)...';
INSERT INTO SanPhamChiTiet (IdSanPham, IdDungTich, IdNongDo, MaSKU, SoLuongTon, GiaNhap, GiaBan, HinhAnh, TrangThai)
VALUES
(1, (SELECT Id FROM DungTich WHERE Ma = 'DT100'), (SELECT Id FROM NongDo WHERE Ma = 'EDP'), 'CHA_BLEU_100_EDP', 50, 2500000, 3500000, 'bleu_de_chanel.jpg', 1),
(2, (SELECT Id FROM DungTich WHERE Ma = 'DT100'), (SELECT Id FROM NongDo WHERE Ma = 'EDT'), 'DIOR_SAUVAGE_100_EDT', 40, 2200000, 3200000, 'sauvage_dior.jpg', 1),
(3, (SELECT Id FROM DungTich WHERE Ma = 'DT50'), (SELECT Id FROM NongDo WHERE Ma = 'EDP'), 'GUCCI_BLOOM_50_EDP', 60, 1800000, 2800000, 'gucci_bloom.jpg', 1);
GO 

-- Chèn Hóa Đơn - Giữ nguyên
INSERT INTO HoaDon (IdKH, IdNV, Ma, NgayTao, NgayThanhToan, TenNguoiNhan, DiaChi, Sdt, TinhTrang) VALUES
((SELECT Id FROM NguoiDung WHERE Ma = 'KH001'), (SELECT Id FROM NguoiDung WHERE Ma = 'NV001'), 'HD001', '2024-05-10 10:00:00', '2024-05-10 10:05:00', N'Lê Thị Mai', N'10 Lý Thường Kiệt, Hoàn Kiếm', '0369852147', 1);

-- Chèn Giảm Giá - Giữ nguyên
INSERT INTO GiamGia (Ma, Ten, LoaiGiam, GiaTri, NgayBatDau, NgayKetThuc, TrangThai)
VALUES 
('GG10', N'Giảm 10% toàn bộ đơn hàng', 'PERCENT', 10, '2024-05-01', '2024-12-31', 1),
('GG100K', N'Giảm 100.000đ cho đơn từ 1 triệu', 'AMOUNT', 100000, '2024-06-01', '2024-12-31', 1);
GO

-- Chèn Hóa Đơn Chi Tiết - Giữ nguyên
INSERT INTO HoaDonChiTiet (IdHoaDon, IdSanPhamChiTiet, SoLuong, DonGia, GhiChu, IdGiamGia)
VALUES
((SELECT Id FROM HoaDon WHERE Ma = 'HD001'), 1, 1, 3500000, N'Hàng chính hãng - tặng hộp quà', (SELECT TOP 1 Id FROM GiamGia WHERE Ma = 'GG10')),
((SELECT Id FROM HoaDon WHERE Ma = 'HD001'), 3, 1, 2800000, N'Hàng mới về - không khuyến mãi', NULL);
GO

-- CHÈN DỮ LIỆU GIOHANG - Giữ nguyên
INSERT INTO GioHang (IdKH, NgayTao, NgaySua, IdGiamGia) VALUES
((SELECT Id FROM NguoiDung WHERE Ma = 'KH002'), '2024-05-12 09:30:00', '2024-05-12 09:30:00', NULL);
GO 

-- Chèn Giỏ Hàng Chi Tiết - Giữ nguyên
INSERT INTO GioHangChiTiet (IdGioHang, IdSanPhamChiTiet, SoLuong) VALUES
(1, 2, 1);

PRINT 'Da chen xong du lieu mau.';
GO

-- =================================================================================
-- PHẦN 3: CÁC CẬP NHẬT PHỤ 
-- =================================================================================
UPDATE HoaDon
SET IdGiamGia = (SELECT TOP 1 Id FROM GiamGia WHERE Ma = 'GG10')
WHERE Ma = 'HD001';
GO

-- =================================================================================
-- PHẦN 4: KIỂM TRA DỮ LIỆU
-- =================================================================================
PRINT 'Hoan tat script. Kiem tra du lieu:';
GO

SELECT * FROM NguoiDung; 
SELECT * FROM ThuongHieu;
SELECT * FROM XuatXu;
SELECT * FROM DungTich;
SELECT * FROM LoaiNuocHoa;
SELECT * FROM MuaThichHop;
SELECT * FROM NongDo;
SELECT * FROM NhomHuong;
SELECT * FROM SanPham;
SELECT * FROM SanPhamChiTiet;
SELECT * FROM GiamGia;
SELECT * FROM HoaDon; 
SELECT * FROM HoaDonChiTiet;
SELECT * FROM GioHang; 
SELECT * FROM GioHangChiTiet;
GO