# Jasper Report Demo

This is an **educational project** demonstrating how to generate reports using **JasperReports** in a Spring Boot application.  
The application takes a template (`.jrxml`) and JSON data, then generates a **PDF report**.  
Logs are saved both in the `output.txt` file and inside the Docker container.

---

## 🚀 Features
- Generate PDF reports from Jasper templates
- Support for custom JSON data
- REST API endpoint for report generation
- Logging of report generation process
- Docker support

---

## 📂 Project Structure
JasperDemo/
├── src/main/java/... # Spring Boot source code
├── src/main/resources/
│ ├── templates/ # JasperReports templates (.jrxml)
│ ├── static/ # Static resources
│ └── application.properties
├── Dockerfile # Docker configuration
├── output.txt # Log file
└── pom.xml # Maven dependencies


---

## 🛠️ Technologies
- **Java 17**
- **Spring Boot**
- **JasperReports**
- **Maven**
- **Docker**

---

## ⚡ Usage

### 1. Build the project
```bash
mvn clean package

2. Run with Maven
mvn spring-boot:run

3. Generate a report

Send a POST request:

POST http://localhost:8080/api/reports/generate


With query params:

template=test_report.jrxml
data=test_data.json
output=my_report.pdf

🐳 Run with Docker
docker build -t jasper-report-demo .
docker run -p 8080:8080 jasper-report-demo

📘 Example Templates

Place your .jrxml templates inside:

src/main/resources/templates/
