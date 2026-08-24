# 📖 Student-Corporate Matcher Platform - Complete API Reference Guide

> **Live Production Base URL**: `https://student-corporate-matcher.onrender.com`  
> **Local Development Base URL**: `http://localhost:8080`  
> **API Version**: `v1` (`/api/v1`)  
> **Live Interactive Swagger UI**: [`https://student-corporate-matcher.onrender.com/swagger-ui/index.html`](https://student-corporate-matcher.onrender.com/swagger-ui/index.html)  
> **Live OpenAPI JSON Spec**: [`https://student-corporate-matcher.onrender.com/v3/api-docs`](https://student-corporate-matcher.onrender.com/v3/api-docs)  
> **Live Keep-Alive Micro-Ping**: [`https://student-corporate-matcher.onrender.com/api/v1/ping`](https://student-corporate-matcher.onrender.com/api/v1/ping) or [`/ping`](https://student-corporate-matcher.onrender.com/ping)

---

## Table of Contents
1. [General API Conventions & Envelope Format](#1-general-api-conventions--envelope-format)
2. [Public Keep-Alive Ping API](#2-public-keep-alive-ping-api)
3. [Authentication API (`/api/v1/auth`)](#3-authentication-api-apiv1auth)
4. [Teacher & Faculty Management API (`/api/v1/teachers`)](#4-teacher--faculty-management-api-apiv1teachers)
5. [Student Management API (`/api/v1/students`)](#5-student-management-api-apiv1students)
6. [Company Management API (`/api/v1/companies`)](#6-company-management-api-apiv1companies)
7. [Matchmaking Engine API (`/api/v1/matches`)](#7-matchmaking-engine-api-apiv1matches)
8. [Master Admin Operations & Diagnostics API (`/api/v1/admin`)](#8-master-admin-operations--diagnostics-api-apiv1admin)
9. [HTTP Status Codes & Error Handling](#9-http-status-codes--error-handling)

---

## 1. General API Conventions & Envelope Format

### Standard Success Response Envelope
All HTTP `200 OK` and `201 Created` responses wrap output data in a consistent JSON envelope:
```json
{
  "status": 200,
  "message": "Human-readable description of the operation",
  "data": { ... },
  "timestamp": "2026-08-23T04:00:00Z"
}
```

### Standard Error Response Envelope
All client and server errors return structured details:
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Detailed explanation of why the request failed",
  "timestamp": "2026-08-23T04:00:00Z",
  "path": "/api/v1/..."
}
```

### Authorization Header
All protected endpoints require a valid JWT Bearer token:
```http
Authorization: Bearer <your_jwt_access_token>
```

---

## 2. Public Keep-Alive Ping API

### `GET /api/v1/ping` or `GET /ping`
- **Purpose**: Zero-auth, zero-database, ultra-lightweight ping service designed to keep free-tier cloud instances (like Render) awake and avoid cold-start latency.
- **Request**:
```bash
curl -X GET https://student-corporate-matcher.onrender.com/api/v1/ping
```
- **Response `200 OK`**:
```json
{
  "status": "UP",
  "pong": true,
  "timestamp": "2026-08-23T04:00:00.123Z",
  "uptimeSeconds": 1420,
  "jvmUptimeMs": 1420456
}
```

---

## 3. Authentication API (`/api/v1/auth`)

### 3.1. Request Email OTP (Students & Companies)
- **Method**: `POST`
- **Path**: `/api/v1/auth/otp/send`
- **Rate Limit**: 60s cooldown per email. Max 3 requests/hour.
- **Request Body**:
```json
{
  "email": "student@university.edu",
  "role": "ROLE_STUDENT"
}
```
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "OTP generated successfully",
  "data": "OTP has been dispatched to student@university.edu. Code valid for 5 minutes.",
  "timestamp": "2026-08-23T04:00:00Z"
}
```
- **Security Rules**:
  - `ROLE_ADMIN` will be rejected with `403 Forbidden` (*Use `/admin/otp/send`*).
  - `ROLE_TEACHER` with unapproved profile will return `403 Forbidden`: `"No further action, verification is in waiting"`.

---

### 3.2. Request Admin 2-Step OTP (Step 1: Password Verification)
- **Method**: `POST`
- **Path**: `/api/v1/auth/admin/otp/send`
- **Request Body**:
```json
{
  "email": "amansingh.mothari85@gmail.com",
  "password": "Admin@RootMaster2026!",
  "sendToRecoveryEmail": true
}
```
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Admin OTP generated successfully",
  "data": "Master Admin authentication step 1 successful. OTP has been dispatched to admin email and recovery email.",
  "timestamp": "2026-08-23T04:00:00Z"
}
```

---

### 3.3. Verify OTP & Log In (Step 2)
- **Method**: `POST`
- **Path**: `/api/v1/auth/otp/verify`
- **Request Body**:
```json
{
  "email": "amansingh.mothari85@gmail.com",
  "otp": "834912"
}
```
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "7c8e9f2a-1b3c-4d5e-6f7a-8b9c0d1e2f3a",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "email": "amansingh.mothari85@gmail.com",
    "role": "ROLE_ADMIN"
  },
  "timestamp": "2026-08-23T04:00:00Z"
}
```

---

### 3.4. Refresh Access Token (28-Day Rotation)
- **Method**: `POST`
- **Path**: `/api/v1/auth/refresh` or `/api/v1/auth/token/refresh`
- **Request Body**:
```json
{
  "refreshToken": "7c8e9f2a-1b3c-4d5e-6f7a-8b9c0d1e2f3a"
}
```
- **Response `200 OK`**: Returns rotated fresh `accessToken` and single-use `refreshToken`.

---

### 3.5. Logout & Invalidate Session
- **Method**: `POST`
- **Path**: `/api/v1/auth/logout`
- **Auth**: Bearer Token required
- **Response `200 OK`**: Revokes all refresh tokens.

---

## 4. Teacher & Faculty Management API (`/api/v1/teachers`)

### 4.1. Teacher Self-Registration
- **Method**: `POST`
- **Path**: `/api/v1/teachers/register`
- **Auth**: Public
- **Request Body**:
```json
{
  "fullName": "Dr. Alan Turing",
  "email": "turing@faculty.edu",
  "employeeId": "EMP-FAC-1002",
  "department": "Computer Science & Engineering",
  "designation": "Professor",
  "phoneNumber": "+1987654321",
  "assignedSubjects": [
    "Theory of Computation",
    "Algorithms"
  ]
}
```
- **Response `201 Created`**:
```json
{
  "status": 200,
  "message": "Faculty registration submitted successfully. Your account is pending administrator verification before login.",
  "data": {
    "id": 1,
    "email": "turing@faculty.edu",
    "fullName": "Dr. Alan Turing",
    "employeeId": "EMP-FAC-1002",
    "department": "Computer Science & Engineering",
    "designation": "Professor",
    "approvalStatus": "PENDING",
    "assignedSubjects": [
      "Theory of Computation",
      "Algorithms"
    ]
  },
  "timestamp": "2026-08-23T04:00:00Z"
}
```

---

### 4.2. Get / Update Faculty Profile
- `GET /api/v1/teachers/profile` (Requires `ROLE_TEACHER` with `APPROVED` status)
- `PUT /api/v1/teachers/profile` (Requires `ROLE_TEACHER` with `APPROVED` status)

---

### 4.3. Fetch Student Marks by Roll Number
- **Method**: `GET`
- **Path**: `/api/v1/teachers/students/{rollNumber}/marks`
- **Auth**: `ROLE_TEACHER` (Approved)
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Student marks retrieved successfully",
  "data": [
    {
      "id": 5,
      "subjectName": "Theory of Computation",
      "selfReportedMarks": 85.0,
      "verifiedMarks": null,
      "isVerified": false,
      "semester": "Semester 4",
      "verificationRemark": "Verification by Teacher is Required"
    }
  ]
}
```

---

### 4.4. Verify Student Marks
- **Method**: `POST`
- **Path**: `/api/v1/teachers/students/{rollNumber}/marks/verify`
- **Auth**: `ROLE_TEACHER` (Approved)
- **Request Body**:
```json
{
  "verifiedMarks": [
    {
      "subjectName": "Theory of Computation",
      "verifiedMarks": 89.0,
      "semester": "Semester 4",
      "remarks": "Verified against final semester exam script"
    }
  ]
}
```

---

## 5. Student Management API (`/api/v1/students`)

- `GET /api/v1/students/profile`: Get current student profile, calculated aggregate percentage, skills, and academic records.
- `PUT /api/v1/students/profile`: Update contact info, roll number, bio, GitHub, and LinkedIn URLs.
- `POST /api/v1/students/marks`: Self-report semester subject marks.
- `POST /api/v1/students/skills`: Register technical skills with proficiency levels (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`) and years of experience.
- `DELETE /api/v1/students/skills/{skillName}`: Remove a registered skill.

---

## 6. Company Management API (`/api/v1/companies`)

- `POST /api/v1/companies/register`: Public company self-registration (defaults to `NOT_VERIFIED` status).
- `GET /api/v1/companies/public`: Public directory of approved, verified hiring companies.
- `GET /api/v1/companies/profile`: Authenticated company profile.
- `PUT /api/v1/companies/profile`: Update company information.
- `POST /api/v1/companies/criteria`: Define hiring job role, minimum aggregate score, required skills, and subject cutoffs.
- `PUT /api/v1/companies/criteria/{id}`: Update hiring criteria.
- `DELETE /api/v1/companies/criteria/{id}`: Remove hiring criteria.

---

## 7. Matchmaking Engine API (`/api/v1/matches`)

### 7.1. Match Students Against Company Criteria
- **Method**: `GET`
- **Path**: `/api/v1/matches/criteria/{criteriaId}`
- **Auth**: `ROLE_COMPANY` (Owner) or `ROLE_ADMIN`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Matching candidates retrieved successfully",
  "data": [
    {
      "studentId": 1,
      "studentName": "Alex Morgan",
      "rollNumber": "CS-2026-089",
      "aggregatePercentage": 92.5,
      "allMarksVerified": true,
      "matchType": "DIRECT_MATCH",
      "matchScore": 98.0,
      "subjectGaps": []
    }
  ]
}
```

---

### 7.2. Match Companies for Authenticated Student
- **Method**: `GET`
- **Path**: `/api/v1/matches/student`
- **Auth**: `ROLE_STUDENT`
- **Response `200 OK`**: Lists all verified companies and positions where student qualifies.

---

## 8. Master Admin Operations & Diagnostics API (`/api/v1/admin`)

> **Guarded by**: `@PreAuthorize("hasRole('ADMIN') and @securityGuard.isMasterAdmin(principal)")`

### 8.1. Pending Teacher Approval Queue
- `GET /api/v1/admin/teachers/pending`: Lists all faculty awaiting verification.
- `POST /api/v1/admin/teachers/{id}/approve`: Approves teacher application and enables marks verification access.
- `POST /api/v1/admin/teachers/{id}/reject`: Rejects application with custom reason.

### 8.2. Full System CRUD
- **Teachers**: `GET /api/v1/admin/teachers`, `GET /api/v1/admin/teachers/{id}`, `POST /api/v1/admin/teachers`, `DELETE /api/v1/admin/teachers/{id}`.
- **Students**: `GET /api/v1/admin/students`, `GET /api/v1/admin/students/{id}`, `DELETE /api/v1/admin/students/{id}`.
- **Companies**: `GET /api/v1/admin/companies`, `GET /api/v1/admin/companies/{id}`, `DELETE /api/v1/admin/companies/{id}`, `PATCH /api/v1/admin/companies/{id}/status`.

### 8.3. Real-Time System Diagnostics
- **Method**: `GET`
- **Path**: `/api/v1/admin/system/diagnostics`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "System diagnostics retrieved successfully",
  "data": {
    "serverStatus": "HEALTHY",
    "jvmVersion": "21.0.7",
    "uptimeSeconds": 18200,
    "memoryUsage": {
      "usedMemoryMb": 118,
      "freeMemoryMb": 138,
      "totalAllocatedMemoryMb": 256,
      "maxAvailableHeapMb": 256,
      "jvmAvailableProcessors": 2
    },
    "databaseStats": {
      "totalUsers": 284,
      "totalStudents": 240,
      "totalTeachers": 18,
      "totalCompanies": 25,
      "pendingTeacherApprovals": 2
    },
    "mailQuotaStats": {
      "dailyDispatchesCount": 18,
      "dailyQuotaLimit": 400,
      "remainingDailyQuota": 382
    },
    "adminEmail": "amansingh.mothari85@gmail.com",
    "adminRecoveryEmail": "hans31144@gmail.com"
  }
}
```

---

## 9. HTTP Status Codes & Error Handling

| HTTP Status | Meaning | Typical Scenario |
| :--- | :--- | :--- |
| `200 OK` | Success | Standard successful retrieval or mutation. |
| `201 Created` | Created | Successful registration or entity creation. |
| `400 Bad Request` | Validation Error | Cooldown timer active, missing field, or duplicate key. |
| `401 Unauthorized` | Auth Failure | Invalid Master Admin password, missing token, or expired token. |
| `403 Forbidden` | Access Denied | Unapproved teacher (`"No further action, verification is in waiting"`), role mismatch, or non-admin user. |
| `404 Not Found` | Not Found | Entity ID or roll number does not exist. |
| `429 Too Many Requests` | Rate Limited | Exceeded rate limiting bucket (100 req/min/IP). |
| `500 Server Error` | Server Error | Unhandled internal exception. |
