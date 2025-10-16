INSERT INTO customer (username, first_name, last_name, email, password, address, country, postal_code, phone_number, provider_customer_id)
VALUES ('alice','Alice','Tan','alice@example.com','pw123','123 Orchard Rd','Singapore','238888','+65-81112222','CUST001');

INSERT INTO cart (id, customer_username) VALUES (1, 'alice');

INSERT INTO product (id, name, brand, category, collection, description, image_url, image_alt, stock, unit_price)
VALUES
(1,'Wireless Mouse','Logi','Peripherals',NULL,'2.4GHz ergonomic mouse','/img/mouse.png','Wireless Mouse',10,29.90),
(2,'USB-C Hub','Hubify','Peripherals',NULL,'6-in-1 USB-C hub','/img/hub.jpg','USB-C Hub',5,49.90);

INSERT INTO cart_item (id, cart_id, product_id, quantity, unit_price, method_type)
VALUES
(1,1,1,2,29.90,'add'),
(2,1,2,1,49.90,'add');

INSERT INTO discount_code (code, percent)
VALUES ('TEAM08',50), ('HELLOWORLD',10);
