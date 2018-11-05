
        -- Вывести все товары и категорию, в которой они находятся.
SELECT "item_name", "category_title"
FROM "item" NATURAL JOIN "category";
    
    -- Вывести все товары из конкретного заказа.
SELECT "item_name"
FROM "item" NATURAL JOIN "item__order"
WHERE "order_id" = ?;

    -- Вывести все заказы с конкретной единицей товара.
SELECT "order_id", "order_address", "order_description"
FROM "order" NATURAL JOIN "item__order"
WHERE "item_id" = ?;

    -- Вывести все товары, заказанные за последний час.
SELECT "item_name" 
FROM "item"
NATURAL JOIN "item__order"
NATURAL JOIN "order"
WHERE "order_updated" >= (now() - interval '1 hour');

    -- Вывести все товары, заказанные за сегодня.
SELECT "item_name" 
FROM "item"
NATURAL JOIN "item__order"
NATURAL JOIN "order"
WHERE "order_updated" >= TIMESTAMP 'today';

    -- Вывести все товары, заказанные за вчера.
SELECT "item_name" 
FROM "item"
NATURAL JOIN "item__order"
NATURAL JOIN "order"
WHERE "order_updated" >= TIMESTAMP 'yesterday'
AND "order_updated" < TIMESTAMP 'today';

    -- Вывести все товары из заданной категории, заказанные за последний час.
SELECT "item_name"
FROM "item"
NATURAL JOIN "item__order"
NATURAL JOIN "order"
WHERE "order_updated" >= (now() - interval '1 hour')
AND "category_id" = ?;

    -- Вывести все товары из заданной категории, заказанные за сегодня.
SELECT "item_name"
FROM "item"
NATURAL JOIN "item__order"
NATURAL JOIN "order"
WHERE "order_updated" >= TIMESTAMP 'today'
AND "category_id" = ?;

    -- Вывести все товары из заданной категории, заказанные за вчера.
SELECT "item_name"
FROM "item"
NATURAL JOIN "item__order"
NATURAL JOIN "order"
WHERE "order_updated" >= TIMESTAMP 'yesterday'
AND "order_updated" < TIMESTAMP 'today'
AND "category_id" = ?;

    -- Вывести все товары, названия которых начинаются с заданной последовательности букв (см. LIKE).
SELECT "item_name"
FROM "item"
WHERE "item_name" LIKE '?%';

    -- Вывести все товары, названия которых заканчиваются заданной последовательностью букв (см. LIKE).
SELECT "item_name"
FROM "item"
WHERE "item_name" LIKE '%?';

    -- Вывести все товары, названия которых содержат заданные последовательности букв (см. LIKE).
SELECT "item_name"
FROM "item"
WHERE "item_name" LIKE '%?%';

    -- Вывести список категорий и количество товаров в каждой категории.
SELECT "category_title", COUNT(*) AS "item_quantity"
FROM "category"
NATURAL JOIN "item"
GROUP BY "category_title";

    -- Вывести список всех заказов и количество товаров в каждом.
SELECT "order_id", "order_address", "order_description", SUM("item__order_quantity") AS "item_quantity"
FROM "order"
NATURAL JOIN "item__order"
GROUP BY "order_id";

    -- Вывести список всех товаров и количество заказов, в которых имеется этот товар.
SELECT "item_name", COUNT("order_id") AS "order_quantity"
FROM "item"
NATURAL LEFT JOIN "item__order"
GROUP BY "item_name";

    -- Вывести список заказов, упорядоченный по дате заказа и суммарную стоимость товаров в каждом из них.
SELECT "order_id", SUM("item_price" * "item__order_quantity") AS "order_price"
FROM "order"
NATURAL JOIN "item__order"
NATURAL JOIN "item"
GROUP BY "order_id"
ORDER BY "order_updated";

    -- Вывести список товаров, цену, количество и суммарную стоимость каждого из них в заказе с заданным ID.
SELECT "item_name" AS "title",
       "item_price" AS "price",
       "item__order_quantity" AS "quantity",
       ("item_price" * "item__order_quantity") AS "total price"
FROM "item"
NATURAL JOIN "item__order"
WHERE "order_id" = ?;
    
    -- Для заданного ID заказа вывести список категорий, товары из которых присутствуют в этом заказе. Для каждой из категорий вывести суммарное количество и суммарную стоимость товаров.
SELECT "category_title",
       SUM("item__order_quantity") AS "item_quantity",
       SUM("item_price" * "item__order_quantity") AS "total price"
FROM "category"
NATURAL JOIN "item"
NATURAL JOIN "item__order"
WHERE "order_id" = ?
GROUP BY "category_title";

    -- Вывести список клиентов, которые заказывали товары из категории с заданным ID за последние 3 дня.
SELECT "customer_name"
FROM "customer"
NATURAL JOIN "order"
NATURAL JOIN "item__order"
NATURAL JOIN "item"
WHERE "category_id" = ?
AND "order_updated" >= (now() - interval '3 days')
GROUP BY "customer_name";

    -- Вывести имена всех клиентов, производивших заказы за последние сутки.
SELECT "customer_name"
FROM "customer"
NATURAL JOIN "order"
WHERE "order_updated" >= (now() - interval '24 hours')
GROUP BY "customer_name";

    -- Вывести всех клиентов, производивших заказы, содержащие товар с заданным ID.
SELECT "customer_name"
FROM "customer"
NATURAL JOIN "order"
NATURAL JOIN "item__order"
WHERE "item_id" = ?
GROUP BY "customer_name";

    /*Для каждой категории вывести урл загрузки изображения с именем category_image в формате 
    'http://img.domain.com/category/<category_id>.jpg' для включенных категорий, и 
    'http://img.domain.com/category/<category_id>_disabled.jpg' для выключеных.*/
SELECT "category_title",
       (CASE "category_enabled" WHEN TRUE 
           THEN format('http://img.domain.com/category/%s.jpg', category_id)
           ELSE format('http://img.domain.com/category/%s_disabled.jpg', category_id)
       END) AS "category_image"
FROM "category";


    -- Для товаров, которые были заказаны за все время во всех заказах общим количеством более X единиц, установить item_popular = TRUE, для остальных — FALSE.
UPDATE "item"
SET "item_popular" = CASE
    WHEN res.total_quantity > ? THEN TRUE
        ELSE FALSE
    END
FROM (
    SELECT "item_id", SUM("item__order_quantity") AS "total_quantity"
    FROM "item"
    NATURAL LEFT JOIN "item__order"
    GROUP BY "item_id"
) "res"
WHERE item.item_id = res.item_id;

    -- Одним запросом для указанных ID категорий установить флаг category_enabled = TRUE, для остальных — FALSE. Не применять WHERE.
UPDATE "category"
SET "category_enabled" = CASE
    WHEN "category_id" IN (?) THEN TRUE
        ELSE FALSE
    END;
    
    -- Или.
    
UPDATE "category" SET "category_enabled" = "category_id" IN (?);
