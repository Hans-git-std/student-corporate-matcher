# Student-Corporate Matcher API Guide

> **Base URL:** `http://localhost:8080`  
> **Swagger UI (Interactive Playground):** `http://localhost:8080/swagger-ui.html`  
> **OpenAPI JSON Spec:** `http://localhost:8080/v3/api-docs`

---

## 1. Global API Standards

### Standard Success Response Envelope (`ApiResponse<T>`)
```json
{
  "timestamp": "2026-08-18T10:15:30.123Z",
  "status": 200,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Standard Error Response Envelope
```json
{
  "timestamp": "2026-08-18T10:15:30.123Z",
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Invalid email format"
    }
  ]
}
```

### Authentication Header
For all protected endpoints (Student, Teacher, Company, Admin), include the JWT access token in the `Authorization` header:
```http
Authorization: Bearer <your_access_token>
```

---

## 2. Real Email Delivery 

### How OTP Delivery Works
* **Development / Testing Mode (Default):** If no SMTP credentials are configured, the API **prints the 6-digit OTP code directly to the server terminal / console**. This lets developers and QA test immediately without needing an active mail server.
* **Production Mode (Real Inbox Delivery):** When SMTP credentials are provided in your `.env` or container environment, the API automatically sends formatted HTML emails to the user's inbox with a 5-minute security code.

#### Option B: SendGrid / Amazon SES / Brevo / Mailgun
```env
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USERNAME=apikey
SMTP_PASSWORD=your_sendgrid_api_key
MAIL_FROM=auth@yourdomain.com
MAIL_FROM_NAME="Student Corporate Matcher Platform"
```

---

## 3. Authentication Endpoints (`/api/v1/auth`)

### 2.1 Send Email OTP
* **URL:** `POST /api/v1/auth/otp/send`
* **Access:** Public (Rate limited to 60 req/min and 5 OTPs/hr per email)
* **Description:** Generates and dispatches a cryptographically secure 6-digit numeric OTP valid for 5 minutes. In development, the OTP is printed directly in the server console.

#### Request Body
```json
{
  "email": "student@university.edu",
  "role": "ROLE_STUDENT"
}
```
* **Field Validation:**
  * `email` *(Required, String, Valid RFC-5322 email regex)*: Cannot be blank.
  * `role` *(Optional, Enum)*: `ROLE_STUDENT`, `ROLE_TEACHER`, `ROLE_COMPANY`, `ROLE_ADMIN` (Default: `ROLE_STUDENT`).
* **Invalid Input Examples:**
  * `{"email": "not-an-email"}` $\rightarrow$ **400 Bad Request** (`Invalid email format`).
  * `{"email": ""}` $\rightarrow$ **400 Bad Request** (`Email is mandatory`).

#### Success Response (`200 OK`)
```json
{
  "timestamp": "2026-08-18T10:00:00Z",
  "status": 200,
  "message": "OTP generated successfully",
  "data": "OTP has been dispatched to student@university.edu. Code valid for 5 minutes."
}
```

#### cURL / Postman
```bash
curl -X POST http://localhost:8080/api/v1/auth/otp/send \
  -H "Content-Type: application/json" \
  -d '{"email":"student@university.edu","role":"ROLE_STUDENT"}'
```

---

### 2.2 Verify OTP & Login
* **URL:** `POST /api/v1/auth/otp/verify`
* **Access:** Public
* **Description:** Validates the 6-digit numeric OTP and returns a stateless 15-minute JWT Access Token and a 7-day Refresh Token.

#### Request Body
```json
{
  "email": "student@university.edu",
  "otp": "123456"
}
```
* **Field Validation:**
  * `email` *(Required, String, Valid email format)*
  * `otp` *(Required, String, Exactly 6 digits `^\d{6}$`)*
* **Invalid Input Examples:**
  * `{"email": "student@university.edu", "otp": "12"}` $\rightarrow$ **400 Bad Request** (`OTP must be exactly 6 numeric digits`).
  * Expired OTP / 3 failed attempts $\rightarrow$ **400 Bad Request** (`The OTP code has expired / Invalid OTP code`).

#### Success Response (`200 OK`)
```json
{
  "timestamp": "2026-08-18T10:00:05Z",
  "status": 200,
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "8fa3c760-496a-4ee1-bcf3-57bb184e9c70",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "email": "student@university.edu",
    "role": "ROLE_STUDENT"
  }
}
```

#### cURL / Postman
```bash
curl -X POST http://localhost:8080/api/v1/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d '{"email":"student@university.edu","otp":"123456"}'
```

---

### 2.3 Refresh Access Token
* **URL:** `POST /api/v1/auth/refresh`
* **Access:** Public
* **Description:** Rotates the refresh token (burns the existing refresh token and issues a new pair) to prevent token replay attacks.

#### Request Body
```json
{
  "refreshToken": "8fa3c760-496a-4ee1-bcf3-57bb184e9c70"
}
```

#### Success Response (`200 OK`)
```json
{
  "timestamp": "2026-08-18T10:15:00Z",
  "status": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "4b689a71-6c2e-4b47-b873-65d1d6a13b63",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "email": "student@university.edu",
    "role": "ROLE_STUDENT"
  }
}
```

#### cURL / Postman
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"8fa3c760-496a-4ee1-bcf3-57bb184e9c70"}'
```

