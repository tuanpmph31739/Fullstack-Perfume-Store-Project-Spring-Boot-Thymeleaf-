
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

