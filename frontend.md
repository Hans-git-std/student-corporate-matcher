# Frontend Developer Implementation Guide: Student-Corporate Matcher

> **Backend API Base URL:** `http://localhost:8080`  
> **Interactive Swagger Documentation:** `http://localhost:8080/swagger-ui.html`

This guide provides everything a frontend engineer needs to build the user interface (React, Vue, Next.js, Angular, or Svelte) without referencing backend source code.

---

## 0. Authentication & Email OTP Behavior (Important for Frontend Devs)

### How to Test OTP in Frontend Development
1. **Local Development (No SMTP Required):**
   * When you click **"Send OTP"** on the frontend, the backend generates the 6-digit code and **prints it directly in the Spring Boot terminal/console**.
   * Simply look at the backend terminal to get the code (e.g. `[SECURITY CODE] Your 6-Digit One-Time Login Code: 123456`) and enter it into your UI.
2. **Production / Staging (Real Inbox Delivery):**
   * When the backend `.env` is configured with SMTP credentials, the user receives an HTML email in their inbox from `noreply@studentmatcher.com` (or your domain).

### 30-Second SMTP Configuration (`.env`)
If you or your team want real emails sent to real inboxes during testing:
```env
# Gmail Example:
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your_16_digit_gmail_app_password
MAIL_FROM=your-email@gmail.com
MAIL_FROM_NAME="Student Corporate Matcher Platform"
```

---

## 1. Design System & UI Tokens

### 1.1 Color Palette
```css
:root {
  /* Brand Primary */
  --primary-50: #eff6ff;
  --primary-500: #3b82f6;
  --primary-600: #2563eb;
  --primary-700: #1d4ed8;

  /* Status Colors */
  --success-500: #10b981; /* Verified / Strict Match */
  --warning-500: #f59e0b; /* Verification Required / Relaxed Match */
  --danger-500: #ef4444;  /* Unverified / Deficit Gaps / Error */

  /* Neutral Dark Mode & Light Mode */
  --bg-main: #0f172a;
  --bg-card: #1e293b;
  --text-primary: #f8fafc;
  --text-secondary: #94a3b8;
  --border-color: #334155;
}
```

### 1.2 Verification Badges & Match Pills
* **`VERIFIED` Company:** Green Badge `✓ Verified`
* **`NOT_VERIFIED` Company:** Amber Badge `⚠ Not Verified`
* **`REJECTED` Company:** Red Badge `✕ Rejected`
* **Pending Marks Reminder:** Amber Callout Banner:  
  `"Verification by Teacher is Required"`
* **Match Types:**
  * `STRICT`: Green Pill `100% Strict Fit`
  * `RELAXED_WEIGHTED`: Amber Pill `Closest Match (Weighted Fit)`

---

## 2. Complete TypeScript Type Definitions (`types/api.ts`)

```typescript
// Standard API Envelope
export interface ApiResponse<T> {
  timestamp: string;
  status: number;
  message: string;
  data: T;
  errors?: ErrorDetail[];
}

export interface ErrorDetail {
  field: string;
  rejectedValue: any;
  message: string;
}

// User & Auth
export type RoleType = 'ROLE_STUDENT' | 'ROLE_TEACHER' | 'ROLE_COMPANY' | 'ROLE_ADMIN';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  email: string;
  role: RoleType;
}

// Student Data Models
export type ProficiencyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface SubjectMarkResponse {
  id?: number;
  subjectName: string;
  selfReportedMarks: number;
  verifiedMarks?: number;
  isVerified: boolean;
  semester?: string;
  verifiedByTeacherId?: string;
  verifiedByTeacherName?: string;
  verifiedAt?: string;
  verificationRemark?: string;
}

export interface StudentSkillResponse {
  id?: number;
  skillName: string;
  proficiency: ProficiencyLevel;
  yearsOfExperience: number;
}

export interface StudentProfileResponse {
  id: number;
  email: string;
  fullName: string;
  rollNumber: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  gender?: string;
  address?: string;
  bio?: string;
  githubUrl?: string;
  linkedinUrl?: string;
  aggregatePercentage?: number;
  allMarksVerified: boolean;
  verificationRemark: string;
  academicMarks: SubjectMarkResponse[];
  skills: StudentSkillResponse[];
}

// Matching Engine Responses
export interface SubjectGapDetail {
  subjectName: string;
  currentScore: number;
  requiredScore: number;
  scoreDeficit: number;
  gapRemark: string;
}

export interface CompanyMatchResponse {
  companyId: number;
  companyName: string;
  logoUrl?: string;
  location?: string;
  companyVerificationStatus: 'VERIFIED' | 'NOT_VERIFIED' | 'REJECTED';
  roleTitle: string;
  matchScore: number;
  matchType: 'STRICT' | 'RELAXED_WEIGHTED';
  isVerificationPending: boolean;
  verificationRemark?: string;
  matchedSkills: string[];
  missingSkills: string[];
  subjectGaps: SubjectGapDetail[];
  academicGapSummary?: string;
}

// Teacher Data Models
export interface TeacherProfileResponse {
  id: number;
  email: string;
  fullName: string;
  employeeId: string;
  department: string;
  phoneNumber?: string;
  assignedSubjects: string[];
}

// Company Data Models
export interface CompanyPublicResponse {
  id: number;
  companyName: string;
  industry?: string;
  websiteUrl?: string;
  location?: string;
  description?: string;
  logoUrl?: string;
  verificationStatus: 'VERIFIED' | 'NOT_VERIFIED' | 'REJECTED';
  verificationBadge: string;
  activeCriteria: HiringCriteriaResponse[];
}

export interface HiringCriteriaResponse {
  id: number;
  roleTitle: string;
  jobDescription?: string;
  minOverallPercentage?: number;
  isActive: boolean;
  requiredSkills: RequiredSkillResponse[];
  subjectCutoffs: SubjectCutoffResponse[];
}

export interface RequiredSkillResponse {
  skillName: string;
  minProficiency: ProficiencyLevel;
  isMandatory: boolean;
  weightage: number;
}

export interface SubjectCutoffResponse {
  subjectName: string;
  minMarksCutoff: number;
  isMandatory: boolean;
}

// Admin Statistics
export interface AdminDashboardStatsResponse {
  totalStudents: number;
  totalTeachers: number;
  totalCompanies: number;
  verifiedCompanies: number;
  pendingCompanyVerifications: number;
  pendingMarksVerifications: number;
  totalSuccessfulMatches: number;
}
```

