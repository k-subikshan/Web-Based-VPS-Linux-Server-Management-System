# Web-Based Linux Server Management System

## Database setup

The application now starts with an embedded H2 database by default, so a local MySQL password is not required.

### Run immediately
```bash
./mvnw spring-boot:run
```

### Use MySQL instead
Set the MySQL profile and password before starting: 
```bash
export SPRING_PROFILES_ACTIVE=mysql
export DB_USERNAME=root
export DB_PASSWORD='your_mysql_password'
export DB_URL='jdbc:mysql://localhost:3306/mydb2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
./mvnw spring-boot:run
```

The previous `Access denied for user 'root'@'localhost' (using password: NO)` error occurs when the MySQL profile is started without a password. The MySQL profile now requires `DB_PASSWORD` instead of silently attempting an empty password.



A Spring Boot + Thymeleaf web application for managing a Linux VPS through SSH from a browser.

## Features

- SSH connection with configurable host, port and username
- Session-based VPS credentials (not stored in MySQL)
- Dashboard with server navigation
- Linux file manager
- Directory browsing with parent navigation
- File upload and download through SFTP
- Web-based Linux terminal with safety filtering
- CPU, RAM, disk, network, uptime and load monitoring
- systemd service monitoring and controlled start/stop/restart actions
- Nginx, SSH/auth, MySQL, MariaDB, Gunicorn and Spring Boot log viewer
- Website HTTP/HTTPS status and response-time monitor
- HTTPS certificate expiry monitor
- Project deployment tracking
- Audit trail for important management actions
- Input validation and clearer SSH error messages
- Environment-variable based database configuration
- Responsive dark administration interface

## Technology Stack

- Java 21
- Spring Boot 4.0.7
- Spring MVC / Thymeleaf
- Spring Data JPA / Hibernate
- MySQL
- JSch 0.2.21
- SFTP
- HTML5 / CSS3 / JavaScript
- Maven

## Setup

1. Install Java 21 and MySQL.
2. Create a database named `mydb2`.
3. Copy `.env.example` values into your IDE environment variables or deployment environment.
4. Set `DB_PASSWORD` to your local MySQL password.
5. Start the application:

```bash
./mvnw spring-boot:run
```

Open `http://localhost:8080/connecttovps`.

## SSH requirements

The target VPS must have SSH enabled and reachable from the machine running this application. The account used must have the permissions required for the actions you want to perform.

For production, use SSH keys and strict host-key verification instead of password authentication and `StrictHostKeyChecking=no`.

## Important security notes

- Never commit real VPS passwords or database passwords.
- Do not expose port 8080 publicly without authentication and HTTPS.
- Use a dedicated non-root Linux account with least-privilege sudo rules.
- Set `VPS_STRICT_HOST_KEY_CHECKING=yes` after configuring trusted host keys.
- Review the terminal command policy before allowing broader administrative commands.
- Restrict the application behind a VPN, reverse proxy or trusted network for production use.

## Main routes

| Route | Purpose |
|---|---|
| `/connecttovps` | VPS connection form |
| `/dashboard` | Main dashboard |
| `/files` | File manager |
| `/run_command` | Web terminal |
| `/server-monitor` | Server health |
| `/service-monitor` | systemd service management |
| `/log-analyzer` | Remote logs |
| `/website-monitor` | Website and TLS monitoring |
| `/projects` | Project tracking |
| `/audit` | Audit history |
| `/api/monitor` | JSON monitoring endpoint |

## Known limitations

This version intentionally does not implement arbitrary destructive shell commands, password changes, recursive deletion, remote package installation or unrestricted `sudo` from the web terminal. Those operations should be exposed only through explicit, authenticated, audited workflows.

## Database startup fix (Spring Boot 4 / Hibernate 7)

The project explicitly configures the MySQL Hibernate dialect because Hibernate 7 can fail to infer the dialect when JDBC metadata is unavailable during startup. The default database is `mydb2` on localhost. Set `DB_USERNAME` and `DB_PASSWORD` to match your local MySQL installation.

Example environment variables:

```text
DB_URL=jdbc:mysql://localhost:3306/mydb2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JPA_DDL_AUTO=update
```

If MySQL is not running, start it before launching the application. The dialect setting fixes the Hibernate dialect error, but it cannot replace an unavailable database or incorrect MySQL credentials.

## ML Assistant (new)

Open **Dashboard → ML Assistant** after connecting to a VPS. The assistant reads live metrics through the existing SSH monitor and provides an explainable risk estimate plus findings and recommendations.

### What it does
- Collects CPU, memory, disk and load-average samples for the connected host.
- Combines warning/critical thresholds with changes from the previous sample and statistical outlier checks against the recent in-memory baseline.
- Displays a LOW / MEDIUM / HIGH risk category and a 0–100 heuristic risk score.
- Answers basic natural-language questions such as “Diagnose my server”, “Predict failure risk”, and “Show CPU and RAM usage”.
- Remains read-only: it does not execute shell commands, restart services, or delete files.

### ML model notes
This is a lightweight online statistical anomaly detector, not a pretrained deep-learning model. It starts with a small baseline and gets more useful after multiple analysis requests. Its bounded history is kept in application memory and is reset when the app restarts. The risk score is an operational estimate, not a guaranteed prediction of server failure. For a production predictive model, persist timestamped metrics to a database and train/validate a time-series classifier on labelled incidents.

### Endpoints
- `GET /api/ml/analysis` — collect live metrics and return anomaly/risk analysis for the current session's VPS.
- `POST /api/ml/assistant` — request body: `{"message":"Diagnose my server"}`; returns a text answer and structured analysis.

Both endpoints use the VPS connection held in the current web session. Keep the application behind authentication and HTTPS. Do not expose these endpoints publicly without adding authentication, authorization, rate limits and audit logging.
