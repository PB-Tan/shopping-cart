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
('Mechanical Keyboard','KeyCo','Peripherals',NULL,'Blue-switch keyboard','https://img/kb','Mechanical Keyboard',2,89.00),
('27-inch Monitor','ViewBest','Display',NULL,'Full HD IPS monitor','https://img/monitor','27-inch Monitor',3,189.00),
('USB-C Hub','Hubify','Peripherals',NULL,'6-in-1 USB-C hub','https://img/hub','USB-C Hub',50,49.90),
('Gaming Headset','SoundMax','Audio',NULL,'Surround sound headset','https://img/headset','Gaming Headset',25,69.00),
('Webcam 1080p','CamPro','Peripherals',NULL,'Full HD USB webcam','https://img/webcam','Webcam 1080p',40,39.90),
('External SSD 1TB','FastDrive','Storage',NULL,'USB 3.2 NVMe SSD','https://img/ssd1tb','External SSD 1TB',15,129.00),
('Laptop Stand','ErgoLift','Accessories',NULL,'Adjustable aluminum stand','https://img/stand','Laptop Stand',80,34.90),
('Wireless Charger','ChargeIt','Accessories',NULL,'Qi-certified fast charging pad','https://img/charger','Wireless Charger',60,24.90),
('Bluetooth Speaker','BeatGo','Audio',NULL,'Portable waterproof speaker','https://img/speaker','Bluetooth Speaker',30,59.00),
('4K Monitor','ViewBest','Display',NULL,'27-inch 4K IPS monitor','https://img/monitor4k','4K Monitor',5,329.00),
('Ergonomic Chair','SitWell','Furniture',NULL,'Lumbar support office chair','https://img/chair','Ergonomic Chair',12,199.00),
('Graphics Tablet','StylusPro','Peripherals',NULL,'Drawing tablet with pen','https://img/tablet','Graphics Tablet',20,149.00);

-- ===== CART ITEMS =====
INSERT INTO `cart_item` (cart_id, product_id, quantity, unit_price, method_type)
VALUES
(1, 1, 2, 29.90, 'add'),   
(1, 2, 1, 89.00, 'add'),   
(1, 3, 4, 189.00, 'add'),  
(2, 2, 1, 89.00, 'add'),
(3, 3, 1, 189.00, 'add');

-- ===== PAYMENT METHODS =====
INSERT INTO `payment_method`
(customer_username, card_holder_name)
VALUES
('alice','ALICE TAN'),
('ben', 'BEN NG'),
('charlie','CHARLIE LIM');

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
(created_at, grand_total, status, discount_code, customer_username, shipment_id, transaction_id)
VALUES
(NOW(6), 338.80, 'Pending',   NULL, 'alice', 1, 1),
(NOW(6),  84.00, 'Shipped',   NULL, 'ben',   2, 2),
(NOW(6), 189.00, 'Completed', NULL, 'charlie',3, 3);

-- ===== ORDER ITEMS =====
INSERT INTO `order_item`
(order_id, product_id, product_name, unit_price, quantity, item_total)
VALUES
(1, 1, 'Wireless Mouse',        29.90, 2,  59.80),
(1, 2, 'Mechanical Keyboard',   89.00, 1,  89.00),
(1, 3, '27-inch Monitor',      189.00, 3, 189.00),
(2, 2, 'Mechanical Keyboard',   89.00, 1,  84.00),
(3, 3, '27-inch Monitor',      189.00, 1, 189.00);

-- ===== DISCOUNT CODE =====
INSERT INTO `discount_code`
(code, percent)
VALUES
('TEAM08',50),
('HELLOWORLD',10),
('HELLO',20);