---

### 2.4 Logout
* **URL:** `POST /api/v1/auth/logout`
* **Access:** Authenticated (Any Role)
* **Description:** Revokes all active refresh tokens in the database for the user.

#### Success Response (`200 OK`)
```json
{
  "timestamp": "2026-08-18T10:30:00Z",
  "status": 200,
  "message": "Logged out",
  "data": "Session successfully terminated"
}
```

---

## 3. Student Endpoints (`/api/v1/students`)
> **Role Required:** `ROLE_STUDENT` (Bearer Token)

### 3.1 Get Student Profile
* **URL:** `GET /api/v1/students/profile`
* **Description:** Fetches full profile, bio, portfolio links, academic marks with verification statuses, and skills.

#### Success Response (`200 OK`)
```json
{
  "timestamp": "2026-08-18T10:00:00Z",
  "status": 200,
  "message": "Student profile retrieved successfully",
  "data": {
    "id": 1,
    "email": "student@university.edu",
    "fullName": "Alex Morgan",
    "rollNumber": "CS-2026-089",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "2003-05-15",
    "gender": "Female",
    "address": "123 University Ave",
    "bio": "Passionate backend engineer with Java & Spring Boot experience.",
    "githubUrl": "https://github.com/alexmorgan",
    "linkedinUrl": "https://linkedin.com/in/alexmorgan",
    "aggregatePercentage": 86.5,
    "allMarksVerified": false,
    "verificationRemark": "Verification by Teacher is Required",
    "academicMarks": [
      {
        "id": 1,
        "subjectName": "Data Structures & Algorithms",
        "selfReportedMarks": 88.0,
        "verifiedMarks": 92.0,
        "isVerified": true,
        "semester": "Semester 4",
        "verifiedByTeacherId": "EMP-FAC-1002",
        "verifiedByTeacherName": "Dr. Alan Turing",
        "verifiedAt": "2026-08-18T09:30:00Z",
        "verificationRemark": "Officially Verified"
      }
    ],
    "skills": [
      {
        "id": 1,
        "skillName": "Java",
        "proficiency": "ADVANCED",
        "yearsOfExperience": 2.5
      }
    ]
  }
}
```

---

### 3.2 Create or Update Student Profile
* **URL:** `PUT /api/v1/students/profile`
* **Description:** Creates or updates profile fields. XSS payload tags are automatically sanitized.

#### Request Body
```json
{
  "fullName": "Alex Morgan",
  "rollNumber": "CS-2026-089",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "2003-05-15",
  "gender": "Female",
  "address": "123 University Ave, Silicon Valley, CA",
  "bio": "Aspiring backend software engineer.",
  "githubUrl": "https://github.com/alexmorgan",
  "linkedinUrl": "https://linkedin.com/in/alexmorgan"
}
```
* **Validation Rules:**
  * `fullName` *(Required, 2-100 chars)*
  * `rollNumber` *(Required, Unique across students)*
  * `bio` *(Max 1000 chars)*
  * `githubUrl`, `linkedinUrl` *(Optional, Valid URL format)*

---

### 3.3 Self-Report Academic Marks
* **URL:** `POST /api/v1/students/marks/self-report`
* **Description:** Submits self-reported subject marks (out of 100). Marks are set to `isVerified: false` with the remark `"Verification by Teacher is Required"`.

