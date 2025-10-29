package com.shop.fperfume.repository; // Đảm bảo đúng đường dẫn package

import com.shop.fperfume.entity.SanPhamChiTiet; // Đảm bảo đúng đường dẫn entity
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Có thể thêm @Repository

import java.util.List;
import java.util.Optional;

@Repository // Annotation này không bắt buộc nhưng nên có
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Long> {

    /**
     * Tìm SanPhamChiTiet theo ID và lấy kèm (fetch) các entity liên quan.
     * Sử dụng phương thức này khi bạn cần dữ liệu đầy đủ cho Response DTO.
     *
     * @param id ID của SanPhamChiTiet cần tìm.
     * @return Optional chứa SanPhamChiTiet với các mối quan hệ đã fetch, hoặc rỗng nếu không tìm thấy.
     */
    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +        // Lấy thông tin SanPham
            "LEFT JOIN FETCH spct.dungTich dt " +       // Lấy thông tin DungTich
            "LEFT JOIN FETCH spct.nongDo nd " +         // Lấy thông tin NongDo
            "WHERE spct.id = :id")
    Optional<SanPhamChiTiet> findByIdFetchingRelationships(@Param("id") Long id);

    /**
     * Lấy tất cả SanPhamChiTiet và fetch các entity liên quan.
     * Sử dụng khi cần dữ liệu đầy đủ cho danh sách Response DTO.
     * Cẩn thận khi dùng với lượng dữ liệu lớn.
     *
     * @return List các SanPhamChiTiet với các mối quan hệ đã fetch.
     */
    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt " +
            "LEFT JOIN FETCH spct.nongDo nd ")
    List<SanPhamChiTiet> findAllFetchingRelationships();

    /**
     * Lấy một trang (Page) SanPhamChiTiet và fetch các entity liên quan.
     * Dùng cho phân trang khi cần dữ liệu đầy đủ cho trang Response DTO.
     *
     * @param pageable Thông tin phân trang.
     * @return Page chứa các SanPhamChiTiet với các mối quan hệ đã fetch.
     */
    @Query(value = "SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt " +
            "LEFT JOIN FETCH spct.nongDo nd ",
            countQuery = "SELECT COUNT(spct) FROM SanPhamChiTiet spct") // Cần countQuery riêng cho phân trang
    Page<SanPhamChiTiet> findAllFetchingRelationships(Pageable pageable);

    /**
     * Lấy danh sách chi tiết sản phẩm theo ID sản phẩm gốc, fetch các entity liên quan.
     * Hữu ích khi hiển thị tất cả các biến thể của một sản phẩm.
     *
     * @param sanPhamId ID của SanPham gốc.
     * @return List các SanPhamChiTiet liên quan với các mối quan hệ đã fetch.
     */
    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH spct.dungTich dt " +
            "LEFT JOIN FETCH spct.nongDo nd " +
            "WHERE sp.id = :sanPhamId")
    List<SanPhamChiTiet> findBySanPhamIdFetchingRelationships(@Param("sanPhamId") Long sanPhamId);

    // Bạn có thể thêm các phương thức truy vấn tùy chỉnh khác ở đây nếu cần.

    Optional<SanPhamChiTiet> findByMaSKU(String maSKU);

    @Query("""
    SELECT DISTINCT spct FROM SanPhamChiTiet spct
    LEFT JOIN FETCH spct.sanPham sp
    LEFT JOIN FETCH sp.thuongHieu th
    LEFT JOIN FETCH spct.dungTich dt
    LEFT JOIN FETCH spct.nongDo nd
    ORDER BY spct.ngayTao DESC
""")
    List<SanPhamChiTiet> findTop5NewestWithRelationships(org.springframework.data.domain.Pageable pageable);


    @Query("""
    SELECT spct FROM SanPhamChiTiet spct
    JOIN FETCH spct.sanPham sp
    LEFT JOIN FETCH sp.thuongHieu th
    WHERE spct.id IN (
        SELECT MIN(ct2.id)
        FROM SanPhamChiTiet ct2
        GROUP BY ct2.sanPham.id
    )
""")
    List<SanPhamChiTiet> findDistinctBySanPham(org.springframework.data.domain.Pageable pageable);

}