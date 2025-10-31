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
    SoLuong INT NOT NULL DEFAULT 0,
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

ALTER TABLE SanPham ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE SanPhamChiTiet ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE HoaDon ADD NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE HoaDonChiTiet ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();


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
INSERT INTO ThuongHieu (Ma, Ten) VALUES 
('TH01', N'Chanel'), 
('TH02', N'Dior'), 
('TH03', N'Yves Saint Laurent'), 
('TH04', N'Versace'), 
('TH05', N'Giorgio Armani'), 
('TH06', N'Dolce & Gabbana'), 
('TH07', N'Gucci'), 
('TH08', N'Montblanc'),
('TH09', N'Jean Paul Gaultier'), 
('TH10', N'Creed'), 
('TH11', N'Maison Francis Kurkdjian (MFK)'), 
('TH12', N'Le Labo'), 
('TH13', N'Tom Ford');

INSERT INTO XuatXu (Ma, Ten) VALUES 
('XX01', N'Pháp'), 
('XX02', N'Ý'),
('XX03', N'Anh'),
('XX04', N'Mỹ'),
('XX05', N'Đức');

INSERT INTO DungTich (Ma, SoMl) VALUES 
('DT30', 30), 
('DT50', 50), 
('DT100', 100);
INSERT INTO LoaiNuocHoa (TenLoai, MoTa) VALUES (N'Nam', N'Hương nam tính'), (N'Nữ', N'Hương nữ tính'), (N'Unisex', N'Hương trung tính');

-- ==========================================
-- SỬA CHÈN DỮ LIỆU MuaThichHop
-- ==========================================
INSERT INTO MuaThichHop (MaMua, TenMua, MoTa) VALUES 
('NONG', N'Mùa Nóng', N'Phù hợp cho thời tiết ấm áp, nóng bức (Xuân, Hè)'), 
('LANH', N'Mùa Lạnh', N'Phù hợp cho thời tiết mát mẻ, se lạnh (Thu, Đông)');
PRINT N'Đã cập nhật dữ liệu mẫu cho MuaThichHop.';
GO

INSERT INTO NongDo (Ma, Ten, MoTa) VALUES 
('EDC', N'Eau de Cologne', N'Nồng độ 3-5% tinh dầu'),
('EDT', N'Eau de Toilette', N'Nồng độ 5-15% tinh dầu'),
('EDP', N'Eau de Parfum', N'Nồng độ 15-20% tinh dầu'),
('Parfum', N'Parfum', N'Nồng độ 20-40% tinh dầu');

