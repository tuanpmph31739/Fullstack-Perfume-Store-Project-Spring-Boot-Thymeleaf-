let invoiceIdCounter = 1; // Bắt đầu từ 2
let carts = {'invoice1': []};  // Khởi tạo giỏ hàng của Hóa đơn 1 ngay từ đầu
let currentInvoiceId = 'invoice1';

// Khởi tạo Hóa đơn mặc định khi tải trang
function initializeDefaultInvoice() {
    if (Object.keys(carts).length === 0) {
        addNewInvoice(true);
    } else {
        // Khôi phục các hóa đơn đã lưu trong localStorage
        Object.keys(carts).forEach(invoiceId => {
            if (!document.getElementById(invoiceId)) { // Kiểm tra nếu hóa đơn chưa tồn tại trong DOM
                addInvoiceTab(invoiceId, invoiceId === currentInvoiceId);
            }
        });

        // Cập nhật invoiceIdCounter dựa trên số hóa đơn lớn nhất
        invoiceIdCounter = Math.max(...Object.keys(carts).map(id => parseInt(id.replace('invoice', '')))) + 1;

        switchInvoice(currentInvoiceId);
    }
}

// Thêm Hóa đơn mới
function addNewInvoice(isDefault = false) {
    const invoiceId = `invoice${invoiceIdCounter++}`; // Tạo số hóa đơn mới từ counter

    addInvoiceTab(invoiceId, isDefault);

    if (!carts[invoiceId]) {
        carts[invoiceId] = [];
    }
    switchInvoice(invoiceId);
    saveCartsToLocalStorage();
}

// Thêm tab hóa đơn vào giao diện
function addInvoiceTab(invoiceId, isActive = false) {
    const tabContainer = document.querySelector('.tab-container');

    const tab = document.createElement('div');
    tab.classList.add('tab');
    if (isActive) tab.classList.add('active');
    tab.id = invoiceId;
    const invoiceNumber = parseInt(invoiceId.replace('invoice', ''));
    tab.innerHTML = `Hóa đơn ${invoiceNumber} <span class="material-icons" onclick="closeInvoice('${invoiceId}', event)">close</span>`;
    tab.onclick = () => switchInvoice(invoiceId);

    tabContainer.insertBefore(tab, tabContainer.querySelector('.material-icons.bg-green-500'));
}

// Chuyển đổi giữa các Hóa đơn
function switchInvoice(invoiceId) {
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    document.getElementById(invoiceId).classList.add('active');
    currentInvoiceId = invoiceId;
    updateCart();
}

// Đóng Hóa đơn
function closeInvoice(invoiceId, event) {
    event.stopPropagation();
    const tab = document.getElementById(invoiceId);

    if (tab) {
        tab.remove();
        delete carts[invoiceId];
        saveCartsToLocalStorage();

        const remainingTabs = document.querySelectorAll('.tab');
        if (remainingTabs.length > 0) {
            switchInvoice(remainingTabs[0].id);
        } else {
            invoiceIdCounter = 2; // Reset nếu không còn hóa đơn nào
            addNewInvoice(true);
        }
    }
}


// Thêm sản phẩm vào giỏ hàng của Hóa đơn hiện tại
function addToCart(id, name, price) {
    const cart = carts[currentInvoiceId];
    const existingItem = cart.find(item => item.id === id);
    if (existingItem) {
        existingItem.quantity++;
        existingItem.total += price;
    } else {
        cart.push({id, name, price, quantity: 1, total: price});
    }
    updateCart();
    saveCartsToLocalStorage();
}

// Cập nhật giao diện giỏ hàng
function updateCart() {
    const cart = carts[currentInvoiceId];
    const cartList = document.getElementById('cartList');
    cartList.innerHTML = ''; // Clear cart content before updating
    let totalAmount = 0;

    cart.forEach((item, index) => {
        // Create a list item with 'cart-item' class for styling
        const li = document.createElement('li');
        li.classList.add('cart-item'); // Apply 'cart-item' class here
        li.innerHTML = `
            <span class="product-name">${item.name}</span>
            <input type="number" class="form-control quantity-input" value="${item.quantity}" min="1" onchange="updateQuantity(${index}, this.value)">
            <span class="product-price">${item.total.toLocaleString()} VND</span>
            <button class="btn btn-danger btn-sm btn-remove" onclick="removeProduct(${index})">Xóa</button>
        `;
        cartList.appendChild(li);

        // Calculate total amount
        totalAmount += item.total;
    });

    // Update total amount display
    document.getElementById('totalAmount').innerText = totalAmount.toLocaleString();
    document.getElementById('modalTotalAmount').innerText = totalAmount.toLocaleString();
}

// Cập nhật số lượng sản phẩm trong giỏ hàng
function updateQuantity(index, quantity) {
    const cart = carts[currentInvoiceId];
    cart[index].quantity = parseInt(quantity);
    cart[index].total = cart[index].price * cart[index].quantity;
    updateCart();
    saveCartsToLocalStorage();
}

// Xóa sản phẩm khỏi giỏ hàng
function removeProduct(index) {
    carts[currentInvoiceId].splice(index, 1);
    updateCart();
    saveCartsToLocalStorage();
}

// Thiết lập sự kiện nhấp chuột cho các sản phẩm trong danh sách sản phẩm
function setupProductClickEvents() {
    document.querySelectorAll('#productList .list-group-item').forEach(item => {
        item.onclick = function () {
            const id = this.getAttribute('data-id');
            const name = this.getAttribute('data-name');
            const price = parseFloat(this.getAttribute('data-price'));

            if (name && !isNaN(price)) {
                console.log(`Adding to cart: ${name} - ${price} VND`);
                addToCart(id, name, price);
            } else {
                console.error("Error: Invalid product data", {name, price});
            }
        };
    });
}

function saveCartsToLocalStorage() {
    localStorage.setItem('carts', JSON.stringify(carts));
    localStorage.setItem('currentInvoiceId', currentInvoiceId);
    localStorage.setItem('invoiceIdCounter', invoiceIdCounter);
    localStorage.setItem('deletedInvoiceNumbers', JSON.stringify(deletedInvoiceNumbers));
}

function loadCartsFromLocalStorage() {
    const savedCarts = localStorage.getItem('carts');
    if (savedCarts) {
        carts = JSON.parse(savedCarts);
    }
    currentInvoiceId = localStorage.getItem('currentInvoiceId') || 'invoice1';
    invoiceIdCounter = parseInt(localStorage.getItem('invoiceIdCounter')) || 2;
    deletedInvoiceNumbers = JSON.parse(localStorage.getItem('deletedInvoiceNumbers')) || [];
}

// Mở/Đóng Modal Thanh toán
function togglePaymentModal() {
    document.getElementById('paymentModal').classList.toggle('show');
}

// Tính tiền thừa trả lại
function calculateChange() {
    const customerPaid = parseFloat(document.getElementById('customerPaid').value) || 0;
    const totalAmount = parseFloat(document.getElementById('modalTotalAmount').innerText.replace(/,/g, ''));
    document.getElementById('changeAmount').innerText = (customerPaid - totalAmount).toLocaleString();
}

// Khởi chạy hàm initializeDefaultInvoice khi trang được tải
window.onload = function () {
    loadCartsFromLocalStorage();
    initializeDefaultInvoice();
    setupProductClickEvents();
    updateCart();
};