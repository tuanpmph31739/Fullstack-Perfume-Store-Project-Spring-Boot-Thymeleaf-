/**
 * File JavaScript (AJAX) xử lý Giỏ Hàng
 * (Sử dụng Bootstrap Modal & Toast cho thông báo chuyên nghiệp)
 */

document.addEventListener("DOMContentLoaded", function() {

    // === KHỞI TẠO CÁC ĐỐI TƯỢNG BOOTSTRAP ===
    const toastLiveExample = document.getElementById('liveToast');
    const toastBootstrap = toastLiveExample ? new bootstrap.Toast(toastLiveExample) : null;
    const toastTitleEl = document.getElementById('toast-title');
    const toastBodyEl = document.getElementById('toast-body');

    const confirmDeleteModalEl = document.getElementById('confirmDeleteModal');
    const confirmDeleteModal = confirmDeleteModalEl ? new bootstrap.Modal(confirmDeleteModalEl) : null;
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');

    let idToDelete = null; // Biến tạm để lưu ID cần xóa

    // === LOGIC 1: TRANG PRODUCT DETAIL (Thêm vào giỏ) ===
    const addToCartForm = document.getElementById("addToCartForm");
    if (addToCartForm) {
        addToCartForm.addEventListener("submit", function(event) {
            event.preventDefault();
            const idSanPhamChiTiet = document.getElementById("selectedSanPhamChiTietId").value;
            const soLuong = document.getElementById("quantityInput").value;

            if (!idSanPhamChiTiet) {
                showToast("Lỗi", "Vui lòng chọn một dung tích."); // Sửa: Dùng Toast
                return;
            }
            ajaxAddToCart(idSanPhamChiTiet, soLuong);
        });
    }

    // === LOGIC 2: TRANG GIỎ HÀNG (Cập nhật / Xóa) ===

    // Bắt sự kiện click vào nút Xóa (class .remove-item-btn)
    document.querySelectorAll('.remove-item-btn').forEach(button => {
        button.addEventListener('click', function() {
            idToDelete = this.getAttribute('data-item-id');
            // Mở Modal xác nhận (thay vì confirm())
            if(confirmDeleteModal) {
                confirmDeleteModal.show();
            } else {
                // Fallback (nếu modal chưa tải)
                if (confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
                    ajaxRemoveFromCart(idToDelete);
                }
            }
        });
    });

    // Bắt sự kiện click vào nút "Xóa" TRONG MODAL
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function() {
            if (idToDelete) {
                ajaxRemoveFromCart(idToDelete);
                confirmDeleteModal.hide();
                idToDelete = null;
            }
        });
    }

    // Bắt sự kiện 'change' (thay đổi giá trị) của ô Số Lượng
    document.querySelectorAll('.quantity-input').forEach(input => {
        input.addEventListener('change', function() {
            const idSanPhamChiTiet = this.getAttribute('data-item-id');
            const soLuong = this.value;
            ajaxUpdateQuantity(idSanPhamChiTiet, soLuong);
        });
    });

    // === HÀM HIỂN THỊ TOAST ===
    function showToast(title, message) {
        if (!toastBootstrap) {
             // Fallback nếu toast không tồn tại
            alert(title + ": " + message);
            return;
        }

        if (toastTitleEl) toastTitleEl.textContent = title;
        if (toastBodyEl) toastBodyEl.textContent = message;

        toastBootstrap.show();
    }


    /**
     * 1. HÀM AJAX: THÊM SẢN PHẨM
     */
    function ajaxAddToCart(idSanPhamChiTiet, soLuong) {
        const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };

        fetch('/cart/add', {
            method: 'POST',
            headers: headers,
            body: new URLSearchParams({
                'idSanPhamChiTiet': idSanPhamChiTiet,
                'soLuong': soLuong
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateCartIcon(data.cartSize);
                // === THAY ĐỔI Ở ĐÂY ===
                showToast("Thành công!", data.message);
            } else {
                showToast("Lỗi!", data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast("Lỗi Hệ Thống", "Đã xảy ra lỗi. Vui lòng thử lại.");
        });
    }

    /**
     * 2. HÀM AJAX: XÓA SẢN PHẨM
     */
    function ajaxRemoveFromCart(idSanPhamChiTiet) {
        const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };

        fetch('/cart/remove', {
            method: 'POST',
            headers: headers,
            body: new URLSearchParams({ 'idSanPhamChiTiet': idSanPhamChiTiet })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                window.location.reload();
            } else {
                showToast("Lỗi!", data.message);
            }
        })
        .catch(error => console.error('Error:', error));
    }

    /**
     * 3. HÀM AJAX: CẬP NHẬT SỐ LƯỢNG
     */
    function ajaxUpdateQuantity(idSanPhamChiTiet, soLuong) {
        const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };

        fetch('/cart/update', {
            method: 'POST',
            headers: headers,
            body: new URLSearchParams({
                'idSanPhamChiTiet': idSanPhamChiTiet,
                'soLuong': soLuong
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                window.location.reload();
            } else {
                showToast("Lỗi!", data.message);
                window.location.reload();
            }
        })
        .catch(error => console.error('Error:', error));
    }

    /**
     * HÀM TIỆN ÍCH: Cập nhật icon giỏ hàng
     */
    function updateCartIcon(cartSize) {
        const cartIcon = document.getElementById("cart-item-count");
        if (cartIcon) {
            cartIcon.textContent = cartSize;
        }
    }
});