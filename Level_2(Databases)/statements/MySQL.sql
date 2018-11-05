/* 1. Создать базу данных shop. */
CREATE DATABASE shop;
use shop;

/* 2. Создать юзера shop и дать ему полный доступ к БД shop. */
GRANT ALL PRIVILEGES ON shop.* TO 'shop'@'localhost' IDENTIFIED BY 'pass';
FLUSH PRIVILEGES;

/* 3. Создать юзера viewer и дать ему доступ на чтение БД shop. */
GRANT SELECT ON shop.* TO 'viewer'@'localhost' IDENTIFIED BY 'pass';
FLUSH PRIVILEGES;

/* 4. Создать таблицу для хранения категорий (хранить название). */
CREATE TABLE categories (
    title VARCHAR(10)
);

/* 5. Добавить несколько категорий. */
INSERT INTO categories VALUES
    ('fruit'),
    ('vegetables');

/* 6. Создать таблицу для хранения товаров (название, категория, цена). */
CREATE TABLE items (
    title VARCHAR(10),
    category VARCHAR(10),
    price REAL(16,4)
);

/* 7. Внести несколько товаров по цене 1.00 */
INSERT INTO items VALUES
    ('apple', 'fruit', 1.00),
    ('potato', 'vegetables', 1.00),
    ('peach', 'fruit', 1.00),
    ('tomato', 'vegetables', 1.00),
    ('pear', 'fruit', 1.00),
    ('pineapple', 'fruit', 1.00),
    ('carrot', 'vegetables', 1.00);

/* 8. Обновить цену первого товара — 3.50 */
UPDATE items
SET price = 3.50
WHERE title = 'apple';

/* 9. Увеличить цену всех товаров на 10%. */
UPDATE items
SET price = price * 1.1;

/* 10. Удалить товар № 2. */
DELETE FROM items
WHERE title = 'potato'; 

/* 11. Выбрать все товары с сортировкой по названию. */
SELECT * FROM items
ORDER BY title;

/* 12. Выбрать все товары с сортировкой по убыванию цены. */
SELECT * FROM items
ORDER BY price DESC;

/* 13. Выбрать 3 самых дорогих товара. */
SELECT * FROM items
ORDER BY price DESC
LIMIT 3;

/* 14. Выбрать 3 самых дешевых товара. */
SELECT * FROM items
ORDER BY price
LIMIT 3;

/* 15. Выбрать вторую тройку самых дорогих товаров (с 4 по 6). */
SELECT * FROM items
ORDER BY price DESC
LIMIT 3, 3;

/* 16. Выбрать наименование самого дорогого товара. */
SELECT title FROM items
WHERE price = (SELECT MAX(price) FROM items);

/* 17. Выбрать наименование самого дешевого товара. */
SELECT title FROM items
WHERE price = (SELECT MIN(price) FROM items);

/* 18. Выбрать количество всех товаров. */
SELECT COUNT(*) FROM items;

/* 19. Выбрать среднюю цену всех товаров. */
SELECT AVG(price) FROM items;

/* 20. Создать представление (create view) с отображением 3 самых дорогих товаров. */
CREATE VIEW top3_price AS
SELECT * FROM items
ORDER BY price DESC
LIMIT 3;
