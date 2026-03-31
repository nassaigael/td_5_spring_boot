CREATE TYPE dish_type AS ENUM ('START', 'MAIN', 'DESSERT');

CREATE TYPE ingredient_category AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

CREATE TYPE unit AS ENUM ('PCS', 'KG', 'L');

CREATE TYPE movement_type AS ENUM ('IN', 'OUT');

CREATE TABLE IF NOT EXISTS dish
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    dish_type     dish_type    NOT NULL,
    selling_price NUMERIC(10, 2)
);

CREATE TABLE IF NOT EXISTS ingredient
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(255)        NOT NULL UNIQUE,
    price         NUMERIC(10, 2)      NOT NULL,
    category      ingredient_category NOT NULL,
    initial_stock NUMERIC(10, 2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dish_ingredient
(
    id                SERIAL PRIMARY KEY,
    id_ingredient     INTEGER        NOT NULL,
    id_dish           INTEGER        NOT NULL,
    required_quantity NUMERIC(10, 2) NOT NULL DEFAULT 1,
    unit              unit           NOT NULL DEFAULT 'PCS',
    FOREIGN KEY (id_ingredient) REFERENCES ingredient (id) ON DELETE CASCADE,
    FOREIGN KEY (id_dish) REFERENCES dish (id) ON DELETE CASCADE,
    UNIQUE (id_ingredient, id_dish)
);

CREATE TABLE IF NOT EXISTS stock_movement
(
    id                SERIAL PRIMARY KEY,
    id_ingredient     INTEGER                     NOT NULL,
    quantity          NUMERIC(10, 2)              NOT NULL,
    unit              unit                        NOT NULL,
    type              movement_type               NOT NULL,
    creation_datetime TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_ingredient) REFERENCES ingredient (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "order"
(
    id                SERIAL PRIMARY KEY,
    reference         VARCHAR(255)                NOT NULL UNIQUE,
    creation_datetime TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS dish_order
(
    id       SERIAL PRIMARY KEY,
    id_order INTEGER NOT NULL,
    id_dish  INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (id_order) REFERENCES "order" (id) ON DELETE CASCADE,
    FOREIGN KEY (id_dish) REFERENCES dish (id) ON DELETE CASCADE
);