document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;

    /** ==============================
     * 1️⃣ — THÊM SẢN PHẨM VÀO GIỎ
     * ============================== */
    const addToCartForm = document.getElementById("addToCartForm");
    if (addToCartForm) {
        addToCartForm.addEventListener("submit", function (e) {
            e.preventDefault();
            const id = document.getElementById("selectedSanPhamChiTietId").value;
            const qty = document.getElementById("quantityInput").value;
            if (!id) {
                showToast("Cảnh báo", "Vui lòng chọn dung tích!", "warning");
                return;
            }
            const headers = { "Content-Type": "application/x-www-form-urlencoded" };
            if (csrfToken) headers[csrfHeader] = csrfToken;

            fetch("/cart/add", {
                method: "POST",
                headers,
                body: new URLSearchParams({ idSanPhamChiTiet: id, soLuong: qty })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        updateCartIcon(data.cartSize);
                        showToast("Thành công", data.message, "success");
                    } else {
                        showToast("Lỗi", data.message, "danger");
                    }
                })
                .catch(err => showToast("Lỗi", err.message, "danger"));
        });
    }

    /** ==============================
     * 2️⃣ — XÓA SẢN PHẨM KHỎI GIỎ
     * ============================== */
    document.querySelectorAll(".remove-item-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const itemRow = this.closest("tr");
            const id = this.getAttribute("data-item-id");

            const headers = { "Content-Type": "application/x-www-form-urlencoded" };
            if (csrfToken) headers[csrfHeader] = csrfToken;

            fetch("/cart/remove", {
                method: "POST",
                headers,
                body: new URLSearchParams({ idSanPhamChiTiet: id })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        itemRow.remove();
                        updateTotals();
                        showToast("Thành công", data.message, "success");
                    } else {
                        showToast("Lỗi", data.message, "danger");
                    }
                })
                .catch(err => showToast("Lỗi", err.message, "danger"));
        });
    });

    /** ==============================
     * 3️⃣ — CẬP NHẬT SỐ LƯỢNG SẢN PHẨM
     * ============================== */
    document.querySelectorAll(".quantity-input").forEach(input => {
        input.addEventListener("change", function () {
            const id = this.getAttribute("data-item-id");
            const newQty = parseInt(this.value);
            if (isNaN(newQty) || newQty <= 0) {
                showToast("Cảnh báo", "Số lượng không hợp lệ!", "warning");
                return;
            }

            const headers = { "Content-Type": "application/x-www-form-urlencoded" };
            if (csrfToken) headers[csrfHeader] = csrfToken;

            fetch("/cart/update", {
                method: "POST",
                headers,
                body: new URLSearchParams({ idSanPhamChiTiet: id, soLuong: newQty })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        updateRowTotal(this);
                        updateTotals();
                        showToast("Thành công", data.message, "success");
                    } else {
                        showToast("Lỗi", data.message, "danger");
                    }
                })
                .catch(err => showToast("Lỗi", err.message, "danger"));
        });
    });

    /** ==============================
     * 4️⃣ — TÍNH LẠI GIÁ SAU KHI CẬP NHẬT
     * ============================== */
    function updateRowTotal(inputEl) {
        const row = inputEl.closest("tr");
        const priceText = row.querySelector("td:nth-child(3)").textContent.replace(/[^\d]/g, "");
        const price = parseFloat(priceText);
        const qty = parseInt(inputEl.value);
        const subtotalCell = row.querySelector("td:nth-child(5)");
        const total = price * qty;
        subtotalCell.textContent = total.toLocaleString("vi-VN") + " ₫";
    }

    function updateTotals() {
        const rows = document.querySelectorAll("tbody tr");
        let subtotal = 0;
        rows.forEach(row => {
            const subtotalCell = row.querySelector("td:nth-child(5)");
            if (subtotalCell) {
                subtotal += parseFloat(subtotalCell.textContent.replace(/[^\d]/g, "")) || 0;
            }
        });
        document.getElementById("cart-subtotal").textContent = subtotal.toLocaleString("vi-VN") + " ₫";
        document.getElementById("cart-total").textContent = subtotal.toLocaleString("vi-VN") + " ₫";
        document.getElementById("cart-discount").textContent = "- 0 ₫";
    }

    /** ==============================
     * 5️⃣ — TOAST THÔNG BÁO
     * ============================== */
    function showToast(title, message, type = "info") {
        const toastEl = document.getElementById("liveToast");
        if (!toastEl) {
            alert(message);
            return;
        }
        document.getElementById("toast-title").textContent = title;
        document.getElementById("toast-body").textContent = message;
        toastEl.className = `toast text-bg-${type}`;
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
    }

    /** ==============================
     * 6️⃣ — CẬP NHẬT ICON GIỎ HÀNG
     * ============================== */
    function updateCartIcon(count) {
        const el = document.getElementById("cart-item-count");
        if (el) el.textContent = count;
    }
});
