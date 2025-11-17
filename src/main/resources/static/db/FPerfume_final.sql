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
                           Id INT PRIMARY KEY IDENTITY(1,1),
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

                            Slug NVARCHAR(150) UNIQUE NULL,

                            HinhAnh NVARCHAR(255) NULL,

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
                         Id INT IDENTITY(1,1) PRIMARY KEY,

                         Ma NVARCHAR(50) NOT NULL UNIQUE,
                         Ten NVARCHAR(255) NOT NULL,
                         MoTa NVARCHAR(MAX) NULL,

                         LoaiGiam VARCHAR(20) NOT NULL CHECK (LoaiGiam IN ('PERCENT', 'AMOUNT')),
                         GiaTri DECIMAL(10, 2) NOT NULL,

                         SoLuong INT NOT NULL DEFAULT 0, -- ⭐ THÊM Ở ĐÂY

                         DonHangToiThieu DECIMAL(18, 2) NOT NULL DEFAULT 0,
                         GiamToiDa DECIMAL(18, 2) NULL,

                         NgayBatDau DATETIME NOT NULL,
                         NgayKetThuc DATETIME NOT NULL,

                         TrangThai BIT NOT NULL DEFAULT 1,

                         PhamViApDung VARCHAR(30) NOT NULL CHECK (PhamViApDung IN ('SANPHAM', 'TOAN_CUA_HANG')),

                         IdSanPhamChiTiet INT NULL,
                         CONSTRAINT FK_GiamGia_SanPhamChiTiet
                             FOREIGN KEY (IdSanPhamChiTiet)
                                 REFERENCES SanPhamChiTiet(Id)
);
PRINT N'Bảng GiamGia đã được tạo!';
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
                        IdKH INT NOT NULL FOREIGN KEY REFERENCES NguoiDung(Id),
                        IdNV INT NULL FOREIGN KEY REFERENCES NguoiDung(Id),
                        Ma NVARCHAR(20) UNIQUE,
                        NgayTao DATETIME2 DEFAULT GETDATE(),
                        NgayThanhToan DATETIME2 NULL,
                        TenNguoiNhan NVARCHAR(100),
                        DiaChi NVARCHAR(255),
                        Sdt NVARCHAR(20),
                        TrangThai NVARCHAR(100) NULL,

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
                         IdKH INT NOT NULL FOREIGN KEY REFERENCES NguoiDung(Id),
                         NgayTao DATETIME2 DEFAULT GETDATE(),
                         NgaySua DATETIME2 DEFAULT GETDATE(),
                         IdGiamGia INT NULL FOREIGN KEY REFERENCES GiamGia(Id)
);
PRINT N'Bảng GioHang đã được tạo.';
GO

-- Xóa nếu đã tồn tại (để tránh lỗi khi chạy lại)
IF OBJECT_ID('dbo.GioHangChiTiet', 'U') IS NOT NULL
BEGIN
DROP TABLE GioHangChiTiet;
PRINT N'Đã xóa bảng GioHangChiTiet cũ.';
END
GO

CREATE TABLE GioHangChiTiet (
                                IdGioHang INT NOT NULL,
                                IdSanPhamChiTiet INT NOT NULL,
                                SoLuong INT NOT NULL CHECK (SoLuong > 0),

                                CONSTRAINT PK_GioHangChiTiet PRIMARY KEY (IdGioHang, IdSanPhamChiTiet),

                                CONSTRAINT FK_GioHangChiTiet_GioHang FOREIGN KEY (IdGioHang)
                                    REFERENCES GioHang(Id)
                                    ON DELETE CASCADE,

                                CONSTRAINT FK_GioHangChiTiet_SanPhamChiTiet FOREIGN KEY (IdSanPhamChiTiet)
                                    REFERENCES SanPhamChiTiet(Id)
                                    ON DELETE CASCADE
);
PRINT N'Bảng GioHangChiTiet đã được tạo (chuẩn Hibernate, hỗ trợ khóa tổng hợp).';
GO


