INSERT INTO orders (
    orderId,          -- PK (camelCase)
    orderNumber,      -- camelCase
    orderStatus,      -- ENUM 문자열 (PENDING 등)
    orderDate,        -- timestamp
    ordererName,      -- camelCase
    deliverAddress,   -- camelCase
    postalCode,       -- camelCase
    totalPrice        -- camelCase
) VALUES (
             1001,
             1001,
             'PENDING',
             CURRENT_TIMESTAMP,
             '홍길동',
             '서울시 어딘가 1',
             '12345',
             50000
         );


--주문정보는 결제후에 출력
