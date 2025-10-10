-- ===== CUSTOMERS =====
INSERT INTO `customer`
(username, first_name, last_name, email, password, address, country, postal_code, phone_number, provider_customer_id)
VALUES
('alice','Alice','Tan','alice@example.com','pw123','123 Orchard Rd','Singapore','238888','+65-81112222','CUST001'),
('ben','Ben','Ng','ben@example.com','pw123','88 Serangoon Ave','Singapore','556677','+65-82223333','CUST002'),
('charlie','Charlie','Lim','charlie@example.com','pw123','1 Pasir Ris St 1','Singapore','510101','+65-83334444','CUST003');

-- ===== CARTS (owning side FK to customer) =====
INSERT INTO `cart` (customer_username)
VALUES ('alice'), ('ben'), ('charlie');

-- ===== PRODUCTS =====
INSERT INTO `product`
(name, brand, category, collection, description, image_url, image_alt, stock, unit_price)
VALUES
('Wireless Mouse','Logi','Peripherals',NULL,'2.4GHz ergonomic mouse','https://img/mouse','Wireless Mouse',100,29.90),
('Mechanical Keyboard','KeyCo','Peripherals',NULL,'Blue-switch keyboard','https://img/kb','Mechanical Keyboard',50,89.00),
('27-inch Monitor','ViewBest','Display',NULL,'Full HD IPS monitor','https://img/monitor','27-inch Monitor',25,189.00);

-- ===== CART ITEMS =====
-- Alice has 3 cart items now
INSERT INTO `cart_item` (cart_id, product_id, quantity, unit_price, method_type)
VALUES
(1, 1, 2, 29.90, 'add'),   
(1, 2, 1, 89.00, 'add'),   
(1, 3, 4, 189.00, 'add'),  
(2, 2, 1, 89.00, 'add'),
(3, 3, 1, 189.00, 'add');

-- ===== PAYMENT METHODS =====
INSERT INTO `payment_method`
(customer_username, expiry_month, expiry_year, card_type, card_holder_name, last_four_digits, is_default)
VALUES
('alice', 12, 2027, 'VISA',       'ALICE TAN',   '4242', 1),
('ben',   11, 2026, 'MASTERCARD', 'BEN NG',      '1111', 1),
('charlie', 6, 2028,'AMEX',       'CHARLIE LIM', '2222', 0);

-- ===== TRANSACTIONS =====
INSERT INTO `transaction`
(charged, created_at, currency, grand_total, idempotency_key, is_default, payment_type, provider, provider_product, provider_transaction_id, payment_method_id)
VALUES
('Y', NOW(6), 'SGD',  338.80, 'TXN001', 1, 'CARD', 'Stripe', 'VISA',       'TXN-1001', 1),
('Y', NOW(6), 'SGD',   89.00, 'TXN002', 1, 'CARD', 'Stripe', 'MASTERCARD', 'TXN-1002', 2),
('Y', NOW(6), 'SGD',  189.00, 'TXN003', 1, 'CARD', 'Stripe', 'AMEX',       'TXN-1003', 3);

-- ===== SHIPMENTS =====
INSERT INTO `shipment`
(courier_name, created_at, delivery_estimate, service_level, shipment_code, shipment_method)
VALUES
('SingPost', NOW(6), DATE_ADD(NOW(6), INTERVAL 3 DAY), 'Standard', 'SHIP001', 'Home Delivery'),
('NinjaVan', NOW(6), DATE_ADD(NOW(6), INTERVAL 1 DAY), 'Express',  'SHIP002', 'Same Day'),
('J&T',      NOW(6), DATE_ADD(NOW(6), INTERVAL 5 DAY), 'Economy',  'SHIP003', 'Pick up Point');

-- ===== ORDERS =====
INSERT INTO `orders`
(created_at, discount_total, fulfilment_status, grand_total, order_status, payment_status, sub_total, tax_total, promo_codes, customer_username, shipment_id, transaction_id)
VALUES
(NOW(6), 0, 'Processing', 338.80, 'Pending',   'Paid', 338.80, 0, NULL, 'alice', 1, 1),
(NOW(6), 5, 'Shipped',     84.00, 'Shipped',   'Paid',  89.00, 0, NULL, 'ben',   2, 2),
(NOW(6), 0, 'Delivered',  189.00, 'Completed', 'Paid', 189.00, 0, NULL, 'charlie',3, 3);

-- ===== ORDER ITEMS =====
INSERT INTO `order_item`
(order_id, product_id, product_name, unit_price, quantity, item_discount, item_tax, item_total)
VALUES
(1, 1, 'Wireless Mouse',        29.90, 2, 0, 0, 59.80),
(1, 2, 'Mechanical Keyboard',   89.00, 1, 0, 0, 89.00),
(1, 3, '27-inch Monitor',      189.00, 3, 0, 0,189.00),
(2, 2, 'Mechanical Keyboard',   89.00, 1, 5, 0, 84.00),
(3, 3, '27-inch Monitor',      189.00, 1, 0, 0,189.00);
