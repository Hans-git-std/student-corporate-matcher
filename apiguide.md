# Student-Corporate Matcher Platform - Complete API Reference Guide

> **Base URL**: `http://localhost:8080`  
> **API Version**: `v1` (`/api/v1`)  
> **Documentation / Swagger UI**: `http://localhost:8080/swagger-ui/index.html`  
> **OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`  
> **Public Keep-Alive Ping**: `http://localhost:8080/api/v1/ping` or `http://localhost:8080/ping`

---

## Table of Contents
1. [Authentication & Security Architecture](#1-authentication--security-architecture)
2. [Ping & Keep-Alive API (Public)](#2-ping--keep-alive-api-public)
3. [Authentication Endpoints (`/api/v1/auth`)](#3-authentication-endpoints-apiv1auth)
4. [Teacher & Faculty Endpoints (`/api/v1/teachers`)](#4-teacher--faculty-endpoints-apiv1teachers)
5. [Student Endpoints (`/api/v1/students`)](#5-student-endpoints-apiv1students)
6. [Company Endpoints (`/api/v1/companies`)](#6-company-endpoints-apiv1companies)
7. [Matching Engine Endpoints (`/api/v1/matches`)](#7-matching-engine-endpoints-apiv1matches)
8. [Admin Operations & Diagnostics (`/api/v1/admin`)](#8-admin-operations--diagnostics-apiv1admin)
9. [Standard Error & Status Codes](#9-standard-error--status-codes)

---

## 1. Authentication & Security Architecture

### Standard Response Envelope
All successful responses follow this JSON structure:
```json
{
  "status": 200,
  "message": "Operation description",
  "data": { ... },
  "timestamp": "2026-08-23T04:00:00Z"
}
```

### Authentication Header
Protected endpoints require a standard Bearer Token:
```http
Authorization: Bearer <your_jwt_access_token>
```

### Zero-Trust Principles
- **Master Admin 2-Step Login**: Admin login requires both the master password and a 6-digit email OTP. The OTP is sent to `amansingh.mothari85@gmail.com` and mirrored to `hans31144@gmail.com`.
- **Teacher Approval Lifecycle**: Teachers self-register and enter `PENDING` state. Login is blocked with `"No further action, verification is in waiting"` until an Admin explicitly approves the account.
- **28-Day Refresh Token Rotation**: Long-lived single-use refresh tokens (`2,419,200,000 ms`). Reusing a token immediately invalidates the entire session chain.
- **Daily SMTP Quota Protection**: Daily ceiling of 400 email dispatches, with a 60-second cooldown per email.

---

## 2. Ping & Keep-Alive API (Public)

### `GET /api/v1/ping` or `GET /ping`
- **Purpose**: Ultra-lightweight endpoint that requires no authentication, executes zero database queries, and returns instantly to keep free-tier cloud instances awake and eliminate cold starts.
- **Headers**: None
- **Sample Request**:
```http
GET /api/v1/ping HTTP/1.1
Host: localhost:8080
```
- **Response `200 OK`**:
```json
{
  "status": "UP",
  "pong": true,
  "timestamp": "2026-08-23T04:00:00.123Z",
  "uptimeSeconds": 3600,
  "jvmUptimeMs": 3600123
}
```

---

## 3. Authentication Endpoints (`/api/v1/auth`)

### 1. Request Email OTP (Students & Companies)
- **Method**: `POST`
- **Path**: `/api/v1/auth/otp/send`
- **Auth Required**: No (Public)
- **Rate Limit**: 60-second cooldown per recipient. Max 3 requests/hour/email.
- **Request Body**:
```json
{
  "email": "student@university.edu",
  "role": "ROLE_STUDENT"
}
```
- **Allowed Roles for Public OTP**: `ROLE_STUDENT`, `ROLE_COMPANY`.
- *(Note: `ROLE_ADMIN` will be rejected with 403 Forbidden; use `/admin/otp/send`)*.
- *(Note: `ROLE_TEACHER` with unapproved account will return 403: `"No further action, verification is in waiting"`)*.
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "OTP generated successfully",
  "data": "OTP has been dispatched to student@university.edu. Code valid for 5 minutes.",
  "timestamp": "2026-08-23T04:00:00Z"
}
```

---

### 2. Request Admin 2-Step Login OTP
- **Method**: `POST`
- **Path**: `/api/v1/auth/admin/otp/send`
- **Auth Required**: No (Public Step 1)
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
- **Error Cases**:
  - `401 Unauthorized`: Invalid password.
  - `403 Forbidden`: Email is not the designated Master Admin.

---

### 3. Verify OTP & Log In
- **Method**: `POST`
- **Path**: `/api/v1/auth/otp/verify`
- **Auth Required**: No (Public Step 2)
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

### 4. Refresh Access Token (28-Day Rotation)
- **Method**: `POST`
- **Path**: `/api/v1/auth/refresh` or `/api/v1/auth/token/refresh`
- **Auth Required**: No
- **Request Body**:
```json
{
  "refreshToken": "7c8e9f2a-1b3c-4d5e-6f7a-8b9c0d1e2f3a"
}
```
- **Response `200 OK`**: Returns new Access Token and freshly rotated Refresh Token.

---

### 5. Logout & Invalidate Session
- **Method**: `POST`
- **Path**: `/api/v1/auth/logout`
- **Auth Required**: Yes (`ROLE_STUDENT`, `ROLE_TEACHER`, `ROLE_COMPANY`, `ROLE_ADMIN`)
- **Response `200 OK`**: Revokes all refresh tokens for the active user.

---

## 4. Teacher & Faculty Endpoints (`/api/v1/teachers`)

### 1. Teacher Self-Registration
- **Method**: `POST`
- **Path**: `/api/v1/teachers/register`
- **Auth Required**: No (Public)
- **Request Body**:
```json
{
  "fullName": "Dr. Barbara Liskov",
  "email": "liskov@faculty.edu",
  "employeeId": "EMP-FAC-1042",
  "department": "Computer Science & Engineering",
  "designation": "Associate Professor",
  "phoneNumber": "+1234567890",
  "assignedSubjects": [
    "Object Oriented Design",
    "Distributed Systems"
  ]
}
```
- **Response `201 Created`**:
```json
{
  "status": 200,
  "message": "Faculty registration submitted successfully. Your account is pending administrator verification before login.",
  "data": {
    "id": 12,
    "email": "liskov@faculty.edu",
    "fullName": "Dr. Barbara Liskov",
    "employeeId": "EMP-FAC-1042",
    "department": "Computer Science & Engineering",
    "designation": "Associate Professor",
    "phoneNumber": "+1234567890",
    "approvalStatus": "PENDING",
    "rejectionReason": null,
    "verifiedByAdminAt": null,
    "createdAt": "2026-08-23T04:00:00Z",
    "assignedSubjects": [
      "Object Oriented Design",
      "Distributed Systems"
    ]
  },
  "timestamp": "2026-08-23T04:00:00Z"
}
```

---

### 2. Get Teacher Profile
- **Method**: `GET`
- **Path**: `/api/v1/teachers/profile`
- **Auth Required**: Yes (`ROLE_TEACHER` with `APPROVED` status)
- **Response `200 OK`**: Returns teacher's profile and assigned subjects.

---

### 3. Update Teacher Profile
- **Method**: `PUT`
- **Path**: `/api/v1/teachers/profile`
- **Auth Required**: Yes (`ROLE_TEACHER`)
- **Request Body**:
```json
{
  "fullName": "Dr. Barbara Liskov",
  "employeeId": "EMP-FAC-1042",
  "department": "Computer Science & Engineering",
  "phoneNumber": "+1987654321",
  "assignedSubjects": [
    "Object Oriented Design",
    "Distributed Systems",
    "Cloud Computing"
  ]
}
```

---

### 4. Fetch Student Marks by Roll Number
- **Method**: `GET`
- **Path**: `/api/v1/teachers/students/{rollNumber}/marks`
- **Auth Required**: Yes (`ROLE_TEACHER`)
- **Sample URL**: `/api/v1/teachers/students/CS-2026-089/marks`
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Student marks retrieved successfully",
  "data": [
    {
      "id": 1,
      "subjectName": "Data Structures & Algorithms",
      "selfReportedMarks": 88.0,
      "verifiedMarks": null,
      "isVerified": false,
      "semester": "Semester 3",
      "verifiedByTeacherId": null,
      "verifiedByTeacherName": null,
      "verifiedAt": null,
      "verificationRemark": "Verification by Teacher is Required"
    }
  ],
  "timestamp": "2026-08-23T04:00:00Z"
}
```

---

### 5. Verify Student Marks
- **Method**: `POST`
- **Path**: `/api/v1/teachers/students/{rollNumber}/marks/verify`
- **Auth Required**: Yes (`ROLE_TEACHER` with `APPROVED` status)
- **Request Body**:
```json
{
  "verifiedMarks": [
    {
      "subjectName": "Data Structures & Algorithms",
      "verifiedMarks": 91.5,
      "semester": "Semester 3",
      "remarks": "Verified against mid-term paper review"
    }
  ]
}
```
- **Response `200 OK`**: Returns updated marks list with `isVerified: true` and teacher audit badge.

---

## 5. Student Endpoints (`/api/v1/students`)

### 1. Get Student Profile
- **Method**: `GET`
- **Path**: `/api/v1/students/profile`
- **Auth Required**: Yes (`ROLE_STUDENT`)

### 2. Update Student Profile
- **Method**: `PUT`
- **Path**: `/api/v1/students/profile`
- **Auth Required**: Yes (`ROLE_STUDENT`)
- **Request Body**:
```json
{
  "fullName": "Alex Morgan",
  "rollNumber": "CS-2026-089",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "2003-05-15",
  "gender": "Female",
  "address": "123 Campus Ave, Silicon Valley, CA",
  "bio": "Passionate backend engineer with expertise in Spring Boot & cloud architectures.",
  "githubUrl": "https://github.com/alexmorgan",
  "linkedinUrl": "https://linkedin.com/in/alexmorgan"
}
```

### 3. Self-Report Academic Marks
- **Method**: `POST`
- **Path**: `/api/v1/students/marks`
- **Auth Required**: Yes (`ROLE_STUDENT`)
- **Request Body**:
```json
{
  "marks": [
    {
      "subjectName": "Data Structures & Algorithms",
      "marks": 88.0,
      "semester": "Semester 3"
    },
    {
      "subjectName": "Database Management Systems",
      "marks": 94.0,
      "semester": "Semester 4"
    }
  ]
}
```

### 4. Manage Student Skills
- **Add Skills**: `POST /api/v1/students/skills`
- **Delete Skill**: `DELETE /api/v1/students/skills/{skillName}`

---

## 6. Company Endpoints (`/api/v1/companies`)

### 1. Company Self-Registration
- **Method**: `POST`
- **Path**: `/api/v1/companies/register`
- **Auth Required**: No (Public)

### 2. Public Verified Companies List
- **Method**: `GET`
- **Path**: `/api/v1/companies/public`
- **Auth Required**: No (Public)

### 3. Manage Hiring Criteria
- **Add Criteria**: `POST /api/v1/companies/criteria`
- **Update Criteria**: `PUT /api/v1/companies/criteria/{id}`
- **Delete Criteria**: `DELETE /api/v1/companies/criteria/{id}`

---

## 7. Matching Engine Endpoints (`/api/v1/matches`)

### 1. Match Students Against Company Criteria
- **Method**: `GET`
- **Path**: `/api/v1/matches/criteria/{criteriaId}`
- **Auth Required**: Yes (`ROLE_COMPANY` owner or `ROLE_ADMIN`)

### 2. Match Companies For Authenticated Student
- **Method**: `GET`
- **Path**: `/api/v1/matches/student`
- **Auth Required**: Yes (`ROLE_STUDENT`)

---

## 8. Admin Operations & Diagnostics (`/api/v1/admin`)

> **Note**: All `/api/v1/admin/**` endpoints are protected with `@PreAuthorize("hasRole('ADMIN') and @securityGuard.isMasterAdmin(principal)")`.

### 1. Pending Teacher Approval Queue
- **Method**: `GET`
- **Path**: `/api/v1/admin/teachers/pending`
- **Response `200 OK`**: Returns array of all faculty members with `approvalStatus: "PENDING"`.

### 2. Approve Teacher Registration
- **Method**: `POST`
- **Path**: `/api/v1/admin/teachers/{id}/approve`
- **Response `200 OK`**: Sets status to `APPROVED` and records `verifiedByAdminAt`.

### 3. Reject Teacher Registration
- **Method**: `POST`
- **Path**: `/api/v1/admin/teachers/{id}/reject`
- **Request Body**:
```json
{
  "reason": "Invalid faculty credentials. Please re-register with official college ID."
}
```

### 4. Teacher List & CRUD
- **List All Teachers**: `GET /api/v1/admin/teachers`
- **Get Teacher Details**: `GET /api/v1/admin/teachers/{id}`
- **Directly Provision Teacher**: `POST /api/v1/admin/teachers`
- **Delete Teacher**: `DELETE /api/v1/admin/teachers/{id}`

### 5. Student List & CRUD
- **List All Students**: `GET /api/v1/admin/students`
- **Get Student Details**: `GET /api/v1/admin/students/{id}`
- **Delete Student**: `DELETE /api/v1/admin/students/{id}`

### 6. Company Verification & CRUD
- **List All Companies**: `GET /api/v1/admin/companies`
- **Get Company Details**: `GET /api/v1/admin/companies/{id}`
- **Create Company**: `POST /api/v1/admin/companies`
- **Update Company**: `PUT /api/v1/admin/companies/{id}`
- **Delete Company**: `DELETE /api/v1/admin/companies/{id}`
- **Update Verification Status**: `PATCH /api/v1/admin/companies/{id}/status`

### 7. System Diagnostics & Health
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
    "uptimeSeconds": 14200,
    "memoryUsage": {
      "usedMemoryMb": 112,
      "freeMemoryMb": 144,
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
      "dailyDispatchesCount": 14,
      "dailyQuotaLimit": 400,
      "remainingDailyQuota": 386
    },
    "adminEmail": "amansingh.mothari85@gmail.com",
    "adminRecoveryEmail": "hans31144@gmail.com"
  },
  "timestamp": "2026-08-23T04:00:00Z"
}
```

---

## 9. Standard Error & Status Codes

| Status Code | Reason | Example Description |
| :--- | :--- | :--- |
| `200 OK` | Success | Operation succeeded |
| `201 Created` | Created | Resource successfully created |
| `400 Bad Request` | Validation Error | Missing required fields, cooldown active, or invalid input |
| `401 Unauthorized` | Auth Error | Missing token, expired token, or invalid admin password |
| `403 Forbidden` | Access Denied | Unapproved teacher (`"No further action, verification is in waiting"`), role mismatch, or non-admin user |
| `404 Not Found` | Resource Not Found | ID or entity does not exist in the database |
| `429 Too Many Requests`| Rate Limited | Exceeded 100 requests/minute per IP address |
| `500 Internal Error`| Server Error | Unexpected internal server exception |
