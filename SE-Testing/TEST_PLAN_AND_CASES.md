# Software Testing Plan and Test Cases

## Techniques
- Unit: charge formula, input validation, status validation.
- Integration: Java JDBC operations and transaction that saves parcel plus initial history.
- System: end-to-end customer, parcel, assignment, status, tracking and reporting workflow.
- Validation: blank fields, invalid zone/status, non-positive weight and unknown identifiers.
- Database: primary/foreign keys, UNIQUE/CHECK/ENUM constraints and referential integrity.
- Python pipeline: retrieval, type conversion, duplicate removal, missing-value treatment and CSV creation.
- Visualization: chart generation from processed CSV and empty-data handling.

## Test cases
Actual-result and status fields remain blank until executed in the target environment.

| ID | Input / action | Expected result | Actual result | Status |
|---|---|---|---|---|
| TC-01 | Add customer with unique phone | Insert succeeds and ID is shown | | Not run |
| TC-02 | Reuse customer phone | UNIQUE constraint rejects duplicate | | Not run |
| TC-03 | Register valid LOCAL parcel | Unique tracking ID and correct charge | | Not run |
| TC-04 | Invalid destination zone | Validation error; no parcel inserted | | Not run |
| TC-05 | Zero/negative weight | Input rejected | | Not run |
| TC-06 | Assign valid personnel | Parcel assignment saved | | Not run |
| TC-07 | Assign unknown personnel ID | Foreign-key error | | Not run |
| TC-08 | Update status | Parcel and history both updated | | Not run |
| TC-09 | Unsupported status | Validation error; no update | | Not run |
| TC-10 | Track existing parcel | Current status and chronological history shown | | Not run |
| TC-11 | Unknown tracking number | Not-found message | | Not run |
| TC-12 | Record delivery attempt | Next attempt number inserted | | Not run |
| TC-13 | View report | Counts grouped by status | | Not run |
| TC-14 | Run Python retrieval | CSV created from MySQL records | | Not run |
| TC-15 | Run preprocessing with duplicate/missing data | Duplicate and missing-value rules applied | | Not run |
| TC-16 | Run visualization | PNG charts saved from processed CSV | | Not run |
| TC-17 | Run visualization without CSV | Clear prerequisite message | | Not run |
| TC-18 | Start Java with MySQL stopped | Connection failure message; app exits | | Not run |

## Execution record
Environment: __________  JDK: __________  Maven: __________  MySQL: __________
Python: __________  Date: __________  Tester: __________

Do not mark tests passed until actually executed and outputs inspected.

## Web application test additions

| ID | Input / action | Expected result | Actual result | Status |
|---|---|---|---|---|
| WEB-01 | Open `/` with configured MySQL | Responsive dashboard loads | | Not run |
| WEB-02 | POST valid customer JSON | Customer is created and returned | | Not run |
| WEB-03 | POST duplicate customer phone | HTTP 409 constraint response | | Not run |
| WEB-04 | POST valid parcel JSON | Unique tracking number, correct tariff and initial history event | | Not run |
| WEB-05 | PUT valid status update | Parcel status changes and history event is appended transactionally | | Not run |
| WEB-06 | GET tracking number | Parcel, chronological history and attempts returned | | Not run |
| WEB-07 | GET reports | Status, city and monthly report arrays returned | | Not run |
| WEB-08 | Invalid request fields | HTTP 400 validation response | | Not run |

The pure tariff calculator has JUnit tests for the three tariff zones, invalid zone and non-positive weight. These tests are present but were not executed in the packaging environment because Maven is not installed.
