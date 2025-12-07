
    document.addEventListener("DOMContentLoaded", function () {
    // Lặp qua tất cả menu có submenu
    document.querySelectorAll("#sidebar .sidebar-item.has-sub > .sidebar-link").forEach(function (link) {
        link.addEventListener("click", function (e) {
            e.preventDefault(); // Ngăn không cho link reload trang

            const parent = link.parentElement; // <li class="sidebar-item has-sub">
            const submenu = parent.querySelector(".submenu");

            // Toggle class active
            if (parent.classList.contains("active")) {
                parent.classList.remove("active");
                submenu.style.maxHeight = null;
            } else {
                // Đóng các menu khác
                document.querySelectorAll("#sidebar .sidebar-item.has-sub.active").forEach(function (item) {
                    item.classList.remove("active");
                    item.querySelector(".submenu").style.maxHeight = null;
                });
                // Mở menu hiện tại
                parent.classList.add("active");
                submenu.style.maxHeight = submenu.scrollHeight + "px";
            }
        });
    });
});

        document.addEventListener('DOMContentLoaded', function () {
        const triggers = document.querySelectorAll('[data-bs-toggle="tooltip"]');
        [...triggers].forEach(el => new bootstrap.Tooltip(el));
    });


    document.addEventListener('DOMContentLoaded', function () {
        const productSelect = document.querySelector('select.choices');

        if (productSelect) {
            new Choices(productSelect, {
                searchEnabled: true,              // bật search
                itemSelectText: '',               // bỏ chữ "Press to select"
                shouldSort: false,                // giữ nguyên thứ tự option
                placeholder: true,
                placeholderValue: '-- Chọn sản phẩm --',
                noResultsText: 'Không tìm thấy sản phẩm',
                noChoicesText: 'Không có dữ liệu sản phẩm',
            });
        }
    });

    function setupPriceInput(displayId, hiddenId) {
        const displayInput = document.getElementById(displayId);
        const hiddenInput  = document.getElementById(hiddenId);
        if (!displayInput || !hiddenInput) return;

        displayInput.addEventListener('input', function () {
            // Lấy toàn bộ số trong input
            let raw = this.value.replace(/\D/g, '');

            // Không cho nhỏ hơn 0
            let num = parseInt(raw || '0', 10);
            if (num < 0) num = 0;

            // Gán vào hidden (giá trị thật gửi lên server)
            hiddenInput.value = num.toString();

            // Format có dấu chấm ngăn cách hàng nghìn để hiển thị
            this.value = num > 0 ? num.toLocaleString('vi-VN') : '';
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        setupPriceInput('giaMinDisplay', 'giaMin');
        setupPriceInput('giaMaxDisplay', 'giaMax');
    });