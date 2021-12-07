CREATE TABLE users
(
    passport_id BIGINT PRIMARY KEY,
    name        VARCHAR(20)  NOT NULL,
    keycloak_id VARCHAR(128) NOT NULL
);

CREATE TABLE cars
(
    id              SERIAL PRIMARY KEY,
    model           VARCHAR(50)    NOT NULL,
    manufacturer    VARCHAR(50)    NOT NULL,
    uah_per_day     DECIMAL(10, 2) NOT NULL,
    thumbnail_url   TEXT           NOT NULL,
    current_user_id BIGINT         REFERENCES users (passport_id) ON DELETE SET NULL,
    description     TEXT           NOT NULL,
    uah_purchase    DECIMAL(10, 2) NOT NULL
);

CREATE TABLE requests
(
    id           SERIAL PRIMARY KEY,
    status       INTEGER NOT NULL DEFAULT 0,
    message      TEXT             DEFAULT NULL,
    user_id      BIGINT  REFERENCES users (passport_id) ON DELETE SET NULL,
    car_id       INT     REFERENCES cars (id) ON DELETE SET NULL,
    days         INT     NOT NULL,
    start_date   DATE    NOT NULL,
    payment_cost DECIMAL(10, 2)
);

CREATE TABLE payments
(
    id         SERIAL PRIMARY KEY,
    uah_amount DECIMAL(10, 2) NOT NULL,
    request_id INTEGER        REFERENCES requests (id) ON DELETE SET NULL,
    type       INTEGER        NOT NULL,
    car_id     INTEGER        REFERENCES cars (id) ON DELETE SET NULL,
    time       TIMESTAMP      NOT NULL
);

INSERT INTO cars
VALUES (DEFAULT, 'Focus (2011)', 'Ford', 300,
        'https://upload.wikimedia.org/wikipedia/commons/0/05/Ford_Focus_Trend_%28III%29_%E2%80%93_Frontansicht%2C_17._September_2011%2C_Ratingen.jpg',
        NULL, 'A modern European hatchback.', 50000);
INSERT INTO cars
VALUES (DEFAULT, 'GT Mk. II', 'Ford', 2000, 'https://upload.wikimedia.org/wikipedia/commons/4/46/2018_Ford_GT.jpg',
        NULL,
        'The Ford GT is a mid-engine two-seater supercar manufactured and marketed by American automobile manufacturer Ford for the 2005 model year. The second generation Ford GT became available for the 2017 model year.',
        4000000);
INSERT INTO cars
VALUES (DEFAULT, 'Lanos', 'Daewoo', 250,
        'https://upload.wikimedia.org/wikipedia/commons/0/0e/1997_Daewoo_Lanos_%28T100%29_SE_sedan_%282010-09-23%29.jpg',
        NULL, 'A classic car.', 10000)