-- Thêm các cột Ngày tạo/sửa
ALTER TABLE SanPham ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE SanPhamChiTiet ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE HoaDon ADD NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE HoaDonChiTiet ADD NgayTao DATETIME2 DEFAULT GETDATE(), NgaySua DATETIME2 DEFAULT GETDATE();
ALTER TABLE NguoiDung ADD VerificationCode NVARCHAR(255) NULL;
ALTER TABLE NguoiDung ADD Enabled BIT DEFAULT 0;
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
INSERT INTO ThuongHieu (Ma, Ten, Slug, HinhAnh) VALUES
('TH01', N'Chanel',                          'chanel',                          'de91baf7-991a-4bd6-bb15-69abd72da11d_logo-chanel.png'),
('TH02', N'Dior',                            'dior',                            '24d9835d-becd-4ec6-9e70-f0e55d22411a_logo-dior.png'),
('TH03', N'Yves Saint Laurent',              'yves-saint-laurent',              '6128435d-baef-447d-b0a9-0dc589dbe34f_logo-ysl-1.png'),
('TH04', N'Versace',                         'versace',                         '8b766ccb-1654-48a2-ab73-f6c07a70bb4a_logo-versace.png'),
('TH05', N'Giorgio Armani',                  'giorgio-armani',                  'c178db98-d1a3-4bee-adb7-78a753062bcf_logo-giorgio-armani.png'),
('TH06', N'Dolce & Gabbana',                 'dolce-gabbana',                   '38439a19-131e-4a56-8c9a-43fe83f5d7fe_logo-dolce-gabbana.png'),
('TH07', N'Gucci',                           'gucci',                           'fdf1bebd-971d-4600-a2a9-3cb84e402428_logo-gucci.png'),
('TH08', N'Montblanc',                       'montblanc',                       '7b15cfb3-6b33-40f8-9b2c-e9ea84c64a6a_logo-mont-blanc.png'),
('TH09', N'Jean Paul Gaultier',              'jean-paul-gaultier',              '574f6e87-d1a1-4d73-9490-b4d396ad63f8_logo-jean-paul-gaultier.png'),
('TH10', N'Creed',                           'creed',                           'aa02617e-c7ba-4846-bafb-46a0fb4795c0_logo-creed.png'),
('TH11', N'Maison Francis Kurkdjian (MFK)',  'maison-francis-kurkdjian-mfk',    'ade4fae1-78e1-4317-b85c-6981984d4330_logo-maison-francis-kurkdjian.png'),
('TH12', N'Le Labo',                         'le-labo',                         '9313f5e9-1d4f-423c-860d-a2582020e124_logo-le-labo.png'),
('TH13', N'Tom Ford',                        'tom-ford',                        '67a2df5d-70ca-46d3-84f0-38dfc5a5c32f_logo-tom-ford.png'),
('TH14', N'Jo Malone London',				 'jo-malone-london',				'efc85365-a9e5-46cb-b4bb-8383dacda5e0_logo-jo-malone.png'),
('TH15', N'Hermes',							 'hermes',							'332fc1d8-9644-482b-a31d-56471063bfa8_logo-hermes.png'),
('TH16', N'Lancome',						 'lancome',							'90fc377a-9aa8-4f88-bd36-c2f26ff0934e_Lancome-logo.png'),
('TH17', N'Kilian',							 'kilian',							'b9e241a9-4eb1-44b9-9a29-76bece8c8530_logo-kilian.png'),
('TH18', N'Byredo',							 'byredo',							'b859c5a9-55a2-4f35-af96-fbd8fd1057e4_logo-byredo.png'),
('TH19', N'Bvlgari',						 'bvlgari',							'49cc07f8-4b70-4544-ae0b-e334b72480a0_logo-bvlgari.png'),
('TH20', N'Prada',							 'prada',							'fe22b22e-3245-4268-b0e9-1f1399d2ec8c_logo-prada.png'),
('TH21', N'Calvin Klein',					 'calvin-klein',					'd4d56595-525e-42b5-a888-178c47b15802_logo-calvin-klein.png'),
('TH22', N'Maison Margiela',				 'maison-margiela',					'f1e7fbf6-d6ab-459f-a005-0417a1b53bb8_logo-maison-margiela.png'),
('TH23', N'Narciso Rodriguez',				 'narciso-rodriguez',				'48829eb1-9564-42e5-94a6-3bd009a89905_logo-narciso-rodriguez.png'),
('TH24', N'Roja Parfums',                    'roja-parfums',					'926c2fca-840d-486f-9342-f841c6773a36_logo-roja-parfums.png');
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
(N'Bleu de Chanel EDP', (SELECT Id FROM ThuongHieu WHERE Ma = 'TH01'), (SELECT Id FROM XuatXu WHERE Ma = 'XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'), (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'), N'Chanel Bleu De Chanel EDP sở hữu nhiều tầng hương khác nhau. Hương đầu là vị chanh vàng, ớt hồng và bạc hà. Tầng hương giữa là dưa vàng hoa nhài và gừng. Tầng hương cuối là hương thơm lan tỏa của gỗ tuyết tùng, hổ phách và gỗ đàn hương. Chanel Bleu De Chanel EDP mang đến sự lịch lãm, nam tính và gai góc nhưng cũng ẩn chứa bên trong sự dịu dàng và tinh tế.'),
(N'Dior Sauvage Eau de Toilette', (SELECT Id FROM ThuongHieu WHERE Ma = 'TH02'), (SELECT Id FROM XuatXu WHERE Ma = 'XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'), (SELECT Id FROM NhomHuong WHERE Ma = 'CITRUS'), N'Hương thơm tươi mát, hoang dã và đầy lôi cuốn.'),
(N'Gucci Bloom EDP', (SELECT Id FROM ThuongHieu WHERE Ma = 'TH07'), (SELECT Id FROM XuatXu WHERE Ma = 'XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'), (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'), N'Một vườn hoa trắng phong phú với hoa huệ, hoa nhài.'),
(N'J’adore Dior', (SELECT Id FROM ThuongHieu WHERE Ma='TH02'), (SELECT Id FROM XuatXu WHERE Ma='XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'), (SELECT Id FROM NhomHuong WHERE Ma='FLORAL'), N'Hương hoa trắng sang trọng, biểu tượng của sự nữ tính hiện đại.'),
(N'Coco Mademoiselle', (SELECT Id FROM ThuongHieu WHERE Ma='TH01'), (SELECT Id FROM XuatXu WHERE Ma='XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='CITRUS'), N'Hương hoa cam, hoắc hương và xạ hương thanh lịch.'),
(N'Black Opium', (SELECT Id FROM ThuongHieu WHERE Ma='TH03'), (SELECT Id FROM XuatXu WHERE Ma='XX01'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='AROMATIC'), N'Hương cà phê, vani và hoa trắng gợi cảm.'),
(N'Versace Eros', (SELECT Id FROM ThuongHieu WHERE Ma='TH04'), (SELECT Id FROM XuatXu WHERE Ma='XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='WOODY'), N'Hương bạc hà, táo xanh và gỗ tuyết tùng mạnh mẽ.'),
(N'Acqua di Gio', (SELECT Id FROM ThuongHieu WHERE Ma='TH05'), (SELECT Id FROM XuatXu WHERE Ma='XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'), (SELECT Id FROM NhomHuong WHERE Ma='AQUATIC'), N'Hương biển mát lạnh, cổ điển, dễ dùng.'),
(N'Light Blue', (SELECT Id FROM ThuongHieu WHERE Ma='TH06'), (SELECT Id FROM XuatXu WHERE Ma='XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nữ'), (SELECT Id FROM MuaThichHop WHERE MaMua='NONG'), (SELECT Id FROM NhomHuong WHERE Ma='CITRUS'), N'Hương chanh tươi mát, thanh lịch và quyến rũ.'),
(N'Gucci Guilty Pour Homme', (SELECT Id FROM ThuongHieu WHERE Ma='TH07'), (SELECT Id FROM XuatXu WHERE Ma='XX02'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='AROMATIC'), N'Hương lavender, cam bergamot và hoắc hương nam tính.'),
(N'Montblanc Explorer', (SELECT Id FROM ThuongHieu WHERE Ma='TH08'), (SELECT Id FROM XuatXu WHERE Ma='XX05'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='WOODY'), N'Hương gỗ và da thuộc sang trọng, lấy cảm hứng từ Creed Aventus.'),
(N'Black Orchid', (SELECT Id FROM ThuongHieu WHERE Ma='TH11'), (SELECT Id FROM XuatXu WHERE Ma='XX04'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Unisex'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='FLORAL'), N'Hương lan đen, socola và gỗ trầm hương đầy mê hoặc.'),
(N'Creed Aventus', (SELECT Id FROM ThuongHieu WHERE Ma='TH10'), (SELECT Id FROM XuatXu WHERE Ma='XX03'), (SELECT Id FROM LoaiNuocHoa WHERE TenLoai=N'Nam'), (SELECT Id FROM MuaThichHop WHERE MaMua='LANH'), (SELECT Id FROM NhomHuong WHERE Ma='FRUITY'), N'Hương dứa, xạ hương và gỗ sồi – biểu tượng của sự thành công.'),

-- TH14 - Jo Malone London (XX03 = Anh)
(N'English Pear & Freesia',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH14'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX03'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Unisex'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FRUITY'),
 N'Hương lê và hoa lan chuông tươi mát, dễ dùng hằng ngày.'),
(N'Wood Sage & Sea Salt',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH14'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX03'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Unisex'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'AQUATIC'),
 N'Hương biển, muối và gỗ tuyết tùng, rất sạch và tự nhiên.'),

-- TH15 - Hermes (XX01 = Pháp)
(N'Terre d''Hermes',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH15'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'),
 N'Hương gỗ, cam chanh và tiêu, nam tính, trưởng thành.'),
(N'Twilly d''Hermes',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH15'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'),
 N'Hương hoa, gừng và gỗ đàn hương, trẻ trung, cá tính.'),

-- TH16 - Lancome (XX01 = Pháp)
(N'La Vie Est Belle',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH16'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'),
 N'Hương kẹo bơ, vani và hoa iris ngọt ngào, nữ tính.'),
(N'Idole',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH16'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'),
 N'Hương hoa hồng hiện đại, tươi sáng, dễ dùng.'),

-- TH17 - Kilian (XX01 = Pháp)
(N'Love Don''t Be Shy',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH17'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Unisex'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FRUITY'),
 N'Hương kẹo marshmallow, cam bergamot và hoa cam, ngọt quyến rũ.'),
(N'Straight to Heaven',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH17'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'),
 N'Hương rượu rum, gỗ tuyết tùng và hoắc hương, trầm ấm.'),

-- TH18 - Byredo (XX03 = Anh/Thụy Điển, tạm dùng Anh)
(N'Gypsy Water',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH18'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX03'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Unisex'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'),
 N'Hương gỗ thông, chanh và vani, sạch, thơm da thịt.'),
(N'Bal d''Afrique',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH18'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX03'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Unisex'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'CITRUS'),
 N'Hương cam chanh, hoa và gỗ, cảm hứng châu Phi.'),

-- TH19 - Bvlgari (XX02 = Ý)
(N'Bvlgari Man in Black',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH19'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX02'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'LEATHER'),
 N'Hương da thuộc, rượu rum và gia vị, rất nam tính.'),
(N'Omnia Crystalline',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH19'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX02'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'),
 N'Hương sen, hoa mẫu đơn và trái lê, nhẹ nhàng, trong trẻo.'),

-- TH20 - Prada (XX02 = Ý)
(N'Prada L''Homme',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH20'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX02'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'),
 N'Hương xà phòng sạch, iris và gỗ, văn phòng lịch sự.'),
(N'Prada Candy',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH20'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX02'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FRUITY'),
 N'Hương caramel, vani và xạ hương, ngọt kẹo, dễ thương.'),

-- TH21 - Calvin Klein (XX04 = Mỹ)
(N'CK One',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH21'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX04'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Unisex'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'CITRUS'),
 N'Hương cam chanh, trà xanh, rất tươi mát, dễ xịt.'),
(N'Eternity for Men',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH21'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX04'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'AROMATIC'),
 N'Hương lavender, thảo mộc và gỗ, cổ điển cho nam.'),

-- TH22 - Maison Margiela (XX04 = Mỹ/Pháp, tạm dùng Mỹ)
(N'Replica Jazz Club',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH22'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX04'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Unisex'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'),
 N'Hương rượu rum, thuốc lá và vani, vibe quán bar jazz.'),
