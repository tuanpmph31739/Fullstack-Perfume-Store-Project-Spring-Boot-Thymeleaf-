document.addEventListener("DOMContentLoaded", function () {
    // Lấy CSRF token
    const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;
    const headers = { "Content-Type": "application/x-www-form-urlencoded" };
    if (csrfToken) headers[csrfHeader] = csrfToken;

    // Các phần tử Modal
    const confirmDeleteModalEl = document.getElementById('confirmDeleteModal');
    const confirmDeleteModal = confirmDeleteModalEl ? new bootstrap.Modal(confirmDeleteModalEl) : null;
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');

    // BIẾN TẠM ĐỂ GIẢI QUYẾT XUNG ĐỘT MODAL
    let itemRowToDelete = null;
    let modalResponseData = null; // Sẽ lưu trữ phản hồi từ server (lỗi hoặc thành công)

    /** ==============================
     * 1️⃣ — THÊM SẢN PHẨM VÀO GIỎ
     * (Chống double-click)
     * ============================== */
    const addToCartForm = document.getElementById("addToCartForm");
    if (addToCartForm) {
        addToCartForm.addEventListener("submit", function (e) {
            e.preventDefault();
            const button = this.querySelector("button[type='submit']");
            if (button) button.disabled = true;

            const id = document.getElementById("selectedSanPhamChiTietId").value;
            const qty = document.getElementById("quantityInput").value;
            if (!id) {
                showNotification("Cảnh báo", "Vui lòng chọn dung tích!", "warning");
                if (button) button.disabled = false;
                return;
            }

            fetch("/cart/add", {
                method: "POST",
                headers,
                body: new URLSearchParams({ idSanPhamChiTiet: id, soLuong: qty })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        updateCartIcon(data.cartSize);
                        showNotification("Thành công", data.message, "success", true); // Toast không gây xung đột
                    } else {
                        showNotification("Lỗi", data.message, "error");
                    }
                })
                .catch(err => showNotification("Lỗi hệ thống", err.message, "error"))
                .finally(() => {
                    if (button) button.disabled = false;
                });
        });
    }

    /** ==============================
     * 2️⃣ — KÍCH HOẠT MODAL XÁC NHẬN XÓA
     * ============================== */
    document.querySelectorAll(".remove-item-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            itemRowToDelete = this.closest("tr");
            const id = this.getAttribute("data-item-id");
            confirmDeleteBtn.dataset.itemId = id;

            // Dọn dẹp biến tạm trước khi mở
            modalResponseData = null;

            if (confirmDeleteModal) confirmDeleteModal.show();
        });
    });

    /** ==============================
     * 2.5️⃣ — XÁC NHẬN XÓA (GỌI API)
     * (FIX LỖI TREO: Chỉ lưu kết quả và đóng modal)
     * ============================== */
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", function () {
            this.disabled = true; // Chống double-click
            const id = this.dataset.itemId;

            fetch("/cart/remove", {
                method: "POST",
                headers,
                body: new URLSearchParams({ idSanPhamChiTiet: id })
            })
                .then(res => res.json())
                .then(data => {
                    modalResponseData = data; // <-- LƯU KẾT QUẢ VÀO BIẾN TẠM
                })
                .catch(err => {
                    modalResponseData = { success: false, message: err.message }; // LƯU LỖI VÀO BIẾN TẠM
                })
                .finally(() => {
                    if (confirmDeleteModal) confirmDeleteModal.hide(); // <-- CHỈ ĐÓNG MODAL
                    this.disabled = false; // Kích hoạt lại nút
                });
        });
    }

    /** ==============================
     * 2.6️⃣ — XỬ LÝ SAU KHI MODAL XÓA ĐÃ ĐÓNG HOÀN TOÀN (FIX LỖI TREO)
     * ============================== */
    if (confirmDeleteModalEl) {
        // Lắng nghe sự kiện "đã đóng xong" của Bootstrap
        confirmDeleteModalEl.addEventListener('hidden.bs.modal', function () {
            // Chỉ chạy khi có dữ liệu phản hồi từ server
            if (modalResponseData) {
                if (modalResponseData.success) {
                    // Nếu thành công:
                    if (itemRowToDelete) {
                        itemRowToDelete.remove(); // Xóa hàng
                    }
                    updateCartSummary(modalResponseData.cartSummary);
                    updateCartIcon(modalResponseData.cartSize);
                    showNotification("Thành công", modalResponseData.message, "success", true); // HIỂN THỊ TOAST (an toàn)
                } else {
                    // Nếu thất bại:
                    showNotification("Lỗi", modalResponseData.message, "error"); // HIỂN THỊ POPUP LỖI (an toàn)
                }
            }

            // Dọn dẹp biến tạm sau khi đã xử lý
            itemRowToDelete = null;
            modalResponseData = null;
        });
    }


    /** ==============================
     * 3️⃣ — CẬP NHẬT SỐ LƯỢNG SẢN PHẨM
     * ============================== */
    document.querySelectorAll(".quantity-input").forEach(input => {
        input.addEventListener("change", function () {
            this.disabled = true;
            const id = this.getAttribute("data-item-id");
            const newQty = parseInt(this.value);

            if (isNaN(newQty) || newQty <= 0) {
                showNotification("Cảnh báo", "Số lượng không hợp lệ!", "warning");
                this.disabled = false;
                return;
            }

            fetch("/cart/update", {
                method: "POST",
                headers,
                body: new URLSearchParams({ idSanPhamChiTiet: id, soLuong: newQty })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        updateRowTotal(this);
                        updateCartSummary(data.cartSummary);
                        updateCartIcon(data.cartSize);
                    } else {
                        if(data.newValidQuantity) {
                            this.value = data.newValidQuantity;
                            updateRowTotal(this);
                            updateCartSummary(data.cartSummary);
                        }
                        showNotification("Lỗi", data.message, "error");
                    }
                })
                .catch(err => showNotification("Lỗi hệ thống", err.message, "error"))
                .finally(() => {
                    this.disabled = false;
                });
        });
    });

    /** ==============================
     * 4️⃣ & 5️⃣ — VOUCHER (AJAX Tải lại trang)
     * ============================== */
    const voucherForm = document.getElementById("voucher-form");
    if(voucherForm) {
        voucherForm.addEventListener("submit", function(e) {
            e.preventDefault();
            const button = this.querySelector("button[type='submit']");
            if (button) button.disabled = true;

            const formData = new URLSearchParams(new FormData(this));

            fetch("/cart/apply-voucher", { method: "POST", headers, body: formData })
                .then(res => {
                    if(res.ok) {
                         location.reload(); // Tải lại trang là cách dễ nhất cho voucher
                    } else {
                        res.json().then(data => {
                            showNotification("Lỗi", data.message || "Không thể áp dụng mã", "error");
                            if (button) button.disabled = false;
                        });
                    }
                })
                .catch(err => {
                    showNotification("Lỗi hệ thống", err.message, "error");
                    if (button) button.disabled = false;
                });
        });
    }

     const removeVoucherForm = document.getElementById("remove-voucher-form");
     if(removeVoucherForm) {
        removeVoucherForm.addEventListener("submit", function(e) {
             e.preventDefault();
             const button = this.querySelector("button[type='submit']");
             if (button) button.disabled = true;

             fetch("/cart/remove-voucher", { method: "POST", headers })
                .then(res => {
                    if(res.ok) {
                        location.reload();
                    } else {
                        res.json().then(data => {
                            showNotification("Lỗi", data.message || "Không thể xóa mã", "error");
                            if (button) button.disabled = false;
                        });
                    }
                })
                .catch(err => {
                    showNotification("Lỗi hệ thống", err.message, "error");
                    if (button) button.disabled = false;
                });
         });
     }

    /** ==============================
     * CÁC HÀM TIỆN ÍCH (HELPERS)
     * ============================== */

    function formatCurrency(number) {
        if (isNaN(number)) number = 0;
        return number.toLocaleString("vi-VN") + " ₫";
    }

    function updateRowTotal(inputEl) {
        const row = inputEl.closest("tr");
        const priceText = row.querySelector(".item-price")?.textContent.replace(/[^\d]/g, "");
        const price = parseFloat(priceText);
        const qty = parseInt(inputEl.value);
        const subtotalCell = row.querySelector(".item-row-total");

        if(subtotalCell) {
            subtotalCell.textContent = formatCurrency(price * qty);
        }
    }

    function updateCartSummary(summaryData) {
        if (!summaryData) return;

        const subtotalEl = document.getElementById("cart-subtotal");
        const discountEl = document.getElementById("cart-discount");
        const totalEl = document.getElementById("cart-total");

        if(subtotalEl) subtotalEl.textContent = formatCurrency(summaryData.subtotal);
        if(discountEl) discountEl.textContent = "- " + formatCurrency(summaryData.discount);
        if(totalEl) totalEl.textContent = formatCurrency(summaryData.total);
    }

    function showNotification(title, message, iconType = "info", isToast = false) {
        if (isToast) {
            const Toast = Swal.mixin({
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 3000,
                timerProgressBar: true,
                didOpen: (toast) => {
                    toast.addEventListener('mouseenter', Swal.stopTimer)
                    toast.addEventListener('mouseleave', Swal.resumeTimer)
                }
            });
            Toast.fire({
                icon: iconType,
                title: message
            });
        } else {
            Swal.fire(title, message, iconType);
        }
    }

    function updateCartIcon(count) {
        const el = document.getElementById("cart-item-count");
        if (el) el.textContent = count;
    }
});