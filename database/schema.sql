-- Active: 1761737904132@@127.0.0.1@5432@ordering_platform
-- 数据库设计脚本 for PostgreSQL (V2)
-- 命名规范: 表名、字段名全小写，使用下划线分隔。
-- 核心变更：引入 menu_items 中间表，实现菜品与菜单的多对多关系。

-- ----------------------------
-- 1. 餐厅表 (无变化)
-- ----------------------------
DROP TABLE IF EXISTS restaurants CASCADE;
CREATE TABLE restaurants (
    restaurant_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE, -- 餐厅名称，唯一
    address VARCHAR(255),             -- 地址
    phone VARCHAR(50),                -- 餐厅联系电话
    logo_url VARCHAR(255),            -- Logo图片URL
    description TEXT,                 -- 餐厅描述
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE restaurants IS '餐厅信息表，是所有商户数据的根源';

-- ----------------------------
-- 2. 用户表 (无变化)
-- ----------------------------
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    restaurant_id INT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('customer', 'merchant', 'superadmin')),
    phone VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE SET NULL
);
COMMENT ON TABLE users IS '用户信息表，包含消费者、商户管理员和平台管理员';

-- ----------------------------
-- 3. 菜单表 (原categories表)
-- ----------------------------
DROP TABLE IF EXISTS menus CASCADE;
CREATE TABLE menus (
    menu_id SERIAL PRIMARY KEY,
    restaurant_id INT NOT NULL,
    name VARCHAR(100) NOT NULL, -- 例如：午市套餐, 招牌单点, 夏季特饮
    description TEXT,           -- 对菜单或套餐的描述
    is_package BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_menus_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
);
COMMENT ON TABLE menus IS '菜单（或套餐）信息表';

-- ----------------------------
-- 4. 菜品表 (重大修改)
-- ----------------------------
DROP TABLE IF EXISTS dishes CASCADE;
CREATE TABLE dishes (
    dish_id SERIAL PRIMARY KEY,
    restaurant_id INT NOT NULL,       -- 数据隔离关键字段，指明菜品归属
    name VARCHAR(255) NOT NULL,
    image_url VARCHAR(255),
    description TEXT,
    default_price DECIMAL(10, 2) NOT NULL CHECK (default_price >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- price 和 category_id 已被移除
    CONSTRAINT fk_dishes_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
);
COMMENT ON TABLE dishes IS '菜品基础信息表（原料库）';

-- ----------------------------
-- 5. 菜单项表 (全新中间表)
-- ----------------------------
DROP TABLE IF EXISTS menu_items CASCADE;
CREATE TABLE menu_items (
    menu_item_id SERIAL PRIMARY KEY,
    menu_id INT NOT NULL,
    dish_id INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity >= 0),
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_menu_items_menu FOREIGN KEY (menu_id) REFERENCES menus(menu_id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_items_dish FOREIGN KEY (dish_id) REFERENCES dishes(dish_id) ON DELETE CASCADE
    -- 已取消唯一约束以允许同一菜品在同一菜单中出现多次（可设置不同价格）
);
COMMENT ON TABLE menu_items IS '定义了哪个菜品在哪个菜单中，以及其特定价格';
COMMENT ON COLUMN menu_items.price IS '该菜品在此菜单中的特定售价';

-- ----------------------------
-- 6. 订单表 (无变化)
-- ----------------------------
DROP TABLE IF EXISTS orders CASCADE;
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    restaurant_id INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED')),
    order_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
);
COMMENT ON TABLE orders IS '订单主表';

-- ----------------------------
-- 7. 订单项表 (无变化)
-- ----------------------------
DROP TABLE IF EXISTS order_items CASCADE;
CREATE TABLE order_items (
    item_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    dish_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL, -- 下单时的单价，防止菜品价格变动影响历史订单
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_dish FOREIGN KEY (dish_id) REFERENCES dishes(dish_id)
);
COMMENT ON TABLE order_items IS '订单详情表，记录订单中的每个菜品';

-- 索引创建，优化查询性能
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_dishes_restaurant_id ON dishes(restaurant_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_menu_items_menu_id ON menu_items(menu_id);
CREATE INDEX idx_menu_items_dish_id ON menu_items(dish_id);