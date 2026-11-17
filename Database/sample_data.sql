USE courier_tracking;
INSERT IGNORE INTO customers(customer_id,full_name,phone,email,address_line,city) VALUES
(1,'Ananya Kumar','9000000001','ananya@example.com','12 Lake Road','Erode'),
(2,'Kavin Raj','9000000002','kavin@example.com','44 Park Street','Coimbatore'),
(3,'Meera S','9000000003','meera@example.com','8 Market Road','Salem');
INSERT IGNORE INTO delivery_personnel(personnel_id,full_name,phone,service_area,active) VALUES
(1,'Arun Delivery','9100000001','Erode',TRUE),(2,'Bala Delivery','9100000002','Coimbatore',TRUE);
INSERT IGNORE INTO parcels(tracking_number,sender_customer_id,receiver_name,receiver_phone,delivery_address,destination_city,destination_zone,weight_kg,delivery_charge,current_status,assigned_personnel_id,booked_at,estimated_delivery_date) VALUES
('CP202611150001',1,'Priya','9200000001','10 Main Road','Erode','LOCAL',1.50,65.00,'DELIVERED',1,'2026-11-01 09:00:00','2026-11-03'),
('CP202611150002',2,'Ramesh','9200000002','20 Hill Road','Chennai','NATIONAL',2.00,140.00,'IN_TRANSIT',2,'2026-11-05 10:30:00','2026-11-10'),
('CP202611150003',3,'Nila','9200000003','7 River Street','Salem','REGIONAL',0.75,72.50,'OUT_FOR_DELIVERY',1,'2026-11-08 14:15:00','2026-11-10');
INSERT INTO tracking_history(parcel_id,status,location,remarks,updated_at,updated_by_personnel_id)
SELECT p.parcel_id,'BOOKED','Erode','Parcel booked','2026-11-01 09:00:00',NULL FROM parcels p WHERE p.tracking_number='CP202611150001' AND NOT EXISTS(SELECT 1 FROM tracking_history h WHERE h.parcel_id=p.parcel_id);
INSERT INTO tracking_history(parcel_id,status,location,remarks,updated_at,updated_by_personnel_id)
SELECT p.parcel_id,'IN_TRANSIT','Coimbatore Hub','Dispatched to destination','2026-11-06 08:00:00',2 FROM parcels p WHERE p.tracking_number='CP202611150002' AND NOT EXISTS(SELECT 1 FROM tracking_history h WHERE h.parcel_id=p.parcel_id);
INSERT IGNORE INTO delivery_attempts(parcel_id,attempt_no,outcome,notes,attempted_at)
SELECT parcel_id,1,'SUCCESS','Delivered to recipient','2026-11-03 16:20:00' FROM parcels WHERE tracking_number='CP202611150001';

INSERT INTO tracking_history(parcel_id,status,location,remarks,updated_at,updated_by_personnel_id)
SELECT p.parcel_id,'PICKED_UP','Erode','Collected from sender','2026-11-01 12:00:00',1 FROM parcels p
WHERE p.tracking_number='CP202611150001' AND NOT EXISTS(SELECT 1 FROM tracking_history h WHERE h.parcel_id=p.parcel_id AND h.status='PICKED_UP');
INSERT INTO tracking_history(parcel_id,status,location,remarks,updated_at,updated_by_personnel_id)
SELECT p.parcel_id,'DELIVERED','Erode','Handed to recipient','2026-11-03 16:20:00',1 FROM parcels p
WHERE p.tracking_number='CP202611150001' AND NOT EXISTS(SELECT 1 FROM tracking_history h WHERE h.parcel_id=p.parcel_id AND h.status='DELIVERED');
INSERT INTO tracking_history(parcel_id,status,location,remarks,updated_at,updated_by_personnel_id)
SELECT p.parcel_id,'OUT_FOR_DELIVERY','Salem','With delivery personnel','2026-11-10 09:00:00',1 FROM parcels p
WHERE p.tracking_number='CP202611150003' AND NOT EXISTS(SELECT 1 FROM tracking_history h WHERE h.parcel_id=p.parcel_id AND h.status='OUT_FOR_DELIVERY');
