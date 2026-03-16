-- V2__add_customer_no_to_customers.sql
ALTER TABLE customers ADD COLUMN customer_no VARCHAR(50) UNIQUE;
CREATE INDEX idx_customers_customer_no ON customers(customer_no);
