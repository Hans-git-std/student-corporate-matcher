# Student-Corporate Matcher Platform - Complete Frontend Developer Guide

> **Architecture**: React / Vue / Next.js / Vanilla JS Single Page Application (SPA)  
> **API Base URL**: `http://localhost:8080` (or injected `VITE_API_BASE_URL` / `NEXT_PUBLIC_API_URL`)  
> **Auth Architecture**: OTP-based Passwordless Auth + Admin 2-Step (Password + OTP) + 28-day Refresh Token Rotation

---

## 1. Authentication & Session State Management

### 1.1. Session Token Storage
Store tokens securely in frontend state:
- `accessToken`: Store in memory (or secure `sessionStorage` / cookie). Valid for 30 minutes.
- `refreshToken`: Store in `localStorage`. Valid for **28 days** (`2,419,200,000 ms`).
- `userRole`: `ROLE_STUDENT`, `ROLE_TEACHER`, `ROLE_COMPANY`, `ROLE_ADMIN`.

### 1.2. Axios / Fetch Interceptor (Automatic 28-Day Refresh Rotation)
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const res = await axios.post('http://localhost:8080/api/v1/auth/refresh', {
            refreshToken
          });
          const { accessToken, refreshToken: newRefreshToken } = res.data.data;
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', newRefreshToken);
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        } catch (refreshErr) {
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);
```

---

## 2. User Flows & Screen Specifications

### 2.1. Student & Company Login Flow
1. User enters Email and selects Role (`ROLE_STUDENT` or `ROLE_COMPANY`).
2. Call `POST /api/v1/auth/otp/send`:
   ```json
   { "email": "user@domain.com", "role": "ROLE_STUDENT" }
   ```
3. Show 6-digit OTP Input Dialog with a 60-second countdown timer.
4. User enters OTP $\rightarrow$ call `POST /api/v1/auth/otp/verify`:
   ```json
   { "email": "user@domain.com", "otp": "123456" }
   ```
5. Save tokens and redirect to Role Dashboard (`/student/dashboard` or `/company/dashboard`).

---

### 2.2. Teacher Self-Registration & Verification Waiting State
1. **Teacher Registration Screen** (`/teacher/register`):
   - Fields: Full Name, Email, Employee ID, Department, Designation, Phone Number, Assigned Subjects (tags/multi-select).
   - Call `POST /api/v1/teachers/register`.
   - On success (201 Created), display modal:
     > **"Registration Submitted Successfully"**  
     > Your faculty account has been submitted and is currently pending administrator verification. Please check back later once an administrator has approved your application.
2. **Teacher Login Attempt** (`/teacher/login`):
   - When teacher requests OTP via `POST /api/v1/auth/otp/send`:
   - If the backend returns `403 Forbidden` with `"No further action, verification is in waiting"`, show a status banner:
     > ⏳ **Verification Pending**: No further action is required from you. Your verification is in waiting.

---

### 2.3. Admin 2-Step Authentication Flow (`/admin/login`)
1. **Step 1: Admin Password Screen**:
   - Fields: Admin Email (`amansingh.mothari85@gmail.com`), Master Password.
   - Call `POST /api/v1/auth/admin/otp/send`:
     ```json
     {
       "email": "amansingh.mothari85@gmail.com",
       "password": "Admin@RootMaster2026!",
       "sendToRecoveryEmail": true
     }
     ```
2. **Step 2: Admin OTP Verification**:
   - Display notice: *"A 6-digit OTP has been sent to your admin email and recovery email."*
   - Call `POST /api/v1/auth/otp/verify`.
   - On success, redirect to Admin Console (`/admin/dashboard`).

---

### 2.4. Admin Dashboard & Control Panel (`/admin/dashboard`)
1. **Summary Metrics Banner**:
   - Total Students, Teachers, Companies, Verified Companies, Pending Marks Verifications (`GET /api/v1/admin/stats`).
2. **Pending Teacher Applications Tab**:
   - Call `GET /api/v1/admin/teachers/pending`.
   - Table columns: Full Name, Email, Employee ID, Department, Assigned Subjects, Registration Date.
   - Actions: **Approve** (`POST /api/v1/admin/teachers/{id}/approve`) and **Reject** (`POST /api/v1/admin/teachers/{id}/reject` with reason input).
3. **Student Directory Tab**:
   - Call `GET /api/v1/admin/students`.
   - Actions: View Detailed Profile (`GET /api/v1/admin/students/{id}`), Delete Student (`DELETE /api/v1/admin/students/{id}`).
4. **Company Directory Tab**:
   - Call `GET /api/v1/admin/companies`.
   - Actions: Verify/Reject Badge (`PATCH /api/v1/admin/companies/{id}/status`), Edit Company, Delete Company.
5. **System Diagnostics & Low-RAM Health Tab**:
   - Call `GET /api/v1/admin/system/diagnostics`.
   - Render RAM Gauge (e.g. 112MB used / 256MB allocated), Daily Mail Quota Usage (e.g. 14 / 400), Server Uptime, and Active Database Stats.

---

### 2.5. Cold-Start Keep-Alive Background Worker (Optional)
If hosting on free cloud platforms (Render, Fly.io, Railway), include a background keep-alive ping in your frontend application every 4 minutes:
```typescript
setInterval(() => {
  fetch('http://localhost:8080/api/v1/ping').catch(() => {});
}, 4 * 60 * 1000);
```