INSERT INTO NhomHuong (Ma, TenNhomHuong) VALUES 
('WOODY', N'Hương Gỗ (Woody)'), 
('CITRUS', N'Hương Cam Chanh (Citrus)'), 
('AROMATIC', N'Hương Thảo Mộc (Aromatic)'), 
('AQUATIC', N'Hương Biển (Aquatic)'), 
('LEATHER', N'Hương Da Thuộc (Leather)'), 
('FRUITY', N'Hương Trái Cây (Fruity)'), 
('FLORAL', N'Hương Hoa Cỏ (Floral)');

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
),
(
	N'J’adore Dior', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH02'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX01'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'),
	 (SELECT Id FROM NhomHuong WHERE Ma='FLORAL'),
	 N'Hương hoa trắng sang trọng, biểu tượng của sự nữ tính hiện đại.'),

	(N'Coco Mademoiselle', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH01'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX01'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'),
	 (SELECT Id FROM NhomHuong WHERE Ma='CITRUS'),
	 N'Hương hoa cam, hoắc hương và xạ hương thanh lịch.'),

	(N'Black Opium', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH03'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX01'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'),
	 (SELECT Id FROM NhomHuong WHERE Ma='AROMATIC'),
	 N'Hương cà phê, vani và hoa trắng gợi cảm.'),

	(N'Versace Eros', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH04'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX02'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'),
	 (SELECT Id FROM NhomHuong WHERE Ma='WOODY'),
	 N'Hương bạc hà, táo xanh và gỗ tuyết tùng mạnh mẽ.'),

	(N'Acqua di Gio', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH05'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX02'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'),
	 (SELECT Id FROM NhomHuong WHERE Ma='AQUATIC'),
	 N'Hương biển mát lạnh, cổ điển, dễ dùng.'),

	(N'Light Blue', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH06'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX02'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'),
	 (SELECT Id FROM NhomHuong WHERE Ma='CITRUS'),
	 N'Hương chanh tươi mát, thanh lịch và quyến rũ.'),

	(N'Gucci Guilty Pour Homme', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH07'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX02'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'),
	 (SELECT Id FROM NhomHuong WHERE Ma='AROMATIC'),
	 N'Hương lavender, cam bergamot và hoắc hương nam tính.'),

	(N'Montblanc Explorer', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH08'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX05'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'),
	 (SELECT Id FROM NhomHuong WHERE Ma='WOODY'),
	 N'Hương gỗ và da thuộc sang trọng, lấy cảm hứng từ Creed Aventus.'),

	(N'Black Orchid', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH11'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX04'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Unisex'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'),
	 (SELECT Id FROM NhomHuong WHERE Ma='FLORAL'),
	 N'Hương lan đen, socola và gỗ trầm hương đầy mê hoặc.'),

	(N'Creed Aventus', 
	 (SELECT Id FROM ThuongHieu WHERE Ma='TH10'),
	 (SELECT Id FROM XuatXu WHERE Ma='XX03'),
	 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'),
	 (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'),
	 (SELECT Id FROM NhomHuong WHERE Ma='FRUITY'),
	 N'Hương dứa, xạ hương và gỗ sồi – biểu tượng của sự thành công.'
);

GO 
PRINT N'Đã cập nhật IdMuaThichHop cho SanPham mẫu.';
GO

-- Chèn SanPhamChiTiet (Biến thể) - Giữ nguyên
PRINT 'Chen du lieu SanPhamChiTiet (Bien the)...';
INSERT INTO SanPhamChiTiet (IdSanPham, IdDungTich, IdNongDo, MaSKU, SoLuongTon, GiaNhap, GiaBan, HinhAnh, TrangThai)
VALUES
(1, (SELECT Id FROM DungTich WHERE Ma = 'DT100'), (SELECT Id FROM NongDo WHERE Ma = 'EDP'), 'CHA_BLEU_100_EDP', 50, 2500000, 3500000, 'e6fd0ab6-5459-438e-9c8f-039ecb0feba9_bleu de chanel.png', 1),
(2, (SELECT Id FROM DungTich WHERE Ma = 'DT100'), (SELECT Id FROM NongDo WHERE Ma = 'EDT'), 'DIOR_SAUVAGE_100_EDT', 40, 2200000, 3200000, 'ff47ec91-d9b7-4760-8db5-6dacdae3435e_dior sauvage.png', 1),
(3, (SELECT Id FROM DungTich WHERE Ma = 'DT50'), (SELECT Id FROM NongDo WHERE Ma = 'EDP'), 'GUCCI_BLOOM_50_EDP', 60, 1800000, 2800000, '30a05ddc-7b17-4036-870e-e0b8ae5c084d_gucci bloom.png', 1),

(4, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'DIOR_JADORE_50_EDP', 40, 2200000, 3200000, '8fda4029-592e-4542-be9e-cc3b4ccc36e7_J’adore Dior.png', 1),
(4, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'DIOR_JADORE_100_EDP', 30, 2900000, 3900000, '5e473f2f-af23-43a1-8cf4-09d393b92487_J’adore Dior.png', 1),

-- Chanel Coco Mademoiselle
(5, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CHANEL_COCO_50_EDP', 35, 2500000, 3600000, 'b7338fe3-d419-450e-b283-178b183bb0ff_Coco Mademoiselle.png', 1),
(5, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CHANEL_COCO_100_EDP', 20, 3400000, 4800000, '653e7a55-dd2e-43b5-b901-b43c2a2607e1_Coco Mademoiselle.png', 1),

-- YSL Black Opium
(6, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'YSL_BLACK_OPIUM_50_EDP', 40, 2100000, 3100000, '0bb50091-21dc-4904-a63c-0a7fd8ea2af9_Black Opium.png', 1),
(6, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'YSL_BLACK_OPIUM_100_EDP', 25, 2900000, 4100000, '10df4683-52ad-4558-a926-a1befe50ae02_Black Opium.png', 1),

-- Versace Eros
(7, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'VERSACE_EROS_100_EDT', 50, 2300000, 3400000, '74aed322-c36b-456d-98fa-4193a79d3414_Versace Eros.png', 1),

-- Armani Acqua di Gio
(8, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'ARMANI_GIO_50_EDT', 40, 2000000, 2900000, '3234fa77-a28d-47c7-8003-b5703d9e14d9_Acqua di Gio.png', 1),
(8, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'ARMANI_GIO_100_EDT', 30, 2600000, 3600000, '8a4b27a1-3b65-43f5-b014-9694ca4c044a_Acqua di Gio.png', 1),

-- D&G Light Blue
(9, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'DG_LIGHT_BLUE_50_EDT', 50, 1800000, 2700000, '7b1ccd59-6205-4497-adc4-de6e8f8caa21_Light Blue.png', 1),

-- Gucci Guilty
(10, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'GUCCI_GUILTY_100_EDT', 35, 2400000, 3400000, '6cc6bcb6-33c0-47c1-a01d-ea92dce40326_Gucci Guilty Pour Homme.png', 1),

-- Montblanc Explorer
(11, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'MONTBLANC_EXPLORER_100_EDP', 40, 2500000, 3600000, 'b4945dd9-8d20-496e-8eee-b327defe757a_Montblanc Explorer.png', 1),

-- Tom Ford Black Orchid
(12, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'TF_BLACK_ORCHID_50_EDP', 20, 3500000, 4800000, '9a6fba4a-d79f-4c4a-9705-3e688b0dba56_Black Orchid.png', 1),
(12, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'TF_BLACK_ORCHID_100_EDP', 15, 4200000, 5800000, 'c7f0f278-8de5-4850-aeef-fff77f4bfdc7_Black Orchid.png', 1),

-- Creed Aventus
(13, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CREED_AVENTUS_50_EDP', 25, 4800000, 6200000, '71e80d45-8055-46e4-90cb-3ef58c1b98bb_Creed Aventus.png', 1),
(13, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CREED_AVENTUS_100_EDP', 20, 6800000, 8800000, 'c5e79850-19a2-4214-b5a1-61f5d259893a_Creed Aventus.png', 1);

GO 

-- Chèn Hóa Đơn - Giữ nguyên
INSERT INTO HoaDon (IdKH, IdNV, Ma, NgayTao, NgayThanhToan, TenNguoiNhan, DiaChi, Sdt, TinhTrang) VALUES
((SELECT Id FROM NguoiDung WHERE Ma = 'KH001'), (SELECT Id FROM NguoiDung WHERE Ma = 'NV001'), 'HD001', '2024-05-10 10:00:00', '2024-05-10 10:05:00', N'Lê Thị Mai', N'10 Lý Thường Kiệt, Hoàn Kiếm', '0369852147', 1);

-- Chèn Giảm Giá - Giữ nguyên
INSERT INTO GiamGia (Ma, Ten, LoaiGiam, GiaTri, SoLuong, NgayBatDau, NgayKetThuc, TrangThai)
VALUES 
('GG10', N'Giảm 10% toàn bộ đơn hàng', 'PERCENT', 10, 100, '2024-05-01', '2024-12-31', 1),
('GG100K', N'Giảm 100.000đ cho đơn từ 1 triệu', 'AMOUNT', 100000, 50, '2024-06-01', '2024-12-31', 1);
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