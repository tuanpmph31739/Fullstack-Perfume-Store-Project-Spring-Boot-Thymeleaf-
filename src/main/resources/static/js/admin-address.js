document.addEventListener("DOMContentLoaded", function () {

    // ====== HÀM DÙNG CHUNG ======

    // Tách "Số nhà..., Phường X, Quận Y, Tỉnh Z" -> 4 phần
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
        if (!targetName || !select) return null;
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

    function initAddressBlock(config) {
        const {
            provinceId,
            districtId,
            wardId,
            streetId,
            fullAddressId
        } = config;

        const provinceSelect   = document.getElementById(provinceId);
        const districtSelect   = document.getElementById(districtId);
        const wardSelect       = document.getElementById(wardId);
        const streetInput      = document.getElementById(streetId);
        const fullAddressInput = document.getElementById(fullAddressId);

        // Nếu form này không có block địa chỉ thì thôi
        if (!provinceSelect || !districtSelect || !wardSelect || !streetInput || !fullAddressInput) {
            return;
        }

        const existingAddressRaw = fullAddressInput.value ? fullAddressInput.value.trim() : "";

        function updateFullAddress() {
            const p = provinceSelect.options[provinceSelect.selectedIndex]?.getAttribute("data-name") || "";
            const d = districtSelect.options[districtSelect.selectedIndex]?.getAttribute("data-name") || "";
            const w = wardSelect.options[wardSelect.selectedIndex]?.getAttribute("data-name") || "";
            const s = streetInput.value.trim();

            const arr = [];
            if (s) arr.push(s);
            if (w) arr.push(w);
            if (d) arr.push(d);
            if (p) arr.push(p);

            fullAddressInput.value = arr.join(", ");
        }

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
                .catch(err => console.error("Lỗi auto-fill địa chỉ (admin):", err));
        }

        // Load tỉnh/TP lần đầu
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
            })
            .catch(err => console.error("Lỗi load tỉnh/TP (admin):", err));

        // Event change
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
    }

    // ====== KHỞI TẠO CHO 2 FORM ADMIN ======

    // Form khách hàng
    initAddressBlock({
        provinceId:   "kh-province",
        districtId:   "kh-district",
        wardId:       "kh-ward",
        streetId:     "kh-street",
        fullAddressId:"kh-fullAddress"
    });

    // Form nhân viên
    initAddressBlock({
        provinceId:   "nv-province",
        districtId:   "nv-district",
        wardId:       "nv-ward",
        streetId:     "nv-street",
        fullAddressId:"nv-fullAddress"
    });

});
