-- ============================================================
-- 数据库初始化脚本（Docker MySQL 首次启动时自动执行）
-- 设计：Database per Service —— 每个服务拥有独立 schema，
-- 别的服务只能通过 HTTP API 访问，不能直接跨库查询。
-- ============================================================

-- 必须显式声明客户端字符集，否则中文种子数据会被二次编码（HEX 变成 C3A7...）
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS product_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- -------------------- user_db --------------------
USE user_db;

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(64)  NOT NULL COMMENT '登录名',
    nickname   VARCHAR(64)  NOT NULL COMMENT '昵称',
    email      VARCHAR(128) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

INSERT INTO users (username, nickname, email) VALUES
    ('alice', '爱丽丝', 'alice@example.com'),
    ('bob',   '鲍勃',   'bob@example.com')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

-- -------------------- product_db --------------------
USE product_db;

CREATE TABLE IF NOT EXISTS products (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    name       VARCHAR(128)   NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    stock      INT            NOT NULL DEFAULT 0,
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品表';

INSERT INTO products (id, name, price, stock) VALUES
    (1, '机械键盘', 399.00, 100),
    (2, '人体工学椅', 1299.00, 20)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- -------------------- order_db --------------------
USE order_db;

CREATE TABLE IF NOT EXISTS orders (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    order_no   VARCHAR(64)    NOT NULL,
    user_id    BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   INT            NOT NULL,
    amount     DECIMAL(10, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL DEFAULT 'CREATED',
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_no (order_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';
