# Courier and Parcel Tracking Management System — Scenario 26

This repository preserves the supplied academic deliverables and original Java console program, and adds a Spring Boot 3 / Java 17 responsive web application backed by MySQL.

## Project layout

```text
Courier_Parcel_Tracking_System/
├── Backend/                 Spring Boot REST API, static HTML/CSS/JS website, tests
├── Database/                Non-destructive MySQL schema and sample data
├── Deployment/              Docker Compose, environment example, hosting guide
├── Documentation/           Deployment guide
├── JP-Source/               Original supplementary Java console program
├── DBMS-Database/           Original database coursework and SQL
├── SE-SRS/                  Software requirements documents
├── SE-Diagrams/             Original diagrams and editable Mermaid sources
├── SE-Testing/              Academic test plan and execution status
├── FDS-Python/              Original data retrieval / preprocessing
├── FDS-Visualization/       Original visualization scripts
├── Dockerfile               Root-context production container build
├── railway.toml / render.yaml
└── README.md
```

## Web application features

- Dashboard counts, recent shipments and status summary
- Customer create/list management
- Parcel booking with unique tracking number and academic tariff
- Parcel listing, delivery personnel assignment and status updates
- Public-style tracking lookup with chronological tracking history and delivery attempts
- Delivery personnel create/list management
- Shipment reports by status, destination city and booking month

The original charge rule is preserved: ₹50 base + ₹10 per kg + zone surcharge (LOCAL ₹0, REGIONAL ₹15, NATIONAL ₹70). The original status, zone and attempt outcome enums are retained.

## Requirements

- Java 17+
- Maven 3.8+
- MySQL 8+
- Optional: Docker Desktop for containerized local run

## Quick local setup

1. Extract the ZIP and open the `Courier_Parcel_Tracking_System` folder in VS Code.
2. In MySQL Workbench, execute `Database/schema.sql`, then `Database/sample_data.sql`. These web deployment scripts do not drop the existing database.
3. Set environment variables and run the backend:

PowerShell:
```powershell
cd Backend
$env:DB_URL='jdbc:mysql://localhost:3306/courier_tracking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USER='courier_app'
$env:DB_PASSWORD='your_local_password'
mvn spring-boot:run
```

macOS/Linux:
```bash
cd Backend
export DB_URL='jdbc:mysql://localhost:3306/courier_tracking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USER='courier_app'
export DB_PASSWORD='your_local_password'
mvn spring-boot:run
```

Open **http://localhost:8080**. The REST API is documented in `Backend/API.md`. Use `Documentation/DEPLOYMENT_GUIDE.md` for complete MySQL user setup, Docker Compose, GitHub, Railway and Render instructions.

## Docker Compose

From `Deployment/`, copy `.env.example` to `.env`, change both passwords, then:
```bash
docker compose up --build
```
Open `http://localhost:8080`. Never commit `.env`.

## Existing academic components

The original Java console program remains under `JP-Source/`. The original `DBMS-Database`, `SE-SRS`, `SE-Diagrams`, `FDS-Python`, `FDS-Visualization`, and `SE-Testing` deliverables are preserved. `DBMS-Database/01_schema.sql` is the original coursework script and includes database reset behavior; for normal web/local deployment use the safer non-destructive `Database/schema.sql`.

The supplied assignment PDF is a common project-instructions sheet and does not include a separate detailed Scenario 26 specification. Existing business rules have therefore been retained from the project SRS and supplied source files.

## Testing and verification status

See `SE-Testing/EXECUTION_REPORT.md`. Do not interpret the presence of test cases as evidence of a live database test or public deployment. The build and integration checks must be executed in an environment with Maven, dependencies and MySQL available.

## Deployment caveat

A public URL is created only after you deploy the service. Hosting platform, managed MySQL, storage, networking, backups and always-on compute may be paid; check current provider pricing and limits. The application currently has no user authentication or role-based access control. Do not expose it publicly with real personal data until access control and security hardening are added.
