document.addEventListener("DOMContentLoaded", function () {
    // 1. UI Thanh toán
    const radioInputs = document.querySelectorAll('.payment-option input[type="radio"]');
    function updatePaymentUI() {
        document.querySelectorAll('.payment-option').forEach(opt => opt.classList.remove('selected'));
        const checked = document.querySelector('.payment-option input[type="radio"]:checked');
        if (checked) checked.closest('.payment-option').classList.add('selected');
    }
    radioInputs.forEach(input => input.addEventListener('change', updatePaymentUI));
    updatePaymentUI();

    // 2. API Địa chỉ
    const provinceSelect   = document.getElementById("province");
    const districtSelect   = document.getElementById("district");
    const wardSelect       = document.getElementById("ward");
    const streetInput      = document.getElementById("street");
    const fullAddressInput = document.getElementById("fullAddress");

    // Địa chỉ mặc định từ DB (NguoiDung.DiaChi -> checkoutForm.diaChi -> hidden#fullAddress)
    const existingAddressRaw = fullAddressInput && fullAddressInput.value
        ? fullAddressInput.value.trim()
        : "";

    // Tách "Số nhà, ngõ, đường, Phường X, Quận Y, Tỉnh Z" -> 4 phần
    function splitAddress(addr) {
        const parts = addr.split(',')
            .map(p => p.trim())
            .filter(p => p.length > 0);

        let province = "", district = "", ward = "", street = "";

        if (parts.length >= 4) {
            province = parts[parts.length - 1];
            district = parts[parts.length - 2];
            ward     = parts[parts.length - 3];
            street   = parts.slice(0, parts.length - 3).join(", ");
        } else if (parts.length === 3) {
            province = parts[2];
            district = parts[1];
            street   = parts[0];
        } else if (parts.length === 2) {
            province = parts[1];
            street   = parts[0];
        } else if (parts.length === 1) {
            street   = parts[0];
        }
        return { province, district, ward, street };
    }

    // Tìm option có tên gần giống
    function findOptionByName(select, targetName) {
        if (!targetName) return null;
        const normTarget = targetName.toLowerCase();
        let found = null;
        Array.from(select.options).forEach(opt => {
            const name = (opt.getAttribute("data-name") || opt.textContent || "").toLowerCase();
            if (!name) return;
            if (name === normTarget || name.includes(normTarget) || normTarget.includes(name)) {
                found = opt;
            }
        });
        return found;
    }

    // Build lại chuỗi địa chỉ gửi lên server
    function updateFullAddress() {
        const p = provinceSelect.options[provinceSelect.selectedIndex]?.getAttribute("data-name") || "";
        const d = districtSelect.options[districtSelect.selectedIndex]?.getAttribute("data-name") || "";
        const w = wardSelect.options[wardSelect.selectedIndex]?.getAttribute("data-name") || "";
        const s = streetInput.value.trim();

        let arr = [];
        if (s) arr.push(s);
        if (w) arr.push(w);
        if (d) arr.push(d);
        if (p) arr.push(p);

        fullAddressInput.value = arr.join(", ");

        validateField(
            streetInput,
            'error-address',
            () => (provinceSelect.value && districtSelect.value && wardSelect.value && s !== "")
        );
    }

    // Auto-fill 4 ô từ địa chỉ mặc định (nếu parse được)
    function autoFillAddressFromExisting() {
        if (!existingAddressRaw) return;

        const parts = splitAddress(existingAddressRaw);
        if (!parts.province) return;

        const provOpt = findOptionByName(provinceSelect, parts.province);
        if (!provOpt) return;

        provinceSelect.value = provOpt.value;

        if (parts.street) {
            streetInput.value = parts.street;
        }

        districtSelect.disabled = false;
        fetch(`https://esgoo.net/api-tinhthanh/2/${provOpt.value}.htm`)
            .then(r => r.json())
            .then(data => {
                districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
                if (data.error === 0) {
                    data.data.forEach(item => {
                        let opt = new Option(item.full_name, item.id);
                        opt.setAttribute("data-name", item.full_name);
                        districtSelect.add(opt);
                    });
                }

                if (!parts.district) {
                    updateFullAddress();
                    return;
                }

                const distOpt = findOptionByName(districtSelect, parts.district);
                if (!distOpt) {
                    updateFullAddress();
                    return;
                }
                districtSelect.value = distOpt.value;

                wardSelect.disabled = false;
                return fetch(`https://esgoo.net/api-tinhthanh/3/${distOpt.value}.htm`)
                    .then(r => r.json())
                    .then(data2 => {
                        wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
                        if (data2.error === 0) {
                            data2.data.forEach(item => {
                                let opt = new Option(item.full_name, item.id);
                                opt.setAttribute("data-name", item.full_name);
                                wardSelect.add(opt);
                            });
                        }

                        if (parts.ward) {
                            const wardOpt = findOptionByName(wardSelect, parts.ward);
                            if (wardOpt) {
                                wardSelect.value = wardOpt.value;
                            }
                        }
                        updateFullAddress();
                    });
            })
            .catch(err => console.error("Lỗi auto-fill địa chỉ:", err));
    }

    // Load Tỉnh/TP lần đầu
    fetch('https://esgoo.net/api-tinhthanh/1/0.htm')
        .then(r => r.json())
        .then(data => {
            if (data.error === 0) {
                data.data.forEach(item => {
                    let opt = new Option(item.full_name, item.id);
                    opt.setAttribute("data-name", item.full_name);
                    provinceSelect.add(opt);
                });
            }
            autoFillAddressFromExisting();
        });

    provinceSelect.addEventListener("change", function () {
        districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
        wardSelect.innerHTML     = '<option value="">Chọn Phường/Xã</option>';
        wardSelect.disabled      = true;

        if (this.value) {
            districtSelect.disabled = false;
            fetch(`https://esgoo.net/api-tinhthanh/2/${this.value}.htm`)
                .then(r => r.json())
                .then(data => {
                    if (data.error === 0) {
                        data.data.forEach(item => {
                            let opt = new Option(item.full_name, item.id);
                            opt.setAttribute("data-name", item.full_name);
                            districtSelect.add(opt);
                        });
                    }
                });
        }
        updateFullAddress();
    });

    districtSelect.addEventListener("change", function () {
        wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
        if (this.value) {
            wardSelect.disabled = false;
            fetch(`https://esgoo.net/api-tinhthanh/3/${this.value}.htm`)
                .then(r => r.json())
                .then(data => {
                    if (data.error === 0) {
                        data.data.forEach(item => {
                            let opt = new Option(item.full_name, item.id);
                            opt.setAttribute("data-name", item.full_name);
                            wardSelect.add(opt);
                        });
                    }
                });
        }
        updateFullAddress();
    });

    wardSelect.addEventListener("change", updateFullAddress);
    streetInput.addEventListener("input", updateFullAddress);

    // 3. VALIDATION
    const form       = document.getElementById("checkoutForm");
    const nameInput  = document.getElementById("tenNguoiNhan");
    const emailInput = document.getElementById("email");
    const phoneInput = document.getElementById("phoneInput");

    function setInvalid(input, errorId, isInvalid) {
        const errorEl = document.getElementById(errorId);
        if (isInvalid) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            if (errorEl) errorEl.classList.remove('d-none');
        } else {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            if (errorEl) errorEl.classList.add('d-none');
        }
    }

    function validateField(input, errorId, checkFn) {
        const isValid = checkFn();
        setInvalid(input, errorId, !isValid);
        return isValid;
    }

    nameInput.addEventListener('input', () =>
        validateField(nameInput, 'error-tenNguoiNhan', () => nameInput.value.trim() !== "")
    );

    emailInput.addEventListener('input', () => {
        if (emailInput.value.trim() === "") {
            emailInput.classList.remove('is-invalid', 'is-valid');
            document.getElementById('error-email').classList.add('d-none');
        } else {
            validateField(
                emailInput,
                'error-email',
                () => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailInput.value)
            );
        }
    });

    function checkPhone() {
        const num = phoneInput.value.trim();
        const regex = /^0(3|5|7|8|9)[0-9]{8}$/; // di động VN
        return validateField(phoneInput, 'error-phone', () => regex.test(num));
    }
    phoneInput.addEventListener('input', checkPhone);

    // SUBMIT FORM ĐẶT HÀNG (có Swal xác nhận)
    form.addEventListener("submit", function (e) {
        e.preventDefault();

        let isNameValid  = validateField(nameInput, 'error-tenNguoiNhan', () => nameInput.value.trim() !== "");
        let isPhoneValid = checkPhone();

        const hasExistingAddress =
            fullAddressInput.value && fullAddressInput.value.trim() !== "";

        const hasNewAddressParts =
            provinceSelect.value ||
            districtSelect.value ||
            wardSelect.value ||
            streetInput.value.trim() !== "";

        let isAddressValid;

        if (hasExistingAddress && !hasNewAddressParts) {
            // Dùng luôn địa chỉ cũ
            isAddressValid = true;
            streetInput.classList.remove('is-invalid');
            document.getElementById('error-address').classList.add('d-none');
        } else {
            isAddressValid = (
                provinceSelect.value &&
                districtSelect.value &&
                wardSelect.value &&
                streetInput.value.trim() !== ""
            );

            if (!isAddressValid) {
                streetInput.classList.add('is-invalid');
                document.getElementById('error-address').classList.remove('d-none');
            } else {
                updateFullAddress();
            }
        }

        if (isNameValid && isPhoneValid && isAddressValid) {
            Swal.fire({
                title: 'Xác nhận đặt hàng?',
                text: 'Bạn có chắc muốn đặt đơn hàng này không?',
                icon: 'question',
                showCancelButton: true,
                confirmButtonColor: '#3085d6',
                cancelButtonColor: '#d33',
                confirmButtonText: 'Đồng ý',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    Swal.fire({
                        title: 'Đang xử lý...',
                        text: 'Vui lòng chờ trong giây lát',
                        allowOutsideClick: false,
                        didOpen: () => {
                            Swal.showLoading();
                        }
                    });

                    setTimeout(() => {
                        form.submit();
                    }, 500);
                }
            });
        } else {
            Swal.fire({
                icon: 'error',
                title: 'Thông tin chưa đủ!',
                text: 'Vui lòng kiểm tra lại thông tin đặt hàng!.',
                confirmButtonColor: '#d33',
                confirmButtonText: 'Đã hiểu'
            });

            const firstError = document.querySelector('.is-invalid');
            if (firstError) {
                firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                firstError.focus();
            }
        }
    });

    // ============= VOUCHER Ở TRANG CHECKOUT =============
    const applyVoucherBtn    = document.getElementById("apply-voucher-btn");
    const voucherCodeInput   = document.getElementById("voucherCode");
    const voucherInlineError = document.getElementById("voucher-inline-error");
    const removeVoucherBtn   = document.getElementById("remove-voucher-btn");

    const csrfTokenEl  = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken    = csrfTokenEl  ? csrfTokenEl.getAttribute('content') : null;
    const csrfHeader   = csrfHeaderEl ? csrfHeaderEl.getAttribute('content') : null;

    if (applyVoucherBtn) {
        applyVoucherBtn.addEventListener("click", function () {
            voucherInlineError.classList.add('d-none');

            const code = voucherCodeInput.value.trim();
            if (!code) {
                voucherInlineError.textContent = "Vui lòng nhập mã giảm giá.";
                voucherInlineError.classList.remove('d-none');
                voucherCodeInput.focus();
                return;
            }

            const body = new URLSearchParams();
            body.append("maGiamGia", code);

            fetch("/cart/apply-voucher", {
                method: "POST",
                headers: Object.assign(
                    { "Content-Type": "application/x-www-form-urlencoded" },
                    (csrfToken && csrfHeader) ? { [csrfHeader]: csrfToken } : {}
                ),
                body: body
            })
                .then(async res => {
                    if (res.ok) {
                        await Swal.fire({
                            icon: 'success',
                            title: 'Áp dụng mã thành công!',
                            showConfirmButton: false,
                            timer: 1200
                        });
                        window.location.reload();
                    } else {
                        let data = {};
                        try { data = await res.json(); } catch (_) {}
                        const msg = data.message || "Không thể áp dụng mã giảm giá.";
                        throw new Error(msg);
                    }
                })
                .catch(err => {
                    Swal.fire({
                        icon: 'error',
                        title: 'Không áp dụng được mã',
                        text: err.message,
                        confirmButtonColor: '#d33'
                    });
                });
        });
    }

    if (removeVoucherBtn) {
        removeVoucherBtn.addEventListener("click", function (e) {
            e.preventDefault();
            fetch("/cart/remove-voucher", {
                method: "POST",
                headers: (csrfToken && csrfHeader) ? { [csrfHeader]: csrfToken } : {}
            })
                .then(async res => {
                    if (res.ok) {
                        await Swal.fire({
                            icon: 'success',
                            title: 'Đã hủy mã giảm giá',
                            showConfirmButton: false,
                            timer: 1000
                        });
                        window.location.reload();
                    } else {
                        let data = {};
                        try { data = await res.json(); } catch (_) {}
                        const msg = data.message || "Không thể hủy mã giảm giá.";
                        throw new Error(msg);
                    }
                })
                .catch(err => {
                    Swal.fire({
                        icon: 'error',
                        title: 'Lỗi',
                        text: err.message,
                        confirmButtonColor: '#d33'
                    });
                });
        });
    }
    // ============= HẾT VOUCHER CHECKOUT =============
});
