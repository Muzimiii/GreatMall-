CREATE DATABASE IF NOT EXISTS greatmall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE greatmall;

DROP TABLE IF EXISTS mall_order;
DROP TABLE IF EXISTS seckill_activity;
DROP TABLE IF EXISTS product;

CREATE TABLE product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    subtitle VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    seckill_price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    sold_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    cover_image VARCHAR(512),
    version INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE seckill_activity (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_id (product_id)
);

CREATE TABLE mall_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_amount DECIMAL(10, 2) NOT NULL,
    status TINYINT NOT NULL,
    source VARCHAR(32) NOT NULL DEFAULT 'SECKILL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id),
    KEY idx_status (status)
);

INSERT INTO product (id, name, subtitle, description, price, seckill_price, stock, sold_count, status, cover_image, version)
VALUES
    (101, 'RTX 级电竞显卡', '热门大促显卡，适合高并发秒杀演示', '展示 Redis 预扣减、异步下单、订单超时释放的完整链路。', 3299.00, 2699.00, 120, 0, 1, 'https://images.unsplash.com/photo-1591489378430-ef2f4c626b35?auto=format&fit=crop&w=1200&q=80', 0),
    (102, '旗舰机械键盘', '客单价适中，便于观察待支付与支付流转', '下单成功后可在前端模拟支付，验证死信取消不会误关已支付订单。', 699.00, 399.00, 300, 0, 1, 'https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=1200&q=80', 0),
    (103, '4K 曲面显示器', '用于缓存热点详情与布隆过滤示例', '商品详情默认走缓存查询，并设置随机 TTL 以降低缓存雪崩风险。', 2499.00, 1999.00, 80, 0, 1, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=1200&q=80', 0);

INSERT INTO seckill_activity (id, product_id, start_time, end_time, status)
VALUES
    (201, 101, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),
    (202, 102, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),
    (203, 103, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);

