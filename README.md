HRpilot
A full-stack Enterprise HR & Workforce Management Platform built with Java 17, Spring Boot 3.3, and React 18.

Features
Authentication & Authorization — JWT-based login with 24 granular permissions across 3 roles: HR_ADMIN, HR_MANAGER, EMPLOYEE
Employee Management — Full CRUD with search, pagination, auto-created user accounts with generated credentials
Leave Management — Apply, approve, reject, cancel leaves with overlap detection and balance tracking
Attendance Tracking — Clock in/out, daily/weekly/monthly views, status tracking
Department Management — CRUD with employee assignment
Salary Management — View/process/manage salary records
Role-Based Dashboards — Personalized admin and employee dashboards with real-time stats and charts
Profile Management — User profile with employee details
Tech Stack
Layer	Technology
Backend	Java 17, Spring Boot 3.3, Spring Data JPA, Spring Security
Database	PostgreSQL (Supabase)
Auth	JWT (jjwt 0.12.6), BCrypt (strength 12)
Frontend	React 18, Vite 5, React Router 6, TanStack React Query 5
UI	Material UI 9, Tailwind CSS 3, Recharts
Forms	React Hook Form 7
API Docs	SpringDoc OpenAPI / Swagger UI
Getting Started
Prerequisites
Java 17+
Node.js 18+
PostgreSQL (or Supabase account)
Backend
# Create .env with your database credentials
cp .env.example .env

# Run
./mvnw spring-boot:run
Server starts on http://localhost:8080

Frontend
cd frontend
npm install
npm run dev
Dev server starts on http://localhost:5173 (proxies /api → :8080)
EMS/
├── src/main/java/com/enterprise/peopleflow/
│   ├── config/          # Security, CORS, data seeding
│   ├── controller/      # REST endpoints (26 total)
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA entities (12 total)
│   ├── exception/       # Global exception handling
│   ├── repository/      # Spring Data JPA repos
│   ├── security/        # JWT filter, CustomUserDetails
│   └── service/         # Business logic
├── frontend/src/
│   ├── api/             # Axios API layer
│   ├── components/      # Reusable UI components
│   ├── contexts/        # Auth context
│   ├── hooks/           # useRole, useCurrentUserEmployee
│   └── pages/           # Route pages (Dashboard, Employees, Leaves, etc.)
└── .env                 # Database credentials
