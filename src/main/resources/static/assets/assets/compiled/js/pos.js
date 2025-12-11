document.addEventListener("DOMContentLoaded", function () {
    // ===========================
    // 1. Mở lại modal khách hàng
    // ===========================
    const customerModalEl = document.getElementById("customerModal");
    if (customerModalEl && customerModalEl.dataset.searchTriggered === "true") {
        const modal = new bootstrap.Modal(customerModalEl);
        modal.show();
    }

    // ===========================
    // 2. Lọc sản phẩm theo text
    // ===========================
    window.filterProducts = function () {
        const input = document.getElementById("productSearchInput");
        if (!input) return;
        const keyword = input.value.toLowerCase();
        const items = document.querySelectorAll(".product-item");

        items.forEach(function (item) {
            const text = (item.getAttribute("data-keywords") || "").toLowerCase();
            if (!keyword || text.indexOf(keyword) !== -1) {
                item.classList.remove("d-none");
            } else {
                item.classList.add("d-none");
            }
        });
    };

    // ===========================
    // 3. POS draft + AJAX form
    // ===========================
    const posRoot = document.getElementById("posRoot");
    if (posRoot) {
        const STORAGE_PREFIX = "POS_DRAFT_";

        function getCurrentOrderId() {
            const id = posRoot.getAttribute("data-order-id");
            if (!id || id === "null" || id === "undefined") return null;
            return id;
        }

        function readCustomerInputs() {
            const hoTenInput  = document.getElementById("posHoTen");
            const sdtInput    = document.getElementById("posSdt");
            const emailInput  = document.getElementById("posEmail");
            const diaChiInput = document.getElementById("posDiaChi");

            return {
                hoTen:  hoTenInput  ? hoTenInput.value  : "",
                sdt:    sdtInput    ? sdtInput.value    : "",
                email:  emailInput  ? emailInput.value  : "",
                diaChi: diaChiInput ? diaChiInput.value : ""
            };
        }

        function applyCustomerInputs(data) {
            if (!data) return;
            const hoTenInput  = document.getElementById("posHoTen");
            const sdtInput    = document.getElementById("posSdt");
            const emailInput  = document.getElementById("posEmail");
            const diaChiInput = document.getElementById("posDiaChi");

            if (hoTenInput)  hoTenInput.value  = data.hoTen  ?? "";
            if (sdtInput)    sdtInput.value    = data.sdt    ?? "";
            if (emailInput)  emailInput.value  = data.email  ?? "";
            if (diaChiInput) diaChiInput.value = data.diaChi ?? "";
        }

        function saveDraftForCurrentOrder() {
            const orderId = getCurrentOrderId();
            if (!orderId) return null;
            const data = readCustomerInputs();
            try {
                localStorage.setItem(STORAGE_PREFIX + orderId, JSON.stringify(data));
            } catch (e) {
                console.warn("Không lưu được draft POS:", e);
            }
            return data;
        }

        function loadDraft(orderId) {
            if (!orderId) return null;
            try {
                const raw = localStorage.getItem(STORAGE_PREFIX + orderId);
                if (!raw) return null;
                return JSON.parse(raw);
            } catch (e) {
                console.warn("Không đọc được draft POS:", e);
                return null;
            }
        }

        function restoreDraftForCurrentOrder() {
            const orderId = getCurrentOrderId();
            if (!orderId) return;
            const draft = loadDraft(orderId);
            if (draft) {
                applyCustomerInputs(draft);
            }
        }

        // Lần đầu load: khôi phục draft (nếu có)
        restoreDraftForCurrentOrder();

        // Khi gõ vào input -> auto lưu draft
        ["posHoTen", "posSdt", "posEmail", "posDiaChi"].forEach(function (id) {
            const el = document.getElementById(id);
            if (el) {
                el.addEventListener("input", function () {
                    saveDraftForCurrentOrder();
                });
            }
        });

        function fillHiddenCustomerInputs(form, data) {
            if (!data) return;
            const hHoTen  = form.querySelector("input[name='hoTen']");
            const hSdt    = form.querySelector("input[name='sdt']");
            const hEmail  = form.querySelector("input[name='email']");
            const hDiaChi = form.querySelector("input[name='diaChi']");

            if (hHoTen)  hHoTen.value  = data.hoTen;
            if (hSdt)    hSdt.value    = data.sdt;
            if (hEmail)  hEmail.value  = data.email;
            if (hDiaChi) hDiaChi.value = data.diaChi;
        }

        function bindPosAjaxForms() {
            const forms = posRoot.querySelectorAll("form.ajax-pos-form, form.sync-customer-form");

            forms.forEach(function (form) {
                if (form.dataset.ajaxBound === "true") return;
                form.dataset.ajaxBound = "true";

                form.addEventListener("submit", function (e) {
                    e.preventDefault();

                    // 1) Lưu draft hiện tại
                    const currentDraft = saveDraftForCurrentOrder();

                    // 2) Ghi vào hidden nếu có
                    fillHiddenCustomerInputs(form, currentDraft);

                    const formData = new FormData(form);

                    fetch(form.action, {
                        method: form.method || "POST",
                        body: formData,
                        headers: {
                            "X-Requested-With": "XMLHttpRequest"
                        }
                    })
                        .then(res => res.text())
                        .then(html => {
                            const parser = new DOMParser();
                            const doc = parser.parseFromString(html, "text/html");
                            const newPosRoot = doc.getElementById("posRoot");
                            if (!newPosRoot) return;

                            // Cập nhật data-order-id theo HTML mới
                            const newOrderId = newPosRoot.getAttribute("data-order-id");
                            if (newOrderId !== null) {
                                posRoot.setAttribute("data-order-id", newOrderId);
                            }

                            // Thay nội dung POS
                            posRoot.innerHTML = newPosRoot.innerHTML;

                            // Gắn lại event cho form mới
                            bindPosAjaxForms();

                            // Khôi phục draft cho hóa đơn hiện tại
                            restoreDraftForCurrentOrder();
                        })
                        .catch(err => {
                            console.error("Lỗi AJAX POS:", err);
                            // fallback: submit bình thường
                            form.submit();
                        });
                });
            });
        }

        bindPosAjaxForms();
    }

    // ===========================
    // 4. Validate Số điện thoại
    // ===========================
    (function () {
        const sdtInput   = document.getElementById("posSdt");
        const sdtErrorEl = document.getElementById("posSdtError");
        const formThanhToan = document.getElementById("formThanhToan");

        if (!sdtInput || !formThanhToan) return;

        // Chỉ cho nhập số
        sdtInput.addEventListener("input", function () {
            this.value = this.value.replace(/\D/g, "");
            if (sdtErrorEl) sdtErrorEl.textContent = "";
            this.classList.remove("is-invalid");
        });

        // Validate khi mất focus
        sdtInput.addEventListener("blur", function () {
            const v = this.value.trim();
            if (!v) {
                if (sdtErrorEl) sdtErrorEl.textContent = "";
                this.classList.remove("is-invalid");
                return;
            }
            const regex = /^0\d{9}$/;
            if (!regex.test(v)) {
                if (sdtErrorEl) {
                    sdtErrorEl.textContent = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0 (VD: 0901234567).";
                }
                this.classList.add("is-invalid");
            } else {
                if (sdtErrorEl) sdtErrorEl.textContent = "";
                this.classList.remove("is-invalid");
            }
        });

        // Chặn submit nếu sai
        formThanhToan.addEventListener("submit", function (e) {
            const v = sdtInput.value.trim();
            const regex = /^0\d{9}$/;

            if (v && !regex.test(v)) {
                e.preventDefault();
                if (sdtErrorEl) {
                    sdtErrorEl.textContent = "Số điện thoại không hợp lệ. Vui lòng kiểm tra lại.";
                }
                sdtInput.classList.add("is-invalid");
                sdtInput.focus();
            }
        });
    })();

    // ===========================
    // 5. Tự mở tab in hóa đơn
    // ===========================
    (function () {
        const body = document.body;
        if (!body) return;
        const printUrl = body.dataset.printUrl;
        if (printUrl) {
            const url = printUrl + "?autoPrint=true";
            window.open(url, "_blank");
        }
    })();
});
