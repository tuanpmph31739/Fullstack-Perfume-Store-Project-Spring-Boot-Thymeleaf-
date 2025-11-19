document.addEventListener("DOMContentLoaded", function () {
    // Lấy CSRF token
    const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;
    const headers = { "Content-Type": "application/x-www-form-urlencoded" };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    /** ==============================
     * 1️⃣ — THÊM SẢN PHẨM VÀO GIỎ + MUA NGAY
     * ============================== */
    const addToCartForm = document.getElementById("addToCartForm");
    const redirectCheckoutInput = document.getElementById("redirectCheckout"); // hidden input
    const buyNowButton = document.getElementById("buyNowButton");              // nút MUA NGAY

    // Click MUA NGAY
    if (buyNowButton && redirectCheckoutInput && addToCartForm) {
        buyNowButton.addEventListener("click", function (e) {
            e.preventDefault();
            // bật cờ redirect
            redirectCheckoutInput.value = "true";
            // submit form (gửi /cart/add như bình thường)
            addToCartForm.requestSubmit();
        });
    }

    if (addToCartForm) {
        addToCartForm.addEventListener("submit", function (e) {
            e.preventDefault();

            const button = this.querySelector("button[type='submit']");
            if (button) button.disabled = true;

            const id = document.getElementById("selectedSanPhamChiTietId").value;
            const qtyInput = document.getElementById("quantityInput");
            const qty = parseInt(qtyInput.value);
            const maxStock = parseInt(qtyInput.max); // tồn kho từ thuộc tính 'max'

            if (!id) {
                showNotification("Cảnh báo", "Vui lòng chọn dung tích!", "warning");
                if (button) button.disabled = false;
                return;
            }

            // Kiểm tra số lượng client-side
            if (isNaN(qty) || qty <= 0) {
                showNotification("Cảnh báo", "Số lượng phải lớn hơn 0!", "warning");
                if (button) button.disabled = false;
                return;
            }
            if (!isNaN(maxStock) && qty > maxStock) {
                showNotification("Lỗi", "Số lượng vượt quá tồn kho! (Tồn kho: " + maxStock + ")", "error");
                if (button) button.disabled = false;
                return;
            }

            // Cờ xem có phải MUA NGAY không
            const shouldRedirect =
                redirectCheckoutInput && redirectCheckoutInput.value === "true";

            fetch("/cart/add", {
                method: "POST",
                headers,
                body: new URLSearchParams({
                    idSanPhamChiTiet: id,
                    soLuong: qty
                })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        if (shouldRedirect) {
                            // ✅ Mua ngay: nhảy thẳng sang checkout
                            window.location.href = "/order/checkout";
                        } else {
                            // ✅ Thêm vào giỏ bình thường
                            updateCartIcon(data.cartSize);
                            showNotification("Thành công", data.message, "success", true);
                        }
                    } else {
                        showNotification("Lỗi", data.message, "error");
                    }
                })
                .catch(err => {
                    showNotification("Lỗi hệ thống", err.message, "error");
                })
                .finally(() => {
                    if (button) button.disabled = false;
                    // reset cờ để lần sau bấm "Thêm vào giỏ" không bị redirect
                    if (redirectCheckoutInput) redirectCheckoutInput.value = "false";
                });
        });
    }

    /** ==============================
     * 2️⃣ — XÓA SẢN PHẨM (Giữ nguyên từ đây trở xuống)
     * ============================== */
    // ... giữ nguyên toàn bộ code còn lại của bạn


    /** ==============================
     * 2️⃣ — XÓA SẢN PHẨM (Giữ nguyên)
     * ============================== */
    document.querySelectorAll(".remove-item-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const id = this.getAttribute("data-item-id");
            const itemRow = this.closest("tr");

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
                    fetch("/cart/remove", {
                        method: "POST",
                        headers,
                        body: new URLSearchParams({ idSanPhamChiTiet: id })
                    })
                    .then(res => res.json())
                    .then(data => {
                        if (data.success) {
                            itemRow.remove();
                            updateCartSummary(data.cartSummary);
                            updateCartIcon(data.cartSize);
                            showNotification(data.message, "", "success", true);
                        } else {
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
     * (SỬA: Thêm kiểm tra tồn kho)
     * ============================== */
    document.querySelectorAll(".quantity-input").forEach(input => {
        input.addEventListener("change", function () {
            this.disabled = true;
            const id = this.getAttribute("data-item-id");
            const newQty = parseInt(this.value);

            // THÊM: Lấy tồn kho từ thuộc tính 'max' của input
            const maxStock = parseInt(this.max);

            if (isNaN(newQty) || newQty <= 0) {
                showNotification("Cảnh báo", "Số lượng không hợp lệ!", "warning");
                this.disabled = false;
                // Cân nhắc trả về giá trị cũ
                // this.value = this.defaultValue;
                return;
            }

            // THÊM: Kiểm tra tồn kho ngay tại client
            if (newQty > maxStock) {
                showNotification("Lỗi", "Số lượng vượt quá tồn kho! (Tồn kho: " + maxStock + ")", "error");
                this.disabled = false;
                // Trả về giá trị max
                this.value = maxStock;
                return; // Dừng, không gửi request
            }
            // --- HẾT PHẦN THÊM ---

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
                        // Logic này của bạn đã đúng, để xử lý lỗi từ server
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
     * 4️⃣ & 5️⃣ — VOUCHER (Giữ nguyên)
     * (Logic của bạn đã chính xác)
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
                         location.reload();
                    } else {
                        res.json().then(data => {
                            showNotification("Vui lòng đăng nhập", data.message || "Không thể áp dụng mã", "alert");
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
     * (Giữ nguyên)
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
                title: title
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