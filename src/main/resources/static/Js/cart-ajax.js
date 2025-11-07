document.addEventListener("DOMContentLoaded", function () {
    // Lấy CSRF token
    const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;
    const headers = { "Content-Type": "application/x-www-form-urlencoded" };
    if (csrfToken) headers[csrfHeader] = csrfToken;

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
                        showNotification("Thành công", data.message, "success", true); // Toast
                    } else {
                        showNotification("Lỗi", data.message, "error"); // Popup
                    }
                })
                .catch(err => showNotification("Lỗi hệ thống", err.message, "error"))
                .finally(() => {
                    if (button) button.disabled = false;
                });
        });
    }

    /** ==============================
     * 2️⃣ — XÓA SẢN PHẨM (Chỉ dùng SweetAlert2)
     * (Đã gộp 2.0, 2.5, 2.6 - Sửa lỗi treo trang + double-click)
     * ============================== */
    document.querySelectorAll(".remove-item-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const id = this.getAttribute("data-item-id");
            const itemRow = this.closest("tr"); // Lưu lại hàng <tr> để xóa

            // 1. Dùng SweetAlert để HỎI
            Swal.fire({
                title: 'Xác nhận xóa?',
                text: "Bạn có chắc muốn xóa sản phẩm này?",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#6e7881',
                confirmButtonText: 'OK, Xóa!',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    // 2. Người dùng đã bấm "OK", gửi request
                    fetch("/cart/remove", {
                        method: "POST",
                        headers,
                        body: new URLSearchParams({ idSanPhamChiTiet: id })
                    })
                    .then(res => res.json())
                    .then(data => {
                        if (data.success) {
                            // 3. THÀNH CÔNG (Không tải lại trang)
                            itemRow.remove(); // Xóa hàng khỏi giao diện
                            updateCartSummary(data.cartSummary);
                            updateCartIcon(data.cartSize);

                            // 4. Dùng SweetAlert (Toast) để BÁO THÀNH CÔNG
                            showNotification(data.message, "", "success", true);

                        } else {
                            // 5. THẤT BẠI (Báo lỗi, ví dụ: double-click)
                            showNotification("Lỗi", data.message, "error");
                        }
                    })
                    .catch(err => {
                        showNotification("Lỗi hệ thống", err.message, "error");
                    });
                }
            });
        });
    });


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

    // Hàm showNotification bây giờ chỉ dùng SweetAlert2
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
                title: title // Dùng title cho toast
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