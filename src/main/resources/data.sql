-- INSERT INTO orders (
--     orderId,          -- PK (camelCase)
--     orderNumber,      -- camelCase
--     orderStatus,      -- ENUM 문자열 (PENDING 등)
--     orderDate,        -- timestamp
--     ordererName,      -- camelCase
--     deliverAddress,   -- camelCase
--     postalCode,       -- camelCase
--     totalPrice        -- camelCase
-- ) VALUES (
--              1001,
--              1001,
--              'PENDING',
--              CURRENT_TIMESTAMP,
--              '홍길동',
--              '서울시 어딘가 1',
--              '12345',
--              50000
--          );
--

--주문정보는 결제후에 출력

-- 애플리케이션 시작 시 자동으로 실행되어 초기 데이터를 넣어줍니다.
INSERT INTO DeliveryPolicies (delivery_policy_name, delivery_fee)
VALUES ('DEFAULT', 3000);

INSERT INTO Packaging (wrapping_paper_name, wrapping_paper_price)
VALUES ('NORMAL', 0);

INSERT INTO Packaging (wrapping_paper_name, wrapping_paper_price)
VALUES ('GIFT', 1500);
