// Đợi tài liệu HTML tải xong
document.addEventListener("DOMContentLoaded", function() {

    // === LOGIC 1: XỬ LÝ NÚT "THÊM VÀO GIỎ HÀNG" (TRÊN TRANG PRODUCT DETAIL) ===
    const addToCartForm = document.getElementById("addToCartForm");

    if (addToCartForm) {
        addToCartForm.addEventListener("submit", function(event) {
            // Ngăn form gửi theo cách truyền thống
            event.preventDefault();

            // Lấy ID sản phẩm chi tiết từ radio button được chọn
            // (Dựa trên product.css, bạn dùng radio .dung-tich-radio)
            const selectedRadio = document.querySelector(".dung-tich-radio:checked");
            if (!selectedRadio) {
                alert("Vui lòng chọn dung tích.");
                return;
            }
            const idSanPhamChiTiet = selectedRadio.value;

            // Lấy số lượng
            const soLuong = document.getElementById("quantityInput").value;

            // Gọi hàm AJAX để thêm
            ajaxAddToCart(idSanPhamChiTiet, soLuong);
        });
    }

    // === LOGIC 2: XỬ LÝ CẬP NHẬT/XÓA TRÊN TRANG GIỎ HÀNG (SẼ LÀM SAU) ===
    // (Chúng ta sẽ thêm logic cho các nút +/- và nút Xóa ở đây)

});

/**
 * Hàm AJAX gửi yêu cầu THÊM SẢN PHẨM
 * (Gọi đến POST /cart/add của GioHangController)
 */
function ajaxAddToCart(idSanPhamChiTiet, soLuong) {
    // Lấy CSRF token (nếu bạn dùng Spring Security)
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [header]: token // Thêm header CSRF
        },
        // Gửi dữ liệu dưới dạng form
        body: new URLSearchParams({
            'idSanPhamChiTiet': idSanPhamChiTiet,
            'soLuong': soLuong
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Cập nhật icon số lượng giỏ hàng trên header
            updateCartIcon(data.cartSize);
            // Thông báo thành công (bạn có thể dùng modal/toast đẹp hơn)
            alert(data.message);
        } else {
            // Thông báo lỗi (ví dụ: "Hết hàng")
            alert(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("Đã xảy ra lỗi. Vui lòng thử lại.");
    });
}

/**
 * Hàm cập nhật số lượng trên icon giỏ hàng (trên header)
 */
function updateCartIcon(cartSize) {
    const cartIcon = document.getElementById("cart-item-count"); // Giả sử ID của span là đây
    if (cartIcon) {
        cartIcon.textContent = cartSize;
    }
}