(N'Replica Lazy Sunday Morning',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH22'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX04'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Unisex'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'),
 N'Hương xà phòng sạch, hoa trắng, cảm giác ga giường mới giặt.'),

-- TH23 - Narciso Rodriguez (XX01 = Pháp/Mỹ, tạm dùng Pháp)
(N'Narciso Rodriguez For Her EDP',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH23'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'),
 N'Hương xạ hương, hoa hồng và đào, nữ tính sang trọng.'),
(N'Narciso Rodriguez For Him Bleu Noir',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH23'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX01'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'WOODY'),
 N'Hương xạ hương, gỗ tuyết tùng và vetiver, trầm ấm, lịch sự.'),
 (N'Roja Elysium Pour Homme',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH24'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX03'),           -- Anh
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'NONG'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'CITRUS'),
 N'Hương cam chanh tươi mát, gỗ và xạ hương, rất dễ dùng, sang và sạch.'),

-- 2. Roja Enigma Parfum Cologne
(N'Roja Enigma Parfum Cologne',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH24'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX03'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nam'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'AROMATIC'),
 N'Hương rượu cognac, vani, thuốc lá, ngọt ấm, rất quyến rũ.'),

-- 3. Roja Scandal Pour Femme
(N'Roja Scandal Pour Femme',
 (SELECT Id FROM ThuongHieu WHERE Ma = 'TH24'),
 (SELECT Id FROM XuatXu WHERE Ma = 'XX03'),
 (SELECT Id FROM LoaiNuocHoa WHERE TenLoai = N'Nữ'),
 (SELECT Id FROM MuaThichHop WHERE MaMua = 'LANH'),
 (SELECT Id FROM NhomHuong WHERE Ma = 'FLORAL'),
 N'Hương hoa trắng, trái cây và xạ hương, rất nữ tính, sang trọng.');
