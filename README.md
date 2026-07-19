# HRPilot 🚀

### Enterprise HR & Workforce Management Platform

HRPilot is a full-stack **Human Resource Management System (HRMS)** designed to simplify employee operations, attendance tracking, leave management, payroll handling, and role-based workforce administration.

Built using **Java 17, Spring Boot 3.3, React 18, and PostgreSQL**, HRPilot provides a secure, scalable, and modern solution for managing enterprise HR workflows.

---

## ✨ Features

### 🔐 Authentication & Authorization

* JWT-based secure authentication
* BCrypt password encryption
* Role-based access control (RBAC)
* 24 granular permissions
* Three user roles:

  * `HR_ADMIN`
  * `HR_MANAGER`
  * `EMPLOYEE`

---

### 👥 Employee Management

* Complete employee CRUD operations
* Search and pagination support
* Department assignment
* Automatic user account creation
* Generated employee credentials

---

### 📝 Leave Management

* Apply for leave
* Approve/reject leave requests
* Cancel leave requests
* Leave balance tracking
* Leave overlap detection

---

### 🕒 Attendance Management

* Employee clock-in / clock-out
* Daily attendance tracking
* Weekly and monthly attendance views
* Attendance status management

---

### 🏢 Department Management

* Create and manage departments
* Assign employees to departments
* Department-wise employee organization

---

### 💰 Salary Management

* Salary record management
* Salary processing
* Employee salary viewing

---

### 📊 Role-Based Dashboards

Personalized dashboards for different roles:

**HR Admin Dashboard**

* Employee statistics
* Leave overview
* Workforce insights
* System management

**Employee Dashboard**

* Attendance summary
* Leave balance
* Profile information
* Personal activity tracking

---

### 👤 Profile Management

* View employee profile
* Update personal details
* Manage account information

---

# 🛠️ Tech Stack

| Layer               | Technology                     |
| ------------------- | ------------------------------ |
| Backend             | Java 17, Spring Boot 3.3       |
| ORM                 | Spring Data JPA, Hibernate     |
| Database            | PostgreSQL (Supabase)          |
| Security            | Spring Security, JWT           |
| Password Encryption | BCrypt                         |
| Frontend            | React 18                       |
| Build Tool          | Vite 5                         |
| Routing             | React Router 6                 |
| Data Fetching       | TanStack React Query 5         |
| UI Framework        | Material UI 9                  |
| Styling             | Tailwind CSS 3                 |
| Charts              | Recharts                       |
| Forms               | React Hook Form                |
| API Documentation   | SpringDoc OpenAPI / Swagger UI |
| Build System        | Maven                          |

---

# 🏗️ Project Architecture

```
HRPilot/
│
├── backend/
│   └── src/main/java/com/enterprise/peopleflow/
│       │
│       ├── config/          # Security, CORS, Data Seeder
│       ├── controller/      # REST API Controllers
│       ├── dto/             # Data Transfer Objects
│       ├── entity/          # JPA Entities
│       ├── exception/       # Global Exception Handling
│       ├── repository/      # Spring Data JPA Repositories
│       ├── security/        # JWT Filter & Authentication
│       └── service/         # Business Logic
│
└── frontend/
    └── src/
        │
        ├── api/             # Axios API Configuration
        ├── components/      # Reusable Components
        ├── contexts/        # Authentication Context
        ├── hooks/           # Custom Hooks
        └── pages/           # Application Pages
```

---

# 🚀 Getting Started

## Prerequisites

Make sure you have installed:

* Java 17+
* Node.js 18+
* Maven
* PostgreSQL / Supabase Account

---

# ⚙️ Backend Setup

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/hrpilot.git

cd hrpilot
```

---

### 2. Configure Environment Variables

Create `.env` file:

```bash
cp .env.example .env
```

Update database credentials:

```env
DATABASE_URL=your_postgresql_url
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password

JWT_SECRET=your_secret_key
```

---

### 3. Run Backend

Navigate to backend:

```bash
cd backend
```

Run:

```bash
./mvnw spring-boot:run
```

Backend will start:

```
http://localhost:8080
```

---

# 🌐 Frontend Setup

Navigate to frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start development server:

```bash
npm run dev
```

Frontend will start:

```
http://localhost:5173
```

---

# 📚 API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

---

# 🔒 Security Features

* JWT Authentication
* Role-based authorization
* Permission-based API access
* BCrypt password hashing
* Protected REST endpoints
* Secure session handling

---

# 📌 Future Enhancements

* Email notifications
* Advanced payroll reports
* Employee performance tracking
* Document management
* Mobile application support
* Cloud deployment

---

# 👨‍💻 Author
Mohammad Musarrat Nazia
Developed as an enterprise-level HR Workforce Management Platform.

---

⭐ If you find this project useful, consider giving it a star!
