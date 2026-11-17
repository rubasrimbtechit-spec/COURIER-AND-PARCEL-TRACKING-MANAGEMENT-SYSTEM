# Supplementary Java console application

The original JDBC console program is retained alongside the Spring Boot web application.

Build: `mvn clean package`

Before running, set all three variables:
- `DB_URL` (for example `jdbc:mysql://localhost:3306/courier_tracking?useSSL=false&serverTimezone=UTC`)
- `DB_USER`
- `DB_PASSWORD`

The console app uses the same `courier_tracking` schema. Initialize it with the original academic SQL in `../DBMS-Database/01_schema.sql` only on a fresh database because that legacy script drops/recreates the database. For existing databases use `../Database/schema.sql`, which is non-destructive.

The console app is a supplementary deliverable; the main web app is in `../Backend`.
