# BranchLess – Online Banking System

A lightweight, beginner-friendly Online Banking System built with **Java 21**, **Spring Boot 3.3.4**, **MySQL**, and a responsive **HTML/CSS/JavaScript** frontend. Designed for easy understanding, clean demonstration, and college viva presentations.

---

## 📌 Features

### 1. User & Authentication
- **Customer Registration**: New customers can register with name, email, phone, and BCrypt-hashed password.
- **Simple Login**: Secure password authentication using BCrypt (no heavyweight JWT or session tokens required).
- **Profile View & Update**: Retrieve profile details and update name/phone.

### 2. Bank Accounts
- **Auto Account Number Generation**: 10-digit unique bank account number.
- **Account Types**: Savings, Checking, and Current accounts.
- **Multiple Accounts Support**: Customers can own multiple bank accounts with a live account switcher.
- **Account Status**: `ACTIVE` or `BLOCKED`.

### 3. Money Operations
- **Deposit**: Add money to an active account with real-time balance updates.
- **Withdrawal**: Safely withdraw money with balance sufficiency checks.
- **Transfer**: Transfer funds between two active accounts within an `@Transactional` boundary.
- **Validation**: Enforces positive amounts, prevents transfers to the same account, and blocks operations on frozen accounts.

### 4. Transaction History
- Complete chronological audit log of all deposits, withdrawals, and transfers associated with an account.

### 5. Administration
- View all customers and accounts.
- Freeze/Block and Reactivate accounts instantly.

### 6. Interactive Frontend Dashboard
- Served directly by Spring Boot at `http://localhost:8080/`.
- Zero external dependencies: pure HTML, CSS, and Vanilla JavaScript (works 100% offline).
- Real-time notifications and clean user-friendly error banners.

---

## 🏛 Architecture

```
Browser Client (http://localhost:8080/)
      │
      ▼
Controller (Spring Web REST APIs + Static Resource Handler)
      │
      ▼
Service (Business Logic, Validation, BCrypt Hashing, @Transactional)
      │
      ▼
Repository (Spring Data JPA)
      │
      ▼
Database (MySQL 8 / 9)
```

### Project Structure
```
Brancheless_Online_banking/
├── .mvn/wrapper/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── branchless/
    │   │           ├── BranchlessApplication.java
    │   │           ├── aspect/
    │   │           │   └── LoggingAspect.java
    │   │           ├── controller/
    │   │           │   ├── AccountController.java
    │   │           │   ├── AdminController.java
    │   │           │   ├── CustomerController.java
    │   │           │   └── TransactionController.java
    │   │           ├── dto/
    │   │           │   ├── LoginRequest.java
    │   │           │   ├── MoneyRequest.java
    │   │           │   └── TransferRequest.java
    │   │           ├── entity/
    │   │           │   ├── Account.java
    │   │           │   ├── Customer.java
    │   │           │   └── Transaction.java
    │   │           ├── exception/
    │   │           │   ├── AccountBlockedException.java
    │   │           │   ├── AccountNotFoundException.java
    │   │           │   ├── CustomerNotFoundException.java
    │   │           │   ├── EmailAlreadyExistsException.java
    │   │           │   ├── GlobalExceptionHandler.java
    │   │           │   ├── InsufficientBalanceException.java
    │   │           │   ├── InvalidCredentialsException.java
    │   │           │   └── InvalidTransactionException.java
    │   │           ├── repository/
    │   │           │   ├── AccountRepository.java
    │   │           │   ├── CustomerRepository.java
    │   │           │   └── TransactionRepository.java
    │   │           └── service/
    │   │               ├── AccountService.java
    │   │               ├── CustomerService.java
    │   │               └── TransactionService.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── index.html
    │           ├── style.css
    │           └── script.js
    └── test/
        └── java/
            └── com/
                └── branchless/
                    ├── AccountControllerTest.java
                    ├── AccountServiceTest.java
                    ├── AdminControllerTest.java
                    ├── CustomerControllerTest.java
                    ├── CustomerServiceTest.java
                    ├── FrontendStaticResourcesTest.java
                    ├── TransactionControllerTest.java
                    └── TransactionServiceTest.java
```

---

## 🛠 Technology Stack

- **Java**: 21 (Temurin / Adoptium)
- **Framework**: Spring Boot 3.3.4
- **Database**: MySQL (`branchless_db`)
- **ORM & Data**: Spring Data JPA / Hibernate
- **Security / Hashing**: `spring-security-crypto` (BCryptPasswordEncoder)
- **Logging & Monitoring**: Spring AOP (`LoggingAspect` records method names and execution times)
- **Frontend**: HTML5, CSS3, Vanilla JavaScript (No React/Angular/Node dependencies)

---

## ⚙️ MySQL Configuration & Environment Variable

`src/main/resources/application.properties` uses:
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/branchless_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

### Setting `DB_PASSWORD` on Windows:

In **PowerShell**:
```powershell
$env:DB_PASSWORD="your_mysql_password"
```

In **Command Prompt (CMD)**:
```cmd
set DB_PASSWORD=your_mysql_password
```

*(If your local MySQL root user has no password, leave it unset).*

---

## 🚀 How to Run the Application

1. Open PowerShell in the project directory:
   ```powershell
   cd C:\Users\naren\OneDrive\Desktop\Brancheless_Online_banking
   ```

2. Set your MySQL root password:
   ```powershell
   $env:DB_PASSWORD="your_mysql_password"
   ```

3. Run the Spring Boot application using the included Maven wrapper:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Open your web browser and navigate to:
   ```
   http://localhost:8080/
   ```

---

## 📡 REST API Reference

### 👤 Customer Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/customers/register` | Register a new customer |
| `POST` | `/customers/login` | Login with email and password |
| `GET` | `/customers/{id}` | Get customer profile details |
| `PUT` | `/customers/{id}` | Update customer name or phone |

### 💳 Account Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/accounts` | Create an account (`customerId`, `accountType`) |
| `GET` | `/accounts/customer/{customerId}` | Retrieve all accounts belonging to a customer |
| `GET` | `/accounts/{accountNumber}` | Get account details and balance |
| `POST` | `/accounts/{accountNumber}/deposit` | Deposit funds (`{"amount": 500}`) |
| `POST` | `/accounts/{accountNumber}/withdraw` | Withdraw funds (`{"amount": 200}`) |
| `POST` | `/accounts/{accountNumber}/transfer` | Transfer funds (`{"destinationAccount": "...", "amount": 100}`) |

### 📊 Transaction Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/transactions/account/{accountNumber}` | View account transaction history |

### 🛡 Admin Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/admin/customers` | List all registered customers |
| `GET` | `/admin/accounts` | List all bank accounts |
| `PUT` | `/admin/accounts/{accountNumber}/block` | Freeze/Block an account |
| `PUT` | `/admin/accounts/{accountNumber}/activate` | Reactivate an account |

---

## 🧪 Testing

To run the full suite of unit, slice, and static frontend resource tests (45 automated tests):
```powershell
.\mvnw.cmd test
```
To package the standalone executable JAR:
```powershell
.\mvnw.cmd clean package
```
The JAR will be located at `target/branchless-banking-1.0.0.jar`.