#### Request Body
```json
{
  "marks": [
    {
      "subjectName": "Data Structures & Algorithms",
      "marksObtained": 85.5,
      "semester": "Semester 4"
    },
    {
      "subjectName": "Operating Systems",
      "marksObtained": 78.0,
      "semester": "Semester 4"
    }
  ]
}
```
* **Validation Rules:**
  * `marksObtained` *(Required, Float/Double between `0.0` and `100.0`)*.
  * **Invalid Example:** `{"marksObtained": 105.0}` $\rightarrow$ **400 Bad Request** (`Marks cannot exceed 100.0`).

---

### 3.4 Add or Update Skill
* **URL:** `POST /api/v1/students/skills`
* **Description:** Associates a skill and proficiency level with the student profile.

#### Request Body
```json
{
  "skillName": "Spring Boot",
  "proficiency": "ADVANCED",
  "yearsOfExperience": 2.0
}
```
* **Proficiency Levels:** `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`.

---

### 3.5 Delete Skill
* **URL:** `DELETE /api/v1/students/skills/{skillName}`
* **Example:** `DELETE /api/v1/students/skills/Spring%20Boot`

---

### 3.6 Get Company Recommendations & Matches
* **URL:** `GET /api/v1/students/matches`
* **Description:** Evaluates all active hiring criteria against student profile.
  * **Strict Mode:** Matches students meeting 100% of cutoffs and skills.
  * **Relaxed Weighted Mode:** If strict matches $< 3$, calculates score ($60\%$ skill $+ 40\%$ academic) and provides actionable deficit feedback (e.g. `"A 10.0 score more is required in Data Structures & Algorithms"`).
  * **Verification Reminder:** Attaches `"Verification by Teacher is Required"` whenever unverified marks are present.

#### Success Response (`200 OK`)
```json
{
  "timestamp": "2026-08-18T10:00:00Z",
  "status": 200,
  "message": "Company matches evaluated successfully",
  "data": [
    {
      "companyId": 101,
      "companyName": "Acme Cloud Corp",
      "logoUrl": "https://acme.com/logo.png",
      "location": "San Francisco, CA",
      "companyVerificationStatus": "VERIFIED",
      "roleTitle": "Associate Backend Engineer",
      "matchScore": 100.0,
      "matchType": "STRICT",
      "isVerificationPending": true,
      "verificationRemark": "Verification by Teacher is Required",
      "matchedSkills": ["Java", "Spring Boot"],
      "missingSkills": [],
      "subjectGaps": [],
      "academicGapSummary": null
    },
    {
      "companyId": 102,
      "companyName": "Data Systems Inc",
      "logoUrl": null,
      "location": "New York, NY",
      "companyVerificationStatus": "VERIFIED",
      "roleTitle": "Data Engineer",
      "matchScore": 82.5,
      "matchType": "RELAXED_WEIGHTED",
      "isVerificationPending": true,
      "verificationRemark": "Verification by Teacher is Required",
      "matchedSkills": ["Java"],
      "missingSkills": ["Apache Kafka (Requires ADVANCED)"],
      "subjectGaps": [
        {
          "subjectName": "Data Structures & Algorithms",
          "currentScore": 65.0,
          "requiredScore": 75.0,
          "scoreDeficit": 10.0,
          "gapRemark": "A 10.0 score more is required in Data Structures & Algorithms"
        }
      ],
      "academicGapSummary": "A 10.0 score more is required in Data Structures & Algorithms"
    }
  ]
}
```

---

## 4. Teacher Endpoints (`/api/v1/teachers`)
> **Role Required:** `ROLE_TEACHER` (Bearer Token)

### 4.1 Get Teacher Profile
* **URL:** `GET /api/v1/teachers/profile`

### 4.2 Update Teacher Profile
* **URL:** `PUT /api/v1/teachers/profile`
```json
{
  "fullName": "Dr. Alan Turing",
  "employeeId": "EMP-FAC-1002",
  "department": "Computer Science & Engineering",
  "phoneNumber": "+1987654321",
  "assignedSubjects": [
    "Data Structures & Algorithms",
    "Operating Systems",
    "Discrete Mathematics"
  ]
}
```

### 4.3 Lookup Student Marks by Roll Number
* **URL:** `GET /api/v1/teachers/students/{rollNumber}/marks`
* **Example:** `GET /api/v1/teachers/students/CS-2026-089/marks`

### 4.4 Add / Verify Official Student Marks
* **URL:** `POST /api/v1/teachers/students/{rollNumber}/marks/verify`
* **Description:** Confirms or overrides self-reported marks with official faculty audit logs.

