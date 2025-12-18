
INSERT INTO CUSTOMERS (id, name, address, income, email, phone) VALUES
(1, 'Alice Gupta', 'Pune', 1200000, 'alice@example.com', '+91-9876543210'),
(2, 'Rahul Verma', 'Mumbai', 800000, 'rahul@example.com', '+91-9123456789');

INSERT INTO ACCOUNTS (id, account_no, type, balance, customer_id) VALUES
(1, 'SB-0001', 'SAVINGS', 50000, 1),
(2, 'CA-0002', 'CURRENT', 200000, 1),
(3, 'SB-0003', 'SAVINGS', 35000, 2);

INSERT INTO PRODUCTS (id, product_code, name, details) VALUES
(1, 'PR-SAV', 'Premium Savings', 'Higher interest tier'),
(2, 'PR-LOAN', 'Home Loan', 'Competitive rate');

INSERT INTO CUSTOMER_OPPORTUNITIES (id, customer_id, product_id, assigned_staff, status) VALUES
(1, 1, 2, 'NiMr X', 'NEW'),
(2, 2, 1, 'Ajent Y', 'CONTACTED');

INSERT INTO TRANSACTIONS (amount, account_id, created_at, id, description) VALUES
(1000, 1, '2024-01-15 10:30:00', 1, 'DEPOSIT'),
(5000, 1, '2024-01-16 14:20:00', 2, 'WITHDRAWAL'),
(50000, 2, '2024-01-17 09:15:00', 3, 'DEPOSIT');