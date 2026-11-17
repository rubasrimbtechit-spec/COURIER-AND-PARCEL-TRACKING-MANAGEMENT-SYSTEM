-- Non-destructive schema setup. Existing courier_tracking records are preserved.
CREATE DATABASE IF NOT EXISTS courier_tracking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE courier_tracking;
CREATE TABLE IF NOT EXISTS customers (
 customer_id BIGINT PRIMARY KEY AUTO_INCREMENT, full_name VARCHAR(100) NOT NULL,
 phone VARCHAR(20) NOT NULL UNIQUE, email VARCHAR(120), address_line VARCHAR(200) NOT NULL,
 city VARCHAR(80) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS delivery_personnel (
 personnel_id BIGINT PRIMARY KEY AUTO_INCREMENT, full_name VARCHAR(100) NOT NULL,
 phone VARCHAR(20) NOT NULL UNIQUE, service_area VARCHAR(100) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS parcels (
 parcel_id BIGINT PRIMARY KEY AUTO_INCREMENT, tracking_number VARCHAR(32) NOT NULL UNIQUE,
 sender_customer_id BIGINT NOT NULL, receiver_name VARCHAR(100) NOT NULL, receiver_phone VARCHAR(20) NOT NULL,
 delivery_address VARCHAR(250) NOT NULL, destination_city VARCHAR(80) NOT NULL,
 destination_zone ENUM('LOCAL','REGIONAL','NATIONAL') NOT NULL, weight_kg DECIMAL(8,2) NOT NULL,
 delivery_charge DECIMAL(10,2) NOT NULL,
 current_status ENUM('BOOKED','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED','DELIVERY_FAILED','RETURNED') NOT NULL DEFAULT 'BOOKED',
 assigned_personnel_id BIGINT NULL, booked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 estimated_delivery_date DATE NULL, delivered_at DATETIME NULL,
 CONSTRAINT fk_parcel_sender FOREIGN KEY(sender_customer_id) REFERENCES customers(customer_id),
 CONSTRAINT fk_parcel_personnel FOREIGN KEY(assigned_personnel_id) REFERENCES delivery_personnel(personnel_id),
 CONSTRAINT chk_weight CHECK(weight_kg>0), CONSTRAINT chk_charge CHECK(delivery_charge>=0),
 INDEX idx_parcel_status(current_status), INDEX idx_parcel_booked(booked_at)
);
CREATE TABLE IF NOT EXISTS tracking_history (
 history_id BIGINT PRIMARY KEY AUTO_INCREMENT, parcel_id BIGINT NOT NULL,
 status ENUM('BOOKED','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED','DELIVERY_FAILED','RETURNED') NOT NULL,
 location VARCHAR(120) NOT NULL, remarks VARCHAR(250), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_by_personnel_id BIGINT NULL,
 CONSTRAINT fk_history_parcel FOREIGN KEY(parcel_id) REFERENCES parcels(parcel_id) ON DELETE CASCADE,
 CONSTRAINT fk_history_personnel FOREIGN KEY(updated_by_personnel_id) REFERENCES delivery_personnel(personnel_id),
 INDEX idx_history_parcel_time(parcel_id,updated_at)
);
CREATE TABLE IF NOT EXISTS delivery_attempts (
 attempt_id BIGINT PRIMARY KEY AUTO_INCREMENT, parcel_id BIGINT NOT NULL, attempt_no INT NOT NULL,
 attempted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 outcome ENUM('SUCCESS','RECIPIENT_UNAVAILABLE','ADDRESS_NOT_FOUND','OTHER') NOT NULL, notes VARCHAR(250),
 CONSTRAINT fk_attempt_parcel FOREIGN KEY(parcel_id) REFERENCES parcels(parcel_id) ON DELETE CASCADE,
 CONSTRAINT uq_attempt_number UNIQUE(parcel_id,attempt_no)
);
CREATE OR REPLACE VIEW shipment_report AS
SELECT p.parcel_id,p.tracking_number,c.full_name sender_name,p.destination_city,p.destination_zone,
 p.weight_kg,p.delivery_charge,p.current_status,p.booked_at,p.estimated_delivery_date,p.delivered_at,
 dp.full_name assigned_personnel FROM parcels p JOIN customers c ON c.customer_id=p.sender_customer_id
 LEFT JOIN delivery_personnel dp ON dp.personnel_id=p.assigned_personnel_id;