#### Request Body
```json
{
  "verifiedMarks": [
    {
      "subjectName": "Data Structures & Algorithms",
      "verifiedMarks": 92.0,
      "semester": "Semester 4",
      "remarks": "Official university exam grade confirmed"
    }
  ]
}
```

---

## 5. Company Endpoints (`/api/v1/companies`)

### 5.1 Browse Public Directory
* **URL:** `GET /api/v1/companies/public`
* **Access:** Public (No token required)
* **Description:** Public catalog showing active criteria and verification badges (`Verified` vs `Not Verified`).

### 5.2 Get Public Company Details
* **URL:** `GET /api/v1/companies/public/{id}`
* **Access:** Public

### 5.3 Self-Register Company
* **URL:** `POST /api/v1/companies/register`
* **Access:** Public
* **Description:** Initializes company account in `NOT_VERIFIED` state with public badge `"Not Verified"` until approved by an Admin.

#### Request Body
```json
{
  "email": "recruiting@fintech.io",
  "companyName": "Fintech Solutions Inc.",
  "industry": "Financial Technology",
  "websiteUrl": "https://fintech.io",
  "location": "Chicago, IL, USA",
  "description": "High-frequency trading and investment platforms."
}
```

### 5.4 Get & Update Authenticated Company Profile
* **URL:** `GET /api/v1/companies/profile` & `PUT /api/v1/companies/profile`
* **Role Required:** `ROLE_COMPANY`

### 5.5 Define Hiring Criteria & Cutoffs
* **URL:** `POST /api/v1/companies/criteria`
* **Role Required:** `ROLE_COMPANY`

#### Request Body
```json
{
  "roleTitle": "Associate Backend Software Engineer",
  "jobDescription": "We are seeking talented fresh graduates with strong fundamentals in CS and Java.",
  "minOverallPercentage": 70.0,
  "requiredSkills": [
    {
      "skillName": "Java",
      "minProficiency": "INTERMEDIATE",
      "isMandatory": true,
      "weightage": 1.5
    },
    {
      "skillName": "Spring Boot",
      "minProficiency": "BEGINNER",
      "isMandatory": false,
      "weightage": 1.0
    }
  ],
  "subjectCutoffs": [
    {
      "subjectName": "Data Structures & Algorithms",
      "minMarksCutoff": 75.0,
      "isMandatory": true
    },
    {
      "subjectName": "Operating Systems",
      "minMarksCutoff": 65.0,
      "isMandatory": false
    }
  ]
}
```

---

## 6. Admin Endpoints (`/api/v1/admin`)
> **Role Required:** `ROLE_ADMIN` (Bearer Token)

### 6.1 List All Companies
* **URL:** `GET /api/v1/admin/companies`

### 6.2 Admin Create Pre-Verified Company
* **URL:** `POST /api/v1/admin/companies`

### 6.3 Update Company Verification Status
* **URL:** `PATCH /api/v1/admin/companies/{id}/status`
* **Description:** Approve (`VERIFIED`), revoke (`NOT_VERIFIED`), or reject (`REJECTED`) a company registration.

#### Request Body
```json
{
  "status": "VERIFIED",
  "adminRemarks": "Corporate credentials and tax documentation verified."
}
```

### 6.4 Provision Teacher Account
* **URL:** `POST /api/v1/admin/teachers`
```json
{
  "email": "hopper@faculty.edu",
  "fullName": "Dr. Grace Hopper",
  "employeeId": "EMP-FAC-1003",
  "department": "Computer Science",
  "phoneNumber": "+1234567899",
  "assignedSubjects": [
    "Compilers",
    "Computer Architecture"
  ]
}
```

### 6.5 System Dashboard Analytics
* **URL:** `GET /api/v1/admin/stats`

#### Success Response (`200 OK`)
```json
{
  "timestamp": "2026-08-18T10:00:00Z",
  "status": 200,
  "message": "Dashboard statistics retrieved successfully",
  "data": {
    "totalStudents": 1250,
    "totalTeachers": 45,
    "totalCompanies": 82,
    "verifiedCompanies": 68,
    "pendingCompanyVerifications": 14,
    "pendingMarksVerifications": 158,
    "totalSuccessfulMatches": 5576
  }
}
```

---

## 7. System Health & Probes

* **Actuator Health Probe:** `GET /actuator/health`
  ```json
  {"status":"UP"}
  ```
