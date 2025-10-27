-- 数据库设计脚本 for PostgreSQL
-- 命名规范: 表名、字段名全小写，使用下划线分隔。

-- ----------------------------
-- 1. 餐厅表 (多租户的核心)
-- ----------------------------
DROP TABLE IF EXISTS restaurants CASCADE;
CREATE TABLE restaurants (
    restaurant_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE, -- 餐厅名称，唯一
    address VARCHAR(255),             -- 地址
    logo_url VARCHAR(255),            -- Logo图片URL
    description TEXT,                 -- 餐厅描述
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE restaurants IS '餐厅信息表，是所有商户数据的根源';
COMMENT ON COLUMN restaurants.restaurant_id IS '餐厅唯一ID';

-- ----------------------------
-- 2. 用户表 (包含所有角色)
-- ----------------------------
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    restaurant_id INT,                -- 外键，关联到restaurants表
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,   -- 实际项目中应存储加密后的哈希值
    role VARCHAR(20) NOT NULL CHECK (role IN ('customer', 'merchant', 'superadmin')),
    phone VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE SET NULL
);
COMMENT ON TABLE users IS '用户信息表，包含消费者、商户管理员和平台管理员';
COMMENT ON COLUMN users.restaurant_id IS '商户管理员所属的餐厅ID, 消费者或平台管理员此项为NULL';
COMMENT ON COLUMN users.role IS '用户角色: customer(消费者), merchant(商户), superadmin(平台管理员)';

-- ----------------------------
-- 3. 菜品分类表
-- ----------------------------
DROP TABLE IF EXISTS categories CASCADE;
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    restaurant_id INT NOT NULL,       -- 数据隔离关键字段
    name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_categories_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
);
COMMENT ON TABLE categories IS '菜品分类表';
COMMENT ON COLUMN categories.restaurant_id IS '分类所属的餐厅ID';

-- ----------------------------
-- 4. 菜品表
-- ----------------------------
DROP TABLE IF EXISTS dishes CASCADE;
CREATE TABLE dishes (
    dish_id SERIAL PRIMARY KEY,
    category_id INT NOT NULL,
    restaurant_id INT NOT NULL,       -- 数据隔离关键字段
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    image_url VARCHAR(255),
    description TEXT,
    is_available BOOLEAN DEFAULT TRUE, -- 是否上架
    CONSTRAINT fk_dishes_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE,
    CONSTRAINT fk_dishes_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
);
COMMENT ON TABLE dishes IS '菜品信息表';
COMMENT ON COLUMN dishes.restaurant_id IS '菜品所属的餐厅ID';
COMMENT ON COLUMN dishes.is_available IS '标记菜品是否对消费者可见';

-- ----------------------------
-- 5. 订单表
-- ----------------------------
DROP TABLE IF EXISTS orders CASCADE;
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,             -- 下单的消费者ID
    restaurant_id INT NOT NULL,       -- 订单所属的餐厅ID
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED')),
    order_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
);
COMMENT ON TABLE orders IS '订单主表';
COMMENT ON COLUMN orders.status IS '订单状态: PENDING(待处理), PROCESSING(制作中), COMPLETED(已完成), CANCELLED(已取消)';

-- ----------------------------
-- 6. 订单项表 (一个订单包含多个菜品)
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
COMMENT ON COLUMN order_items.unit_price IS '下单时的菜品单价快照';

-- 索引创建，优化查询性能
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_dishes_restaurant_id ON dishes(restaurant_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
