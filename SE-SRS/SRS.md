# COURIER AND PARCEL TRACKING MANAGEMENT SYSTEM
## Software Requirements Specification — Scenario 26

### 1. Introduction
This SRS describes the academic Courier and Parcel Tracking Management System. The repository now contains a Spring Boot web application with a responsive HTML/CSS/JavaScript interface and REST API, while retaining the original Java console program as a supplementary deliverable.

### 2. Scope and users
The system manages customer records, parcel booking, delivery charges, personnel assignments, status changes, tracking history, delivery attempts and shipment reports. Administrators/operators manage records. Tracking users can query a parcel by tracking number. Authentication and role-based authorization are not implemented in this academic version.

### 3. Technology and operating environment
- Java 17, Spring Boot 3, Maven
- MySQL 8+
- HTML5, CSS3, JavaScript
- REST/JSON API
- Optional Docker deployment; Python analysis coursework remains in FDS-Python and FDS-Visualization.

### 4. Functional requirements
FR-01 Customer management: create and list customers; phone is unique.
FR-02 Parcel registration: capture sender, receiver, delivery address, destination city/zone and weight.
FR-03 Tracking number: generate unique tracking number.
FR-04 Delivery charge: base ₹50 + ₹10/kg + zone surcharge (LOCAL ₹0, REGIONAL ₹15, NATIONAL ₹70).
FR-05 Personnel management and assignment: create/list delivery personnel and assign a person to a parcel.
FR-06 Status management: validate status from BOOKED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, DELIVERY_FAILED, RETURNED.
FR-07 Tracking history: registration and every status update append a timestamped location/status event.
FR-08 Delivery attempts: record attempt number, time, outcome and notes; attempt number unique per parcel.
FR-09 Tracking: retrieve current parcel status, chronological tracking history and delivery attempts.
FR-10 Reports: summarize parcels by status, destination city and booking month; show recent attempts.
FR-11 Dashboard: display counts, recent shipments and status summary.
FR-12 Python data retrieval/preprocessing and visualization: original coursework scripts are retained.

### 5. Data requirements
Tables: customers, delivery_personnel, parcels, tracking_history, delivery_attempts. The schema retains the original foreign keys, uniqueness rules, enum domains and indexes. `Database/schema.sql` is the non-destructive setup script for the web app.

### 6. External interfaces
The browser website is served by Spring Boot from `Backend/src/main/resources/static`. The REST API is rooted at `/api`; request/response data uses JSON. Full route list and example payload are in `Backend/API.md`.

### 7. Non-functional requirements
- Validation: Jakarta Bean Validation and domain validation.
- Persistence: MySQL through Spring JDBC; parcel registration and initial tracking event are transactional.
- Configuration: DB URL, username and password supplied through environment variables.
- Maintainability: controller, service, repository and model packages.
- Portability: Java 17 and Maven; Docker image supplied.
- Security limitation: no login/authorization, rate limiting or audit identity; public use with personal data requires security hardening.

### 8. Acceptance criteria
A user can create/list customers and personnel, book a parcel, assign personnel, update status, track events and attempts, and view reports. Database-dependent criteria require a configured MySQL instance. The included automated Spring context test requires Maven dependency resolution and a reachable configured database only if the context triggers datasource connectivity; actual test status is recorded separately.
