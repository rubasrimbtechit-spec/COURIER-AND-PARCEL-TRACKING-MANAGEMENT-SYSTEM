# Build and verification report

Execution date: 2026-11-17 (packaging environment)

| Check | Result | Notes |
|---|---|---|
| Uploaded ZIP inspection | PASS | 30 original files inspected; original academic directories retained |
| Java runtime availability | PASS | OpenJDK 21 present; project targets Java 17 |
| Maven availability | NOT RUN | `mvn` executable is not installed in packaging environment |
| Maven compile/test | NOT RUN | Could not execute without Maven |
| Live MySQL integration | NOT RUN | No configured MySQL service/credentials in packaging environment |
| Browser end-to-end | NOT RUN | No running web application/browser test environment |
| Public deployment | NOT PERFORMED | Requires user's hosting and database accounts |
| Python source syntax | PASS (prior package report) | Existing validation report documents syntax checks for original Python scripts |

Do not report Maven compilation, REST integration, browser workflows or deployment as passed. On a machine with Java 17+, Maven 3.8+ and MySQL 8+, run `mvn test` and `mvn clean package` from `Backend/`, then execute the manual cases in `TEST_PLAN_AND_CASES.md` against a test database.
