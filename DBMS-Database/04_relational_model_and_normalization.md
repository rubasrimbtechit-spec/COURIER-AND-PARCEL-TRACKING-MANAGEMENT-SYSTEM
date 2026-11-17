# Relational model and normalization

## Relations
- **CUSTOMERS**(`customer_id` PK, full_name, phone UNIQUE, email, address_line, city, created_at)
- **DELIVERY_PERSONNEL**(`personnel_id` PK, full_name, phone UNIQUE, service_area, active)
- **PARCELS**(`parcel_id` PK, tracking_number UNIQUE, `sender_customer_id` FK, receiver_name, receiver_phone, delivery_address, destination_city, destination_zone, weight_kg, delivery_charge, current_status, `assigned_personnel_id` FK nullable, booked_at, estimated_delivery_date, delivered_at)
- **TRACKING_HISTORY**(`history_id` PK, `parcel_id` FK, status, location, remarks, updated_at, `updated_by_personnel_id` FK nullable)
- **DELIVERY_ATTEMPTS**(`attempt_id` PK, `parcel_id` FK, attempt_no, attempted_at, outcome, notes; UNIQUE(parcel_id, attempt_no))

## Cardinalities
- One customer can send many parcels; each parcel has one sender.
- One delivery person can be assigned many parcels; a parcel has zero or one currently assigned person.
- One parcel has many tracking-history events.
- One parcel has many delivery attempts.

## Normalization
**1NF:** Each column contains a single atomic value; repeating tracking events and attempts are stored as separate rows, not multi-valued columns.

**2NF:** Every table uses a single-column primary key except that the business uniqueness of an attempt is additionally enforced by `(parcel_id, attempt_no)`. Non-key attributes depend on the whole candidate key. No partial dependency is stored in a composite-key relation.

**3NF:** Customer details, personnel details, parcel facts, tracking events and delivery attempts are separated. Customer name/phone/address are not repeated in each parcel row; personnel attributes are not stored in parcel history; event-specific data belongs to the history/attempt relations. Non-key attributes depend on the key, the whole key, and nothing but the key.

## Charge rule used by the sample application
`charge = 50.00 + (weight_kg × 10.00) + zone_surcharge`, where LOCAL = 0, REGIONAL = 15, NATIONAL = 70. This is an academic example tariff and must be changed if the faculty supplies official rates.