GO

GO
PRINT N'Đã chèn SanPham mẫu.';
GO

-- Chèn SanPhamChiTiet (Biến thể)
INSERT INTO SanPhamChiTiet (IdSanPham, IdDungTich, IdNongDo, MaSKU, SoLuongTon, GiaNhap, GiaBan, HinhAnh, TrangThai)
VALUES
(1, (SELECT Id FROM DungTich WHERE Ma = 'DT100'), (SELECT Id FROM NongDo WHERE Ma = 'EDP'), 'CHA_BLEU_100_EDP', 50, 2500000, 3500000, '4b2a32cb-0f0f-47c3-8a91-2c9c4394cca1_bleu de chanel.png', 1),
(2, (SELECT Id FROM DungTich WHERE Ma = 'DT100'), (SELECT Id FROM NongDo WHERE Ma = 'EDT'), 'DIOR_SAUVAGE_100_EDT', 40, 2200000, 3200000, '2de4f339-7875-417b-ade1-55434dbbe940_dior sauvage.png', 1),
(3, (SELECT Id FROM DungTich WHERE Ma = 'DT50'), (SELECT Id FROM NongDo WHERE Ma = 'EDP'), 'GUCCI_BLOOM_50_EDP', 60, 1800000, 2800000, 'd9b503e2-3243-4fca-858c-d93f9d79fdd8_gucci bloom.png', 1),
(4, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'DIOR_JADORE_50_EDP', 40, 2200000, 3200000, '211d95c5-1fd3-40ad-9a96-a327557d939c_J’adore Dior.png', 1),
(4, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'DIOR_JADORE_100_EDP', 30, 2900000, 3900000, '973a5421-fc6e-4afe-a989-a149b84961a4_J’adore Dior.png', 1),
(5, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CHANEL_COCO_50_EDP', 35, 2500000, 3600000, '881695d2-67b3-4278-bca2-413a73397dca_Coco Mademoiselle.png', 1),
(5, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CHANEL_COCO_100_EDP', 20, 3400000, 4800000, 'ea8cbe30-9d06-41c4-94e6-55a8409bc293_Coco Mademoiselle.png', 1),
(6, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'YSL_BLACK_OPIUM_50_EDP', 40, 2100000, 3100000, '57b38f57-924e-4597-bf1e-7404b09f6a35_Black Opium.png', 1),
(6, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'YSL_BLACK_OPIUM_100_EDP', 25, 2900000, 4100000, 'b0efb088-aea3-4c8c-9565-c398d7eeccba_Black Opium.png', 1),
(7, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'VERSACE_EROS_100_EDT', 50, 2300000, 3400000, 'aa6dd6e6-46f4-40ef-a011-01b9b4b53f47_Versace Eros.png', 1),
(8, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'ARMANI_GIO_50_EDT', 40, 2000000, 2900000, 'af56c0b0-cbc6-464a-a440-6d1a6bad1745_Acqua di Gio.png', 1),
(8, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'ARMANI_GIO_100_EDT', 30, 2600000, 3600000, 'f957717a-5897-4e7e-9f96-beb358ce2a17_Acqua di Gio.png', 1),
(9, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'DG_LIGHT_BLUE_50_EDT', 50, 1800000, 2700000, '9c1706a0-15b9-475c-96c2-c0f323541de0_Light Blue.png', 1),
(10, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDT'), 'GUCCI_GUILTY_100_EDT', 35, 2400000, 3400000, '3d2bc963-78ce-40e9-9269-8ca6ccb3c975_Gucci Guilty Pour Homme.png', 1),
(11, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'MONTBLANC_EXPLORER_100_EDP', 40, 2500000, 3600000, '76b1584e-01d3-4823-812c-41ec38e85b76_Montblanc Explorer.png', 1),
(12, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'TF_BLACK_ORCHID_50_EDP', 20, 3500000, 4800000, '3248ff98-908b-49ad-9cde-487a2bc02119_Black Orchid.png', 1),
(12, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'TF_BLACK_ORCHID_100_EDP', 15, 4200000, 5800000, 'b7d25888-59d2-445b-8e8d-2746af7b8f92_Black Orchid.png', 1),
(13, (SELECT Id FROM DungTich WHERE Ma='DT50'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CREED_AVENTUS_50_EDP', 25, 4800000, 6200000, 'fc7738e7-268d-4885-a297-20251b89701c_Creed Aventus.png', 1),
(13, (SELECT Id FROM DungTich WHERE Ma='DT100'), (SELECT Id FROM NongDo WHERE Ma='EDP'), 'CREED_AVENTUS_100_EDP', 20, 6800000, 8800000, 'f0c27633-d26f-4fea-af7e-9d1db8ac5f79_Creed Aventus.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'English Pear & Freesia'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'JM_EPF_50_EDP', 30, 1800000, 2600000, 'a8532af8-e05c-473e-9c4c-905feaab7f17_English Pear & Freesia.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'English Pear & Freesia'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'JM_EPF_100_EDP', 20, 2600000, 3600000, '211a5ade-2725-40f2-bea3-5421ec1c13c6_English Pear & Freesia.png', 1),

-- Jo Malone - Wood Sage & Sea Salt
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Wood Sage & Sea Salt'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'JM_WS_50_EDP', 25, 1700000, 2500000, '1416d412-fd54-46ba-b598-0bd7f9ce5fc7_Wood Sage & Sea Salt.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Wood Sage & Sea Salt'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'JM_WS_100_EDP', 18, 2500000, 3500000, '023fbded-22b6-44b3-8de2-c6c951a220c4_Wood Sage & Sea Salt.png', 1),

-- Hermes - Terre d'Hermes (EDT)
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Terre d''Hermes'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'HER_TERRE_50_EDT', 20, 1900000, 2800000, '35dfcffe-2df7-4a6d-8805-f35ea8b2b38f_Terre dHermes.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Terre d''Hermes'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'HER_TERRE_100_EDT', 15, 2600000, 3800000, '438269bf-3f06-47a4-a503-614fb2cf9636_Terre dHermes.png', 1),

-- Hermes - Twilly d'Hermes
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Twilly d''Hermes'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'HER_TWILLY_50_EDP', 18, 2000000, 3000000, '457b4714-71cc-4a0f-a985-924388223a07_Twilly dHermes.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Twilly d''Hermes'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'HER_TWILLY_100_EDP', 12, 2800000, 4000000, '3a53b3a0-192a-451f-b58d-1d031cd202d9_Twilly dHermes.png', 1),

-- Lancome - La Vie Est Belle
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'La Vie Est Belle'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'LAN_LVEB_50_EDP', 30, 1900000, 2900000, 'b39d17d4-43ba-466b-b0f5-ad07d22f0984_La Vie Est Belle.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'La Vie Est Belle'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'LAN_LVEB_100_EDP', 20, 2600000, 3800000, 'ddeeda6d-a589-44d3-b132-a1df297a8c16_La Vie Est Belle.png', 1),

-- Lancome - Idole
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Idole'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'LAN_IDOLE_50_EDP', 25, 1800000, 2700000, 'dcb34a3e-169d-430a-83bc-692e608fb3be_Idole.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Idole'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'LAN_IDOLE_100_EDP', 15, 2500000, 3600000, '08f318e4-a281-4b59-9970-c3b4c3a54e66_Idole.png', 1),

-- Kilian - Love Don't Be Shy
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Love Don''t Be Shy'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'KIL_LDBS_50_EDP', 10, 3500000, 4800000, 'b88e1418-ca5f-4e60-990b-dd42847e93c5_Love Dont Be Shy.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Love Don''t Be Shy'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'KIL_LDBS_100_EDP', 8, 4800000, 6800000, '9551da18-9b98-46b0-8226-4db66b635edc_Love Dont Be Shy.png', 1),

-- Kilian - Straight to Heaven
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Straight to Heaven'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'KIL_STH_50_EDP', 10, 3400000, 4700000, '06210874-869b-4785-847a-c4d64b9bdc25_Straight to Heaven.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Straight to Heaven'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'KIL_STH_100_EDP', 7, 4700000, 6700000, '89c3db3a-5cd2-4277-804e-eb366702bde0_Straight to Heaven.png', 1),

-- Byredo - Gypsy Water
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Gypsy Water'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'BYR_GW_50_EDP', 18, 3000000, 4200000, 'a1bbc2e2-7ae0-4498-ba11-d01a8e1f7144_Gypsy Water.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Gypsy Water'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'BYR_GW_100_EDP', 12, 4200000, 6200000, 'a4ed01ff-cb8b-43ab-bbd2-d97c40527820_Gypsy Water.png', 1),

-- Byredo - Bal d'Afrique
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Bal d''Afrique'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'BYR_BDA_50_EDP', 16, 3000000, 4200000, 'e8a11929-bfdd-4c1c-bf6f-fee83c895b73_Bal dAfrique.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Bal d''Afrique'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'BYR_BDA_100_EDP', 10, 4200000, 6200000, 'c19200e5-3d97-4255-ade6-2f5943ba4f08_Bal dAfrique.png', 1),

-- Bvlgari - Man in Black
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Bvlgari Man in Black'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'BVLG_MIB_50_EDP', 20, 2200000, 3200000, 'f03a828c-7f02-438c-a465-05536b368b86_Bvlgari Man in Black.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Bvlgari Man in Black'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'BVLG_MIB_100_EDP', 14, 3000000, 4300000, '1d47cadb-85f7-4452-bca2-cb097f4fa6c8_Bvlgari Man in Black.png', 1),

-- Bvlgari - Omnia Crystalline
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Omnia Crystalline'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'BVLG_OMNIA_50_EDT', 22, 1800000, 2600000, '5a24f991-ecb0-4277-aa36-bd3f5da2a1bd_Omnia Crystalline.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Omnia Crystalline'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'BVLG_OMNIA_100_EDT', 15, 2400000, 3400000, '2478187f-4833-480d-b60f-b72fba9ec5ea_Omnia Crystalline.png', 1),

-- Prada - L'Homme
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Prada L''Homme'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'PRD_LHOMME_50_EDT', 24, 2100000, 3000000, 'a85278d6-0794-4fcf-966c-cabc11bcf7e8_Prada LHomme.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Prada L''Homme'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'PRD_LHOMME_100_EDT', 18, 2800000, 3900000, '6a206580-f3cb-4868-98e2-a8d48be31dd4_Prada LHomme.png', 1),

-- Prada - Candy
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Prada Candy'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'PRD_CANDY_50_EDP', 20, 2000000, 2900000, '42b79463-5c9f-4f18-bddf-34cbc0eb31e0_Prada Candy.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Prada Candy'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'PRD_CANDY_100_EDP', 14, 2700000, 3800000, '3c9a0476-fb92-4de0-ad2f-0eea07e25abe_Prada Candy.png', 1),

-- Calvin Klein - CK One
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'CK One'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'CK_ONE_50_EDT', 40, 900000, 1400000, '6adbf848-e5c2-4f82-8844-d98a68bbe1bd_CK One.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'CK One'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'CK_ONE_100_EDT', 30, 1200000, 1900000, 'f95c93c9-64df-4a21-97e2-da7e9bce37e8_CK One.png', 1),

-- Calvin Klein - Eternity for Men
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Eternity for Men'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'CK_ETERNITY_50_EDT', 28, 1100000, 1700000, 'a358ca87-53f7-4d16-90c1-4000af892cc0_Eternity for Men.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Eternity for Men'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'CK_ETERNITY_100_EDT', 20, 1500000, 2300000, '50aff9a4-152c-4903-9c1d-7ab8792cef25_Eternity for Men.png', 1),

-- Maison Margiela - Replica Jazz Club
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Replica Jazz Club'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'MM_JAZZ_50_EDT', 16, 2400000, 3400000, '2c39fdce-4a0d-45a0-9122-7ecc8b9df72e_Replica Jazz Club.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Replica Jazz Club'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'MM_JAZZ_100_EDT', 10, 3200000, 4400000, '9383cfcc-ac49-42f5-82fd-133c2bd56fa5_Replica Jazz Club.png', 1),

-- Maison Margiela - Replica Lazy Sunday Morning
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Replica Lazy Sunday Morning'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'MM_LSM_50_EDT', 18, 2300000, 3300000, 'bad20d28-2709-48b9-a283-21e97fa61dae_Replica Lazy Sunday Morning.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Replica Lazy Sunday Morning'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'MM_LSM_100_EDT', 12, 3000000, 4200000, '20bf8b02-b47f-484e-83c8-b4afdd31898c_Replica Lazy Sunday Morning.png', 1),

-- Narciso Rodriguez For Her EDP
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Narciso Rodriguez For Her EDP'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'NR_FORHER_50_EDP', 20, 2300000, 3400000, '32ad6ca2-4cf8-43cf-a46a-37e6cebfedb8_Narciso Rodriguez For Her EDP.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Narciso Rodriguez For Her EDP'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'NR_FORHER_100_EDP', 14, 3200000, 4500000, 'fdbc1deb-1563-4dcb-9f5e-50f8d8db7daf_Narciso Rodriguez For Her EDP.png', 1),

-- Narciso Rodriguez For Him Bleu Noir
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Narciso Rodriguez For Him Bleu Noir'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'NR_FORHIM_50_EDT', 18, 2200000, 3200000, '897204c7-350e-419b-ae3b-b696a3292d24_Narciso Rodriguez For Him Bleu Noir.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Narciso Rodriguez For Him Bleu Noir'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDT'),
 'NR_FORHIM_100_EDT', 12, 3000000, 4200000, '405295f3-4d16-4c4a-934b-446eaa06de39_Narciso Rodriguez For Him Bleu Noir.png', 1),
 ((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Roja Elysium Pour Homme'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'ROJA_ELYSIUM_50_EDP', 15, 4200000, 5900000, '425b2a69-0488-46e6-8bf4-8d3b786d18ad_Roja Elysium Pour Homme.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Roja Elysium Pour Homme'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'ROJA_ELYSIUM_100_EDP', 10, 6200000, 8900000, 'be8a74d8-1925-4484-8f43-701a71df999b_Roja Elysium Pour Homme.png', 1),

-- Roja Enigma Parfum Cologne
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Roja Enigma Parfum Cologne'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT50'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'ROJA_ENIGMA_50_EDP', 12, 4500000, 6300000, 'bde77dec-acbc-4b86-be67-2f11092d3a07_Roja Enigma Parfum Cologne.png', 1),
((SELECT Id FROM SanPham WHERE TenNuocHoa = N'Roja Enigma Parfum Cologne'),
 (SELECT Id FROM DungTich WHERE Ma = 'DT100'),
 (SELECT Id FROM NongDo WHERE Ma = 'EDP'),
 'ROJA_ENIGMA_100_EDP', 8, 6800000, 9500000, '705d3a9f-e534-41c9-be0b-d65b6339dc8a_Roja Enigma Parfum Cologne.png', 1);
GO
PRINT N'Đã chèn SanPhamChiTiet mẫu.';
GO

-- Chèn Giảm Giá
INSERT INTO GiamGia
(Ma, Ten, MoTa, LoaiGiam, GiaTri, SoLuong, DonHangToiThieu, GiamToiDa,
 NgayBatDau, NgayKetThuc, TrangThai, PhamViApDung, IdSanPhamChiTiet)
VALUES
-- 1. Giảm 10% cho sản phẩm chi tiết ID 1
('GGSP10', N'Giảm 10% sản phẩm', N'Áp dụng cho sản phẩm ID 1',
 'PERCENT', 10, 100, 0, 50000,
 GETDATE(), DATEADD(DAY, 30, GETDATE()), 1, 'SANPHAM', 1),

-- 2. Giảm 50.000đ cho sản phẩm chi tiết ID 2
('GGSP50K', N'Giảm 50.000đ', N'Áp dụng cho sản phẩm 2',
 'AMOUNT', 50000, 50, 0, NULL,
 GETDATE(), DATEADD(DAY, 20, GETDATE()), 1, 'SANPHAM', 2),

-- 3. Giảm 15% toàn cửa hàng (giảm tối đa 100.000đ)
('GG15ALL', N'Giảm 15% toàn shop', N'Áp dụng toàn cửa hàng',
 'PERCENT', 15, 9999, 200000, 100000,
 GETDATE(), DATEADD(DAY, 40, GETDATE()), 1, 'TOAN_CUA_HANG', NULL),

-- 4. Giảm 30K cho đơn hàng từ 150K
('GG30K', N'Giảm 30K đơn 150K', N'Áp dụng toàn shop',
 'AMOUNT', 30000, 300, 150000, NULL,
 GETDATE(), DATEADD(DAY, 15, GETDATE()), 1, 'TOAN_CUA_HANG', NULL),

-- 5. Giảm 20% cho sản phẩm chi tiết ID 3
('GG20SP', N'Giảm 20% SP', N'Áp dụng SP ID 3',
 'PERCENT', 20, 80, 0, 70000,
 GETDATE(), DATEADD(DAY, 45, GETDATE()), 1, 'SANPHAM', 3),

-- 6. Giảm 100K cho sản phẩm chi tiết ID 4
('GG100K', N'Giảm 100.000đ', N'Áp dụng SP 4',
 'AMOUNT', 100000, 40, 0, NULL,
 GETDATE(), DATEADD(DAY, 25, GETDATE()), 1, 'SANPHAM', 4),

-- 7. Giảm 25% toàn cửa hàng – max 150k, tối thiểu 300k
('GG25ALL', N'Giảm 25% toàn shop', N'Siêu ưu đãi lớn',
 'PERCENT', 25, 500, 300000, 150000,
 GETDATE(), DATEADD(DAY, 60, GETDATE()), 1, 'TOAN_CUA_HANG', NULL),

-- 8. Giảm 40K cho SP ID 5
('GG40K', N'Giảm 40.000đ', N'Chỉ áp dụng SP 5',
 'AMOUNT', 40000, 120, 0, NULL,
 GETDATE(), DATEADD(DAY, 10, GETDATE()), 1, 'SANPHAM', 5),

-- 9. Giảm 5% toàn cửa hàng
('GG5ALL', N'Giảm 5% toàn shop', N'Mã nhỏ dùng nhiều',
 'PERCENT', 5, 9999, 0, NULL,
 GETDATE(), DATEADD(DAY, 90, GETDATE()), 1, 'TOAN_CUA_HANG', NULL),

-- 10. Giảm 70K đơn hàng tối thiểu 400K
('GG70K', N'Giảm 70.000đ', N'Áp dụng toàn shop',
 'AMOUNT', 70000, 200, 400000, NULL,
 GETDATE(), DATEADD(DAY, 50, GETDATE()), 1, 'TOAN_CUA_HANG', NULL);

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

-- Chèn Hóa Đơn mẫu
INSERT INTO HoaDon (IdKH, IdNV, Ma, NgayTao, NgayThanhToan, TenNguoiNhan, DiaChi, Sdt, TrangThai, IdGiamGia, TongTienHang, TienGiamGia, PhiShip, TongThanhToan, IdThanhToan)
VALUES
(
    (SELECT Id FROM NguoiDung WHERE Ma = 'KH001'), -- IdKH
    (SELECT Id FROM NguoiDung WHERE Ma = 'NV001'), -- IdNV
    N'HD001', -- Ma
    '2024-10-25 10:30:00', -- NgayTao
    '2024-10-25 10:35:00', -- NgayThanhToan
    N'Lê Thị Mai', -- TenNguoiNhan
    N'10 Lý Thường Kiệt, Hoàn Kiếm', -- DiaChi
    '0369852147', -- Sdt
    3, -- TinhTrang (Đã hoàn thành)
    (SELECT Id FROM GiamGia WHERE Ma = 'GG100K'), -- IdGiamGia
    3500000, -- TongTienHang
    100000, -- TienGiamGia
    30000, -- PhiShip
    3430000, -- TongThanhToan (3.500.000 - 100.000 + 30.000)
    (SELECT Id FROM ThanhToan WHERE HinhThucThanhToan LIKE N'%COD%') -- IdThanhToan
);
GO
PRINT N'Đã chèn HoaDon mẫu.';
GO

-- Chèn Hóa Đơn Chi Tiết mẫu
INSERT INTO HoaDonChiTiet (IdHoaDon, IdSanPhamChiTiet, SoLuong, DonGia)
VALUES
(
    (SELECT Id FROM HoaDon WHERE Ma = 'HD001'), -- IdHoaDon
    (SELECT Id FROM SanPhamChiTiet WHERE MaSKU = 'CHA_BLEU_100_EDP'), -- IdSanPhamChiTiet (Bleu de Chanel)
    1, -- SoLuong
    3500000 -- DonGia (Đóng băng giá)
);
GO
PRINT N'Đã chèn HoaDonChiTiet mẫu.';
GO
-- ======================================================



PRINT 'Da chen xong toan bo du lieu mau.';
GO

-- =================================================================================
-- PHẦN 4: KIỂM TRA DỮ LIỆU
-- =================================================================================
PRINT 'Hoan tat script. Kiem tra du lieu:';
GO


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
SELECT * FROM NguoiDung;
SELECT * FROM GioHang;
SELECT * FROM GioHangChiTiet;
GO
