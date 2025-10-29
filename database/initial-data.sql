-- ====================================================================
-- 项目初始数据脚本 (V3)
-- 适配V2版本Schema，使用menu/dish/menu_items结构
-- ====================================================================

-- 清理旧数据，以便重复执行此脚本
TRUNCATE TABLE restaurants, users, menus, dishes, menu_items, orders, order_items RESTART IDENTITY CASCADE;

-- ====================================================================
-- 餐厅与商户用户
-- ====================================================================
-- 餐厅 1: 测试餐厅 (ID=1), 商户: testmerchant (ID=1)
WITH new_restaurant AS (
    INSERT INTO restaurants (name, description, address, phone)
    VALUES ('测试餐厅', '这是一家用于测试的餐厅', '虚拟地址123号', '010-88886666')
    RETURNING restaurant_id
)
INSERT INTO users (restaurant_id, username, password, role, phone)
SELECT restaurant_id, 'testmerchant', 'password123', 'merchant', '13800138001' FROM new_restaurant;

-- 餐厅 2: 美味快餐店 (ID=2), 商户: merchant2 (ID=2)
WITH new_restaurant AS (
    INSERT INTO restaurants (name, description, address, phone)
    VALUES ('美味快餐店', '提供快速、美味的简餐', '美食街456号', '021-99997777')
    RETURNING restaurant_id
)
INSERT INTO users (restaurant_id, username, password, role, phone)
SELECT restaurant_id, 'merchant2', 'password123', 'merchant', '13900139002' FROM new_restaurant;

-- ====================================================================
-- 消费者用户
-- ====================================================================
-- 消费者 1: testcustomer (ID=3)
INSERT INTO users (username, password, role, phone)
VALUES ('testcustomer', 'password123', 'customer', '13700137003');

-- ====================================================================
-- 菜品库 (Dishes - Raw Materials)
-- ====================================================================
-- 餐厅 1 的菜品库
INSERT INTO dishes (restaurant_id, name, description)
VALUES
    (1, '红烧牛肉面', '精选牛腩，慢火熬制，汤头浓郁'), -- dish_id=1
    (1, '老坛酸菜鱼', '酸爽开胃，鱼肉嫩滑'),         -- dish_id=2
    (1, '香酥鸡排', '外酥里嫩，鲜嫩多汁'),         -- dish_id=3
    (1, '冰镇可乐', '经典碳酸饮料'),             -- dish_id=4
    (1, '鲜榨西瓜汁', '夏季解暑必备');             -- dish_id=5

-- 餐厅 2 的菜品库
INSERT INTO dishes (restaurant_id, name, description)
VALUES
    (2, '双层芝士牛肉堡', '双倍牛肉，双倍满足'), -- dish_id=6
    (2, '香辣鸡腿堡', '整块鸡腿肉，香辣过瘾'),   -- dish_id=7
    (2, '美式薯条', '外脆内绵，盐香四溢'),     -- dish_id=8
    (2, '冰镇可乐', '经典碳酸饮料');             -- dish_id=9 (注意：这是餐厅2的可乐)

-- ====================================================================
-- 菜单与菜单项 (Menus and Menu Items)
-- ====================================================================
-- 餐厅 1 的菜单
INSERT INTO menus (restaurant_id, name, description)
VALUES
    (1, '招牌单点', '本店所有招牌菜品均可在此单点'), -- menu_id=1
    (1, '工作日午市套餐', '牛肉面+可乐，实惠之选');    -- menu_id=2

-- 为餐厅 1 的菜单添加菜品和价格
INSERT INTO menu_items (menu_id, dish_id, price)
VALUES
    -- “招牌单点” 菜单中的菜品
    (1, 1, 32.00), -- 红烧牛肉面
    (1, 2, 28.00), -- 老坛酸菜鱼
    (1, 3, 15.00), -- 香酥鸡排
    (1, 4, 6.00),  -- 冰镇可乐
    (1, 5, 12.00), -- 鲜榨西瓜汁
    -- “工作日午市套餐” 菜单中的菜品 (价格可能不同)
    (2, 1, 30.00), -- 套餐里的牛肉面，优惠2元
    (2, 4, 4.00);  -- 套餐里的可乐，优惠2元

-- 餐厅 2 的菜单
INSERT INTO menus (restaurant_id, name, description)
VALUES
    (2, '经典汉堡单点', '所有汉堡均可单点'); -- menu_id=3

-- 为餐厅 2 的菜单添加菜品和价格
INSERT INTO menu_items (menu_id, dish_id, price)
VALUES
    (3, 6, 25.00), -- 双层芝士牛肉堡
    (3, 7, 22.00), -- 香辣鸡腿堡
    (3, 8, 12.00), -- 美式薯条
    (3, 9, 5.00);  -- 冰镇可乐

-- ====================================================================
-- 历史订单
-- ====================================================================
-- 消费者(user_id=3) 在 餐厅1(restaurant_id=1) 点了一份“工作日午市套餐”
-- total_price = 30.00 * 1 (套餐牛肉面) + 4.00 * 1 (套餐可乐) = 34.00
INSERT INTO orders (user_id, restaurant_id, total_price, status)
VALUES (3, 1, 34.00, 'COMPLETED'); -- order_id=1

-- 订单项
INSERT INTO order_items (order_id, dish_id, quantity, unit_price)
VALUES
    (1, 1, 1, 30.00), -- 1份牛肉面，下单时快照单价为套餐价30.00
    (1, 4, 1, 4.00);  -- 1份可乐，下单时快照单价为套餐价4.00