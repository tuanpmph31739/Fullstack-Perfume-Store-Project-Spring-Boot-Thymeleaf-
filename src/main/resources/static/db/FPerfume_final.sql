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
-- PHẦN 1: TẠO CẤU TRÚC BẢNG (Đã sắp xếp lại thứ tự)
-- =================================================================================
PRINT 'Bat dau tao cac bang...';

-- Bảng Cấp 1 (Không phụ thuộc)
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
PRINT N'Bảng NguoiDung đã được tạo.';
GO

CREATE TABLE ThuongHieu (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100),
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgaySua DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE XuatXu (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    Ten NVARCHAR(100),
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgaySua DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE DungTich (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    SoMl INT,
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgaySua DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE LoaiNuocHoa (
    Id INT PRIMARY KEY IDENTITY(1,1),
    TenLoai NVARCHAR(50) NOT NULL,
    MoTa NVARCHAR(255),
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgaySua DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE MuaThichHop (
    Id BIGINT IDENTITY(1,1) PRIMARY KEY,
    MaMua NVARCHAR(20) UNIQUE,
    TenMua NVARCHAR(50),
    MoTa NVARCHAR(255),
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgaySua DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE NongDo (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(10) UNIQUE,
    Ten NVARCHAR(100),
    MoTa NVARCHAR(255),
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgaySua DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE NhomHuong (
    Id BIGINT PRIMARY KEY IDENTITY(1,1),
    Ma NVARCHAR(20) UNIQUE,
    TenNhomHuong NVARCHAR(100),
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgaySua DATETIME2 DEFAULT GETDATE()
);
PRINT N'Đã tạo các bảng thuộc tính (Cấp 1).';
GO

-- Bảng Cấp 2 (Phụ thuộc Cấp 1)
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
PRINT N'Bảng SanPham đã được tạo.';
GO

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
PRINT N'Bảng SanPhamChiTiet đã được tạo.';
GO

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
PRINT N'Bảng GiamGia đã được tạo.';
GO

-- ======================================================
-- PHẦN SẮP XẾP LẠI: ThanhToan PHẢI ĐƯỢC TẠO TRƯỚC HoaDon
-- ======================================================
CREATE TABLE ThanhToan (
    Id BIGINT IDENTITY(1,1) PRIMARY KEY,
    HinhThucThanhToan NVARCHAR(255) NOT NULL,
    Mota NVARCHAR(1000),
    TrangThai BIT NOT NULL DEFAULT 1,
    NgayTao DATETIME2 DEFAULT GETDATE(), -- Đã sửa
    NgaySua DATETIME2 DEFAULT GETDATE()  -- Đã sửa
);
PRINT N'Bảng ThanhToan đã được tạo.';
GO

CREATE TABLE HoaDon (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdKH BIGINT NOT NULL FOREIGN KEY REFERENCES NguoiDung(Id),
    IdNV BIGINT NULL FOREIGN KEY REFERENCES NguoiDung(Id),
    Ma NVARCHAR(20) UNIQUE,
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgayThanhToan DATETIME2 NULL,
    TenNguoiNhan NVARCHAR(100),
    DiaChi NVARCHAR(255),
    Sdt NVARCHAR(20),
	TrangThai NVARCHAR(50) NOT NULL DEFAULT N'CHỜ XÁC NHẬN', -- Trạng thái đơn hàng
    CONSTRAINT CK_orders_status CHECK (TrangThai IN (N'CHỜ XÁC NHẬN', N'ĐÃ XÁC NHẬN', N'ĐANG GIAO HÀNG', N'HOÀN THÀNH', N'ĐÃ HỦY', N'ĐÃ THANH TOÁN', N'ĐANG CHỜ THANH TOÁN')),

    -- 1. Tổng tiền hàng (SUM từ HoaDonChiTiet)
    TongTienHang DECIMAL(20, 0) NOT NULL DEFAULT 0,

    -- 2. Mã giảm giá áp dụng (nếu có)
    IdGiamGia INT NULL FOREIGN KEY REFERENCES GiamGia(Id),

    -- 3. Số tiền được giảm (tính toán từ IdGiamGia)
    TienGiamGia DECIMAL(20, 0) NOT NULL DEFAULT 0,

    -- 4. Phí vận chuyển
    PhiShip DECIMAL(20, 0) DEFAULT 30000,

    -- 5. TỔNG TIỀN CUỐI CÙNG (TongTienHang - TienGiamGia + PhiShip)
    TongThanhToan DECIMAL(20, 0) NOT NULL DEFAULT 0,

    -- Khóa ngoại tham chiếu đến ThanhToan
    IdThanhToan BIGINT NULL FOREIGN KEY REFERENCES ThanhToan(Id),
    NgayGiaoHang DATETIME2 NULL
);
PRINT N'Bảng HoaDon đã được tạo (sau ThanhToan).';
GO
-- ======================================================

-- Bảng Cấp 3 (Phụ thuộc Cấp 2)
CREATE TABLE HoaDonChiTiet (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdHoaDon INT NOT NULL FOREIGN KEY REFERENCES HoaDon(Id),
    IdSanPhamChiTiet INT NOT NULL FOREIGN KEY REFERENCES SanPhamChiTiet(Id),
    SoLuong INT NOT NULL CHECK (SoLuong > 0),
    DonGia DECIMAL(20, 0) NOT NULL CHECK (DonGia >= 0),
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    GhiChu NVARCHAR(255) NULL,
    TrangThai INT DEFAULT 1
);
PRINT N'Bảng HoaDonChiTiet đã được tạo.';
GO

CREATE TABLE GioHang (
    Id INT PRIMARY KEY IDENTITY(1,1),
    IdKH BIGINT NOT NULL FOREIGN KEY REFERENCES NguoiDung(Id),
    NgayTao DATETIME2 DEFAULT GETDATE(),
    NgaySua DATETIME2 DEFAULT GETDATE(),
    IdGiamGia INT NULL FOREIGN KEY REFERENCES GiamGia(Id)
);
PRINT N'Bảng GioHang đã được tạo.';
GO

CREATE TABLE GioHangChiTiet (
    IdGioHang INT FOREIGN KEY REFERENCES GioHang(Id),
    IdSanPhamChiTiet INT FOREIGN KEY REFERENCES SanPhamChiTiet(Id),
    SoLuong INT,
    PRIMARY KEY (IdGioHang, IdSanPhamChiTiet)
);
PRINT N'Bảng GioHangChiTiet đã được tạo.';
GO

-- Thêm các cột Ngày tạo/sửa
ALTER TABLE SanPham ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE SanPhamChiTiet ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE HoaDon ADD NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE HoaDonChiTiet ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();

PRINT 'Da tao xong tat ca cac bang.';
GO

-- =================================================================================
-- PHẦN 2: CHÈN DỮ LIỆU MẪU (Đã thêm HoaDon, ThanhToan)
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
('TH01', N'Chanel'), ('TH02', N'Dior'), ('TH03', N'Yves Saint Laurent'), ('TH04', N'Versace'), ('TH05', N'Giorgio Armani'),
('TH06', N'Dolce & Gabbana'), ('TH07', N'Gucci'), ('TH08', N'Montblanc'), ('TH09', N'Jean Paul Gaultier'), ('TH10', N'Creed'),
('TH11', N'Maison Francis Kurkdjian (MFK)'), ('TH12', N'Le Labo'), ('TH13', N'Tom Ford');
GO
INSERT INTO XuatXu (Ma, Ten) VALUES ('XX01', N'Pháp'), ('XX02', N'Ý'), ('XX03', N'Anh'), ('XX04', N'Mỹ'), ('XX05', N'Đức');
GO
INSERT INTO DungTich (Ma, SoMl) VALUES ('DT30', 30), ('DT50', 50), ('DT100', 100);
GO
INSERT INTO LoaiNuocHoa (TenLoai, MoTa) VALUES (N'Nam', N'Hương nam tính'), (N'Nữ', N'Hương nữ tính'), (N'Unisex', N'Hương trung tính');
GO
INSERT INTO MuaThichHop (MaMua, TenMua, MoTa) VALUES
('NONG', N'Mùa Nóng', N'Phù hợp cho thời tiết ấm áp, nóng bức (Xuân, Hè)'),
('LANH', N'Mùa Lạnh', N'Phù hợp cho thời tiết mát mẻ, se lạnh (Thu, Đông)');
GO
INSERT INTO NongDo (Ma, Ten, MoTa) VALUES
('EDC', N'Eau de Cologne', N'Nồng độ 3-5% tinh dầu'), ('EDT', N'Eau de Toilette', N'Nồng độ 5-15% tinh dầu'),
('EDP', N'Eau de Parfum', N'Nồng độ 15-20% tinh dầu'), ('Parfum', N'Parfum', N'Nồng độ 20-40% tinh dầu');
GO
INSERT INTO NhomHuong (Ma, TenNhomHuong) VALUES
('WOODY', N'Hương Gỗ (Woody)'), ('CITRUS', N'Hương Cam Chanh (Citrus)'), ('AROMATIC', N'Hương Thảo Mộc (Aromatic)'),
('AQUATIC', N'Hương Biển (Aquatic)'), ('LEATHER', N'Hương Da Thuộc (Leather)'), ('FRUITY', N'Hương Trái Cây (Fruity)'), ('FLORAL', N'Hương Hoa Cỏ (Floral)');
GO
-- Chèn SanPham (Gốc)
INSERT INTO SanPham (TenNuocHoa, IdThuongHieu, IdXuatXu, IdLoai, IdMuaThichHop, IdNhomHuong, MoTa)
VALUES
(N'Bleu de Chanel', (SELECT Id FROM ThuongHieu WHERE Ma = 'TH01'), (SELECT Id FROM XuatXu WHERE Ma = 'XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'), (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'), N'Mùi hương gỗ thơm nam tính, mạnh mẽ.'),
(N'Sauvage Dior', (SELECT Id FROM ThuongHieu WHERE Ma = 'TH02'), (SELECT Id FROM XuatXu WHERE Ma = 'XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'), (SELECT Id FROM NhomHuong WHERE Ma = 'CITRUS'), N'Hương thơm tươi mát, hoang dã và đầy lôi cuốn.'),
(N'Gucci Bloom', (SELECT Id FROM ThuongHieu WHERE Ma = 'TH03'), (SELECT Id FROM XuatXu WHERE Ma = 'XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'), (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'), N'Một vườn hoa trắng phong phú với hoa huệ, hoa nhài.'),
(N'J’adore Dior', (SELECT Id FROM ThuongHieu WHERE Ma='TH02'), (SELECT Id FROM XuatXu WHERE Ma='XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'), (SELECT Id FROM NhomHuong WHERE Ma='FLORAL'), N'Hương hoa trắng sang trọng, biểu tượng của sự nữ tính hiện đại.'),
(N'Coco Mademoiselle', (SELECT Id FROM ThuongHieu WHERE Ma='TH01'), (SELECT Id FROM XuatXu WHERE Ma='XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='CITRUS'), N'Hương hoa cam, hoắc hương và xạ hương thanh lịch.'),
(N'Black Opium', (SELECT Id FROM ThuongHieu WHERE Ma='TH03'), (SELECT Id FROM XuatXu WHERE Ma='XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='AROMATIC'), N'Hương cà phê, vani và hoa trắng gợi cảm.'),
(N'Versace Eros', (SELECT Id FROM ThuongHieu WHERE Ma='TH04'), (SELECT Id FROM XuatXu WHERE Ma='XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='WOODY'), N'Hương bạc hà, táo xanh và gỗ tuyết tùng mạnh mẽ.'),
(N'Acqua di Gio', (SELECT Id FROM ThuongHieu WHERE Ma='TH05'), (SELECT Id FROM XuatXu WHERE Ma='XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'), (SELECT Id FROM NhomHuong WHERE Ma='AQUATIC'), N'Hương biển mát lạnh, cổ điển, dễ dùng.'),
(N'Light Blue', (SELECT Id FROM ThuongHieu WHERE Ma='TH06'), (SELECT Id FROM XuatXu WHERE Ma='XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'), (SELECT Id FROM NhomHuong WHERE Ma='CITRUS'), N'Hương chanh tươi mát, thanh lịch và quyến rũ.'),
(N'Gucci Guilty Pour Homme', (SELECT Id FROM ThuongHieu WHERE Ma='TH07'), (SELECT Id FROM XuatXu WHERE Ma='XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='AROMATIC'), N'Hương lavender, cam bergamot và hoắc hương nam tính.'),
(N'Montblanc Explorer', (SELECT Id FROM ThuongHieu WHERE Ma='TH08'), (SELECT Id FROM XuatXu WHERE Ma='XX05'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='WOODY'), N'Hương gỗ và da thuộc sang trọng, lấy cảm hứng từ Creed Aventus.'),
(N'Black Orchid', (SELECT Id FROM ThuongHieu WHERE Ma='TH11'), (SELECT Id FROM XuatXu WHERE Ma='XX04'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Unisex'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='FLORAL'), N'Hương lan đen, socola và gỗ trầm hương đầy mê hoặc.'),
(N'Creed Aventus', (SELECT Id FROM ThuongHieu WHERE Ma='TH10'), (SELECT Id FROM XuatXu WHERE Ma='XX03'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='FRUITY'), N'Hương dứa, xạ hương và gỗ sồi – biểu tượng của sự thành công.');
GO
PRINT N'Đã chèn SanPham mẫu.';
GO

-- Chèn SanPhamChiTiet (Biến thể)
INSERT INTO SanPhamChiTiet (IdSanPham, IdDungTich, IdNongDo, MaSKU, SoLuongTon, GiaNhap, GiaBan, HinhAnh, TrangThai)
VALUES
(1, (SELECT Id FROM DungTich WHERE Ma = 'DT100'), (SELECT Id FROM NongDo WHERE Ma = 'EDP'), 'CHA_BLEU_100_EDP', 50, 2500000, 3500000, '44fd0374-9682-4f77-970a-595a111f8afa_bleu de chanel.png', 1),
(2, (SELECT Id FROM DungTich WHERE Ma = 'DT100'), (SELECT Id FROM NongDo WHERE Ma = 'EDT'), 'DIOR_SAUVAGE_100_EDT', 40, 2200000, 3200000, '3e926c9a-a4c9-4b06-b4ce-61a131b5afb9_dior sauvage.png', 1),
(3, (SELECT Id FROM DungTich WHERE Ma = 'DT50'), (SELECT Id FROM NongDo WHERE Ma = 'EDP'), 'GUCCI_BLOOM_50_EDP', 60, 1800000, 2800000, 'f4f07ace-f749-46c6-a2d3-e141e440e8ef_gucci bloom.png', 1),
(4, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'DIOR_JADORE_50_EDP', 40, 2200000, 3200000, 'cfa1edc0-faed-4ff8-90c0-4484d687cf59_J’adore Dior.png', 1),
(4, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'DIOR_JADORE_100_EDP', 30, 2900000, 3900000, '7f0aa36a-9cc7-4eea-ab52-adae0297bb84_J’adore Dior.png', 1),
(5, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CHANEL_COCO_50_EDP', 35, 2500000, 3600000, '6d52a2f7-78a7-463e-a01a-62a5b7056c81_Coco Mademoiselle.png', 1),
(5, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CHANEL_COCO_100_EDP', 20, 3400000, 4800000, 'b5788325-b52c-4a20-a6b1-0a3355ee0f9c_Coco Mademoiselle.png', 1),
(6, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'YSL_BLACK_OPIUM_50_EDP', 40, 2100000, 3100000, '886ac3c5-2097-4504-98f6-bc67a64eb897_Black Opium.png', 1),
(6, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'YSL_BLACK_OPIUM_100_EDP', 25, 2900000, 4100000, 'c477b2e6-eb5e-43ed-9d50-24bf1909293a_Black Opium.png', 1),
(7, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'VERSACE_EROS_100_EDT', 50, 2300000, 3400000, '927ccc3c-0f8e-48de-965d-a4f6a558809c_Versace Eros.png', 1),
(8, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'ARMANI_GIO_50_EDT', 40, 2000000, 2900000, '512b53a6-f198-4116-b035-7a1435989c70_Acqua di Gio.png', 1),
(8, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'ARMANI_GIO_100_EDT', 30, 2600000, 3600000, 'c8a127c4-6edb-4e0e-8c73-81023c96f436_Acqua di Gio.png', 1),
(9, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'DG_LIGHT_BLUE_50_EDT', 50, 1800000, 2700000, '7d7be7b8-915e-406d-bab1-5444ad5dd1ba_Light Blue.png', 1),
(10, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'GUCCI_GUILTY_100_EDT', 35, 2400000, 3400000, '5cbc4821-8296-4951-974e-97967ae295db_Gucci Guilty Pour Homme.png', 1),
(11, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'MONTBLANC_EXPLORER_100_EDP', 40, 2500000, 3600000, 'e8304397-9d1d-4423-9fd5-fb0dbdc7b997_Montblanc Explorer.png', 1),
(12, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'TF_BLACK_ORCHID_50_EDP', 20, 3500000, 4800000, 'af5393d5-6a12-49bb-9b88-49be149462ce_Black Orchid.png', 1),
(12, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'TF_BLACK_ORCHID_100_EDP', 15, 4200000, 5800000, '4fb50443-65cb-4a28-ac01-1c1ced77fc4c_Black Orchid.png', 1),
(13, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CREED_AVENTUS_50_EDP', 25, 4800000, 6200000, 'bf3d9c82-9848-452f-a032-01652e849962_Creed Aventus.png', 1),
(13, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CREED_AVENTUS_100_EDP', 20, 6800000, 8800000, '1e05e142-564d-421f-861c-3dcf6eef9ce7_Creed Aventus.png', 1);
GO
PRINT N'Đã chèn SanPhamChiTiet mẫu.';
GO

-- Chèn Giảm Giá
INSERT INTO GiamGia (Ma, Ten, LoaiGiam, GiaTri, SoLuong, NgayBatDau, NgayKetThuc, TrangThai)
VALUES
('GG10', N'Giảm 10% toàn bộ đơn hàng', 'PERCENT', 10, 100, '2024-05-01', '2024-12-31', 1),
('GG100K', N'Giảm 100.000đ cho đơn từ 1 triệu', 'AMOUNT', 100000, 50, '2024-06-01', '2024-12-31', 1);
GO
PRINT N'Đã chèn GiamGia mẫu.';
GO

-- ======================================================
-- PHẦN THÊM MỚI: Chèn dữ liệu cho ThanhToan, HoaDon
-- ======================================================
INSERT INTO ThanhToan (HinhThucThanhToan, Mota, TrangThai)
VALUES
(N'Thanh toán khi nhận hàng (COD)', N'Khách hàng trả tiền mặt khi nhận được sản phẩm', 1),
(N'Chuyển khoản ngân hàng', N'Khách hàng chuyển khoản qua ngân hàng', 1),
(N'Thanh toán qua VNPay', N'Thanh toán trực tuyến qua cổng VNPay', 1);
GO
PRINT N'Đã chèn ThanhToan mẫu.';
GO



-- ======================================================

-- CHÈN DỮ LIỆU GIOHANG
INSERT INTO GioHang (IdKH, NgayTao, NgaySua, IdGiamGia) VALUES
((SELECT Id FROM NguoiDung WHERE Ma = 'KH002'), '2024-05-12 09:30:00', '2024-05-12 09:30:00', NULL);
GO

-- Chèn Giỏ Hàng Chi Tiết
INSERT INTO GioHangChiTiet (IdGioHang, IdSanPhamChiTiet, SoLuong) VALUES
(1, 2, 1);
GO

PRINT 'Da chen xong toan bo du lieu mau.';
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
SELECT * FROM ThanhToan; -- Đã thêm
SELECT * FROM HoaDon;
SELECT * FROM HoaDonChiTiet;
SELECT * FROM GioHang;
SELECT * FROM GioHangChiTiet;
GO