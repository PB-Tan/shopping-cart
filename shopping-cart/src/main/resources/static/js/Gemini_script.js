        // --- Data Model ---
        // Array of available products
        const products = [
            { id: 1, name: 'China Fuji Apple (5 per pack)', price: 3.45, image: 'https://placehold.co/80x80/FFDDC1/8B4513?text=Fuji' },
            { id: 2, name: 'Enza Envy Apple (5 per pack)', price: 6.45, image: 'https://placehold.co/80x80/FFDDC1/8B4513?text=Envy' },
            { id: 3, name: 'South Africa Candy Apple (5 per pack)', price: 5.30, image: 'https://placehold.co/80x80/FFDDC1/8B4513?text=Candy' }
        ];

        // Cart items stored in an array
        let cart = JSON.parse(localStorage.getItem('shoppingCart')) || []; // Load cart from local storage

        // --- DOM Elements ---
        const catalogPage = document.getElementById('catalogPage');
        const cartPage = document.getElementById('cartPage');
        const showCatalogBtn = document.getElementById('showCatalogBtn');
        const showCartBtn = document.getElementById('showCartBtn');
        const productTableBody = document.getElementById('productTableBody');
        const cartTableBody = document.getElementById('cartTableBody');
        const subtotalDisplay = document.getElementById('subtotalDisplay');
        const totalDisplay = document.getElementById('totalDisplay');
        const shippingOptions = document.getElementById('shippingOptions');
        const freeShippingMessage = document.getElementById('freeShippingMessage');
        const flatRateShipping = document.getElementById('flatRateShipping');
        const selfCollectShipping = document.getElementById('selfCollectShipping');
        const emptyCartMessage = document.getElementById('emptyCartMessage');

        // --- Helper Functions ---

        /**
         * Saves the current cart state to local storage.
         */
        function saveCart() {
            localStorage.setItem('shoppingCart', JSON.stringify(cart));
        }

        /**
         * Finds a product by its ID.
         * @param {number} productId - The ID of the product to find.
         * @returns {object|undefined} The product object if found, otherwise undefined.
         */
        function getProductById(productId) {
            return products.find(p => p.id === productId);
        }

        /**
         * Calculates the subtotal of all items in the cart.
         * @returns {number} The calculated subtotal.
         */
        function calculateSubtotal() {
            return cart.reduce((sum, item) => {
                const product = getProductById(item.productId);
                return sum + (product ? product.price * item.quantity : 0);
            }, 0);
        }

        /**
         * Updates the total amount displayed, considering shipping costs.
         */
        function updateCartTotal() {
            let subtotal = calculateSubtotal();
            let total = subtotal;
            const shippingCost = 7.00; // Flat rate shipping cost

            // Check for free shipping condition
            if (subtotal > 99) {
                shippingOptions.classList.add('hidden');
                freeShippingMessage.classList.remove('hidden');
                total = subtotal; // Free shipping
            } else {
                shippingOptions.classList.remove('hidden');
                freeShippingMessage.classList.add('hidden');

                // Apply shipping cost based on selected radio button
                if (flatRateShipping.checked) {
                    total += shippingCost;
                }
                // Self-collect means no additional cost
            }

            subtotalDisplay.textContent = `$${subtotal.toFixed(2)}`;
            totalDisplay.textContent = `$${total.toFixed(2)}`;
        }

        // --- Render Functions ---

        /**
         * Renders the product catalog table.
         */
        function renderProducts() {
            productTableBody.innerHTML = ''; // Clear existing rows
            products.forEach(product => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td class="py-3 px-4"><img src="${product.image}" alt="${product.name}" class="w-20 h-20 object-cover rounded-md"></td>
                    <td class="py-3 px-4 font-medium">${product.name}</td>
                    <td class="py-3 px-4">$${product.price.toFixed(2)}</td>
                    <td class="py-3 px-4">
                        <div class="quantity-control">
                            <button class="btn-secondary btn-icon quantity-btn" data-action="decrease" data-product-id="${product.id}">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd" />
                                </svg>
                            </button>
                            <input type="number" class="quantity-input" value="0" min="0" data-product-id="${product.id}" readonly>
                            <button class="btn-secondary btn-icon quantity-btn" data-action="increase" data-product-id="${product.id}">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                                </svg>
                            </button>
                        </div>
                    </td>
                    <td class="py-3 px-4">
                        <button class="btn-icon add-to-cart-btn" data-product-id="${product.id}">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-600 hover:text-indigo-800" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                            </svg>
                        </button>
                    </td>
                `;
                productTableBody.appendChild(row);
            });

            // Attach event listeners for quantity controls and add to cart buttons
            productTableBody.querySelectorAll('.quantity-btn').forEach(button => {
                button.addEventListener('click', handleProductQuantityChange);
            });
            productTableBody.querySelectorAll('.add-to-cart-btn').forEach(button => {
                button.addEventListener('click', handleAddToCart);
            });
        }

        /**
         * Renders the shopping cart table and updates the checkout summary.
         */
        function renderCart() {
            cartTableBody.innerHTML = ''; // Clear existing rows

            if (cart.length === 0) {
                emptyCartMessage.classList.remove('hidden');
            } else {
                emptyCartMessage.classList.add('hidden');
                cart.forEach(item => {
                    const product = getProductById(item.productId);
                    if (!product) return; // Skip if product not found (shouldn't happen with valid data)

                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td class="py-3 px-4"><img src="${product.image}" alt="${product.name}" class="w-20 h-20 object-cover rounded-md"></td>
                        <td class="py-3 px-4 font-medium">${product.name}</td>
                        <td class="py-3 px-4">$${product.price.toFixed(2)}</td>
                        <td class="py-3 px-4">
                            <div class="quantity-control">
                                <button class="btn-secondary btn-icon cart-quantity-btn" data-action="decrease" data-product-id="${item.productId}">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path fill-rule="evenodd" d="M3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clip-rule="evenodd" />
                                    </svg>
                                </button>
                                <input type="number" class="quantity-input" value="${item.quantity}" min="1" data-product-id="${item.productId}" readonly>
                                <button class="btn-secondary btn-icon cart-quantity-btn" data-action="increase" data-product-id="${item.productId}">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                        <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                                    </svg>
                                </button>
                            </div>
                        </td>
                        <td class="py-3 px-4">
                            <button class="btn-icon remove-from-cart-btn" data-product-id="${item.productId}">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-red-600 hover:text-red-800" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                </svg>
                            </button>
                        </td>
                    `;
                    cartTableBody.appendChild(row);
                });
            }

            // Attach event listeners for cart quantity controls and remove buttons
            cartTableBody.querySelectorAll('.cart-quantity-btn').forEach(button => {
                button.addEventListener('click', handleCartQuantityChange);
            });
            cartTableBody.querySelectorAll('.remove-from-cart-btn').forEach(button => {
                button.addEventListener('click', handleRemoveFromCart);
            });

            updateCartTotal(); // Always update total when cart renders
        }

        // --- Event Handlers ---

        /**
         * Handles quantity changes on the product catalog page.
         * @param {Event} event - The click event.
         */
        function handleProductQuantityChange(event) {
            const button = event.currentTarget;
            const productId = parseInt(button.dataset.productId);
            const action = button.dataset.action;
            const input = button.parentNode.querySelector('.quantity-input');
            let quantity = parseInt(input.value);

            if (action === 'increase') {
                quantity++;
            } else if (action === 'decrease' && quantity > 0) {
                quantity--;
            }
            input.value = quantity;
        }

        /**
         * Adds a product to the cart from the catalog page.
         * @param {Event} event - The click event.
         */
        function handleAddToCart(event) {
            const button = event.currentTarget;
            const productId = parseInt(button.dataset.productId);
            const input = button.parentNode.parentNode.querySelector('.quantity-input');
            const quantity = parseInt(input.value);

            if (quantity <= 0) {
                alert('Please select a quantity greater than 0 to add to cart.');
                return;
            }

            const existingItemIndex = cart.findIndex(item => item.productId === productId);

            if (existingItemIndex > -1) {
                // Item already in cart, update quantity
                cart[existingItemIndex].quantity += quantity;
            } else {
                // Add new item to cart
                cart.push({ productId: productId, quantity: quantity });
            }

            saveCart();
            // Optionally, switch to cart page or show a confirmation
            alert(`${quantity} of ${getProductById(productId).name} added to cart!`);
            input.value = 0; // Reset quantity input on catalog page
            renderCart(); // Re-render cart to reflect changes
        }

        /**
         * Handles quantity changes for items within the shopping cart.
         * @param {Event} event - The click event.
         */
        function handleCartQuantityChange(event) {
            const button = event.currentTarget;
            const productId = parseInt(button.dataset.productId);
            const action = button.dataset.action;
            const itemIndex = cart.findIndex(item => item.productId === productId);

            if (itemIndex > -1) {
                if (action === 'increase') {
                    cart[itemIndex].quantity++;
                } else if (action === 'decrease') {
                    cart[itemIndex].quantity--;
                    if (cart[itemIndex].quantity <= 0) {
                        // If quantity drops to 0 or less, remove the item
                        cart.splice(itemIndex, 1);
                    }
                }
                saveCart();
                renderCart(); // Re-render cart to reflect changes
            }
        }

        /**
         * Removes an item completely from the shopping cart.
         * @param {Event} event - The click event.
         */
        function handleRemoveFromCart(event) {
            const button = event.currentTarget;
            const productId = parseInt(button.dataset.productId);
            cart = cart.filter(item => item.productId !== productId); // Filter out the item
            saveCart();
            renderCart(); // Re-render cart to reflect changes
        }

        /**
         * Toggles between the catalog and cart pages.
         * @param {string} pageId - The ID of the page to show ('catalogPage' or 'cartPage').
         */
        function showPage(pageId) {
            if (pageId === 'catalogPage') {
                catalogPage.classList.remove('hidden');
                cartPage.classList.add('hidden');
                renderProducts(); // Re-render products to reset quantities if needed
            } else {
                cartPage.classList.remove('hidden');
                catalogPage.classList.add('hidden');
                renderCart(); // Ensure cart is up-to-date when viewed
            }
        }

        // --- Event Listeners for Page Navigation and Shipping ---
        showCatalogBtn.addEventListener('click', () => showPage('catalogPage'));
        showCartBtn.addEventListener('click', () => showPage('cartPage'));

        flatRateShipping.addEventListener('change', updateCartTotal);
        selfCollectShipping.addEventListener('change', updateCartTotal);

        // --- Initial Load ---
        document.addEventListener('DOMContentLoaded', () => {
            // Set default shipping option
            flatRateShipping.checked = true;
            showPage('catalogPage'); // Start on the catalog page
        });

        // Simple alert replacement for demonstration
        function alert(message) {
            const alertBox = document.createElement('div');
            alertBox.className = 'fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-indigo-600 text-white px-6 py-4 rounded-lg shadow-xl z-50 animate-fade-in-down';
            alertBox.textContent = message;
            document.body.appendChild(alertBox);
            setTimeout(() => {
                alertBox.classList.add('animate-fade-out-up');
                alertBox.addEventListener('animationend', () => alertBox.remove());
            }, 2000); // Message disappears after 2 seconds
        }

        // Add simple fade-in/out animations for the alert box
        const style = document.createElement('style');
        style.innerHTML = `
            @keyframes fade-in-down {
                from { opacity: 0; transform: translate(-50%, -60%); }
                to { opacity: 1; transform: translate(-50%, -50%); }
            }
            @keyframes fade-out-up {
                from { opacity: 1; transform: translate(-50%, -50%); }
                to { opacity: 0; transform: translate(-50%, -60%); }
            }
            .animate-fade-in-down {
                animation: fade-in-down 0.3s ease-out forwards;
            }
            .animate-fade-out-up {
                animation: fade-out-up 0.3s ease-in forwards;
            }
        `;
        document.head.appendChild(style);
