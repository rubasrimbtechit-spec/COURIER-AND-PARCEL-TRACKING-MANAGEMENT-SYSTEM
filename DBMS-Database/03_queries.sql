USE courier_tracking;
-- Track one parcel and view its event timeline
SELECT p.tracking_number, p.current_status, h.location, h.remarks, h.updated_at
FROM parcels p JOIN tracking_history h ON h.parcel_id=p.parcel_id
WHERE p.tracking_number='CP202611150002' ORDER BY h.updated_at;

-- Shipment count by status
SELECT current_status, COUNT(*) AS shipment_count
FROM parcels GROUP BY current_status ORDER BY shipment_count DESC;

-- Monthly shipment volume
SELECT DATE_FORMAT(booked_at,'%Y-%m') AS month, COUNT(*) AS shipment_count
FROM parcels GROUP BY DATE_FORMAT(booked_at,'%Y-%m') ORDER BY month;

-- Delivery completion rate
SELECT COUNT(*) AS total_shipments,
       SUM(current_status='DELIVERED') AS delivered,
       ROUND(100.0*SUM(current_status='DELIVERED')/NULLIF(COUNT(*),0),2) AS completion_pct
FROM parcels;

-- Delivery delay in days (only parcels with an actual delivery date)
SELECT tracking_number, DATEDIFF(DATE(delivered_at),estimated_delivery_date) AS delay_days
FROM parcels WHERE delivered_at IS NOT NULL AND estimated_delivery_date IS NOT NULL;

-- Delivery attempts per parcel
SELECT p.tracking_number, COUNT(a.attempt_id) AS attempts
FROM parcels p LEFT JOIN delivery_attempts a ON a.parcel_id=p.parcel_id
GROUP BY p.parcel_id,p.tracking_number;