---

## 3. Axios API Client with Silent Token Refresh (`lib/apiClient.ts`)

```typescript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach Access Token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response Interceptor: Auto Refresh on 401
let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: any) => void }> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const res = await axios.post('http://localhost:8080/api/v1/auth/refresh', { refreshToken });
        const { accessToken, refreshToken: newRefreshToken } = res.data.data;

        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        apiClient.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;

        processQueue(null, accessToken);
        return apiClient(originalRequest);
      } catch (refreshErr) {
        processQueue(refreshErr, null);
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

---

## 4. UI/UX Screen Specifications & Workflows

### 4.1 Authentication Screens (`/login`)
1. **Step 1 - Email Input:**
   * User inputs email address and selects target role (Student, Teacher, Company, Admin).
   * Clicks **"Send OTP"** $\rightarrow$ Triggers `POST /api/v1/auth/otp/send`.
2. **Step 2 - 6-Digit OTP Code Verification:**
   * 6-box numeric PIN input with 5-minute countdown timer.
   * Clicks **"Verify & Login"** $\rightarrow$ Triggers `POST /api/v1/auth/otp/verify`.
   * On success, stores tokens in `localStorage` and routes to role-specific dashboard (`/student`, `/teacher`, `/company`, `/admin`).

---

### 4.2 Student Dashboard (`/student`)
1. **Profile Card:** Full name, roll number, aggregate score %, bio, GitHub/LinkedIn links, and the Teacher Verification banner (`"Verification by Teacher is Required"`).
2. **Self-Reporting Academic Marks:**
   * Dynamic table where student inputs Subject Name, Score (0.0 to 100.0), and Semester.
   * Visual badge shows `Pending Verification` until verified by faculty.
3. **Skills Manager:**
   * Dropdown to select/add skills (`Java`, `Spring Boot`, `React`, `Python`) with proficiency (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`).
4. **Smart Recommended Companies Section (`/student/matches`):**
   * Displays company match cards:
     * **Company Name, Role Title, Location, and Verification Badge**
     * **Match Score Gauge:** 100% (Strict) vs. Calculated Score % (Weighted)
     * **Matched Skills:** Green chips
     * **Missing Skills:** Gray chips with required proficiency
     * **Academic Gap Callout:** If relaxed mode, displays amber alert:  
       `"A 10.0 score more is required in Data Structures & Algorithms"`
     * **Verification Notice:** Amber warning icon:  
       `"Verification by Teacher is Required"`

---

### 4.3 Teacher Portal (`/teacher`)
1. **Student Search Bar:** Input student **Roll Number** (e.g. `CS-2026-089`).
2. **Academic Verification Table:**
   * Shows student's self-reported marks side-by-side with official verification input box.
   * Teacher enters official mark, optional notes, and clicks **"Verify & Confirm"** (`POST /api/v1/teachers/students/{rollNumber}/marks/verify`).
   * Displays audit metadata (`Verified by Dr. Alan Turing on Aug 18, 2026`).

---

### 4.4 Company Portal (`/company`)
1. **Company Profile & Status:**
   * Shows corporate info and verification badge (**`Verified`** or **`Not Verified`**).
2. **Hiring Criteria Builder:**
   * Role Title (e.g. "Junior Backend Developer") and Job Description.
   * Minimum Overall GPA Cutoff %.
   * Technical Skills list: Select skill, minimum required proficiency, weightage slider (0.5 to 3.0), and `Mandatory` checkbox.
   * Subject Cutoffs: Select subject, minimum mark (0-100), and `Mandatory` checkbox.
   * Clicks **"Publish Criteria"** (`POST /api/v1/companies/criteria`).

---

### 4.5 Admin Console (`/admin`)
1. **Platform Analytics Cards:** Total Students, Teachers, Registered Companies, Verified Companies, Total Matches Generated.
2. **Company Verification Queue:**
   * Table of unverified company registrations.
   * Action buttons: **Approve (`VERIFIED`)**, **Reject (`REJECTED`)** with remarks modal (`PATCH /api/v1/admin/companies/{id}/status`).
3. **Faculty Account Provisioning:**
   * Form to create teacher accounts with assigned subjects (`POST /api/v1/admin/teachers`).

---

## 5. Ready-to-Use Frontend Implementation Checklist

- [ ] Initialize project with TypeScript & Tailwind CSS / CSS Modules
- [ ] Configure `apiClient.ts` with Axios interceptors
- [ ] Implement `AuthContext` with role-based routing guards (`<ProtectedRoute role="ROLE_STUDENT">`)
- [ ] Build Authentication View (Email input + 6-digit OTP modal)
- [ ] Build Student Profile & Self-Report Marks form
- [ ] Build Student Matches View with score deficit chips and verification warnings
- [ ] Build Teacher Verification Portal with roll number search
- [ ] Build Company Criteria Builder
- [ ] Build Admin Verification Console
