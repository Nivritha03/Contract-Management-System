# Contract Management System — Backend API

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot 3.2.5](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36.svg)](https://maven.apache.org/)
[![MySQL 8.0](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

A robust, production-grade Spring Boot backend REST API for managing the lifecycle of contracts. It provides automated text extraction for PDF and Microsoft Word documents, MySQL persistence, a validated status state-machine, and an in-memory keyword-matching Q&A search engine.

---

## 1. Features

- **Multi-Format Document Parsing**: Automated plain text extraction from **PDF** (Apache PDFBox 3.x), **DOCX** (Apache POI 5.x OOXML), and legacy **DOC** (Apache POI Scratchpad).
- **Safe Local Document Storage**: Uploaded files are stored in an isolated `uploads/` directory with unique UUID prefixes to prevent filename collisions and directory traversal attacks.
- **Relational Persistence**: Full metadata, timestamps, and extracted text stored in MySQL 8 using Spring Data JPA and Hibernate.
- **Finite-State Machine Workflow**: Strictly enforced status transitions (`DRAFT` ➔ `REVIEW` ➔ `APPROVED`), rejecting illegal state jumps with descriptive `400 Bad Request` messages.
- **Keyword-Based Question Answering**: Fast, in-memory NLP pipeline featuring sentence boundary detection, stop-word removal, and keyword scoring without third-party paid AI APIs.
- **Transactional Rollback Cleanup**: If database persistence fails after file upload, temporary stored files on disk are automatically cleaned up.
- **Centralized Exception Handling**: Uniform timestamped JSON error responses for validation, size limits, and missing resources.

---

## 2. Technology Stack

| Component | Technology | Version |
|---|---|---|
| **Language** | Java (OpenJDK / Eclipse Adoptium) | 17 |
| **Framework** | Spring Boot | 3.2.5 |
| **Data Access** | Spring Data JPA / Hibernate | 6.4.4 |
| **Database** | MySQL Server | 8.0 |
| **Document Parser** | Apache PDFBox | 3.0.2 |
| **Office Parser** | Apache POI (`poi-ooxml`, `poi-scratchpad`) | 5.2.5 |
| **Validation** | Jakarta Bean Validation | Spring Boot Starter |
| **Build Tool** | Apache Maven | 3.9+ |
| **Testing** | JUnit 5, Spring Boot Test | 3.2.5 |

---

## 3. Project Architecture

```
backend/contract-management/
├── pom.xml                                  # Maven project configuration & dependencies
├── README.md                                # Project overview & API documentation
├── SUBMISSION_DOCUMENTATION.md              # Detailed academic submission & viva guide
├── uploads/                                 # Secure local storage for uploaded contracts
├── sample-files/                            # Pre-generated sample contracts (.pdf, .docx)
└── src/
    ├── main/
    │   ├── java/com/example/contract/
    │   │   ├── ContractManagementApplication.java
    │   │   ├── controller/
    │   │   │   └── ContractController.java       # REST endpoints & HTTP mappings
    │   │   ├── service/
    │   │   │   ├── ContractService.java          # Core business logic & storage coordinator
    │   │   │   ├── DocumentParserService.java    # PDFBox & POI document extraction
    │   │   │   └── QuestionAnswerService.java    # Sentence splitting & keyword scoring
    │   │   ├── repository/
    │   │   │   └── ContractRepository.java       # Spring Data JPA database operations
    │   │   ├── entity/
    │   │   │   └── Contract.java                 # JPA Entity mapped to 'contracts' table
    │   │   ├── enums/
    │   │   │   └── ContractStatus.java           # DRAFT, REVIEW, APPROVED enum
    │   │   ├── dto/
    │   │   │   ├── ContractResponse.java         # API response payload
    │   │   │   ├── StatusUpdateRequest.java      # Status transition request
    │   │   │   ├── QuestionRequest.java          # Q&A question request
    │   │   │   └── AnswerResponse.java           # Q&A answer response
    │   │   └── exception/
    │   │       ├── ResourceNotFoundException.java
    │   │       └── GlobalExceptionHandler.java   # Centralized error handler (@RestControllerAdvice)
    │   └── resources/
    │       └── application.properties            # Application & MySQL configuration
    └── test/
        ├── java/com/example/contract/
        │   ├── ContractManagementApplicationTests.java
        │   └── SampleFilesGeneratorTest.java     # Automated test document generator
        └── resources/
```

---

## 4. Database Setup (MySQL)

1. Open your terminal or Command Prompt and connect to MySQL:
   ```bash
   mysql -u root -p
   ```
2. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS contract_db;
   ```
3. Hibernate will automatically generate and maintain the `contracts` schema upon application startup:
   ```sql
   USE contract_db;
   SHOW TABLES;
   DESCRIBE contracts;
   ```

---

## 5. Configuration (`application.properties`)

Located at `src/main/resources/application.properties`:

```properties
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/contract_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
# Reads from environment variable MYSQL_PASSWORD or set your local password
spring.datasource.password=${MYSQL_PASSWORD:your_password_here}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Multipart File Upload Configuration (10 MB max)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Local Directory for Document Storage
app.upload.dir=uploads
```

---

## 6. Build and Run

### Compile and Run Tests
```bash
mvn clean test
```

### Start the Application
```bash
mvn spring-boot:run
```
The server will start on port `8080`: `http://localhost:8080`

---

## 7. REST API Reference

### 1. Upload Contract
- **Method**: `POST`
- **Endpoint**: `/api/contracts`
- **Content-Type**: `multipart/form-data`
- **Request Body**:
  - `file`: Form-data file binary (`.pdf`, `.docx`, or `.doc`, max 10 MB)
- **Response**: `201 Created`
```json
{
  "id": 1,
  "filename": "sample-agreement.pdf",
  "storedFilename": "108fca66-d42b-409b-ade4-446a882fcc89_sample-agreement.pdf",
  "fileType": "PDF",
  "fileSize": 993,
  "extractedText": "SERVICES AGREEMENT\r\nThis Services Agreement is entered into between Acme Corp and Beta LLC.\r\nThe payment shall be completed within 30 days of receiving the invoice.\r\nThe agreement shall commence on January 1, 2026 and continue for one year.\r\nEither party may terminate this agreement upon 60 days written notice.",
  "status": "DRAFT",
  "uploadedAt": "2026-11-08T20:44:36.016633",
  "updatedAt": "2026-11-08T20:44:36.016633"
}
```

### 2. Retrieve All Contracts
- **Method**: `GET`
- **Endpoint**: `/api/contracts`
- **Response**: `200 OK`
```json
[
  {
    "id": 1,
    "filename": "sample-agreement.pdf",
    "storedFilename": "108fca66-d42b-409b-ade4-446a882fcc89_sample-agreement.pdf",
    "fileType": "PDF",
    "fileSize": 993,
    "status": "DRAFT",
    "uploadedAt": "2026-11-08T20:44:36.016633",
    "updatedAt": "2026-11-08T20:44:36.016633"
  }
]
```

### 3. Retrieve Single Contract by ID
- **Method**: `GET`
- **Endpoint**: `/api/contracts/{id}`
- **Response**: `200 OK`

### 4. Update Contract Status
- **Method**: `PATCH`
- **Endpoint**: `/api/contracts/{id}/status`
- **Content-Type**: `application/json`
- **Workflow State Machine**:
  - `DRAFT` ➔ `REVIEW`
  - `REVIEW` ➔ `APPROVED`
  - *All other transitions are rejected with HTTP 400.*
- **Request Body**:
```json
{
  "status": "REVIEW"
}
```
- **Response**: `200 OK`

### 5. Contract Keyword Q&A
- **Method**: `POST`
- **Endpoint**: `/api/contracts/{id}/ask`
- **Content-Type**: `application/json`
- **Request Body**:
```json
{
  "question": "What is the payment period?"
}
```
- **Response**: `200 OK`
```json
{
  "contractId": 1,
  "question": "What is the payment period?",
  "answer": "The payment shall be completed within 30 days of receiving the invoice."
}
```
*(If no matching clause is found, returns: `"Information not found in the contract."`)*

---

## 8. Postman Testing Guide

1. **Upload Contract (`POST /api/contracts`)**:
   - In Postman, set method to `POST`, URL to `http://localhost:8080/api/contracts`.
   - In the **Body** tab, select **form-data**.
   - Set Key as `file`, switch dropdown from *Text* to *File*.
   - Choose a `.pdf`, `.docx`, or `.doc` from `sample-files/` and click **Send**.
2. **Fetch Contract (`GET /api/contracts/1`)**:
   - Set method to `GET`, URL to `http://localhost:8080/api/contracts/1` and click **Send**.
3. **Ask Question (`POST /api/contracts/1/ask`)**:
   - Set method to `POST`, URL to `http://localhost:8080/api/contracts/1/ask`.
   - Body: `raw` -> `JSON`: `{"question": "What is the payment period?"}`. Click **Send**.
4. **Transition Status (`PATCH /api/contracts/1/status`)**:
   - Send `{"status": "REVIEW"}` ➔ **200 OK**.
   - Send `{"status": "APPROVED"}` ➔ **200 OK**.
   - Send `{"status": "DRAFT"}` on the approved contract ➔ **400 Bad Request** (illegal transition).

---

## 9. Error Handling Specifications

The application uses centralized error handling (`@RestControllerAdvice`) providing structured error messages:

| Error Type | HTTP Status | Response Example |
|---|---|---|
| Non-existent ID | `404 Not Found` | `{"status": 404, "error": "Not Found", "message": "Contract not found with id: 999"}` |
| Invalid Status Transition | `400 Bad Request` | `{"status": 400, "error": "Bad Request", "message": "Invalid status transition from 'APPROVED' to 'DRAFT'..."}` |
| Unsupported File Type | `400 Bad Request` | `{"status": 400, "error": "Bad Request", "message": "Unsupported file type: .exe. Allowed types: PDF, DOC, DOCX."}` |
| File Size Exceeded | `400 Bad Request` | `{"status": 400, "error": "File Size Limit Exceeded", "message": "File size exceeds maximum upload limit of 10 MB."}` |
| Blank Question | `400 Bad Request` | `{"status": 400, "error": "Validation Failed", "errors": {"question": "Question cannot be blank"}}` |
