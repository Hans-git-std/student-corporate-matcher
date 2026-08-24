# 💻 Student-Corporate Matcher Platform - Frontend Developer Handbook

> **Production API Base URL**: `https://student-corporate-matcher.onrender.com/api/v1`  
> **Local Development API Base URL**: `http://localhost:8080/api/v1`  
> **Target Framework**: React / Vue / Next.js / Angular / Vanilla JS (SPA)  
> **Auth Model**: Passwordless Email OTP + Admin 2-Step (Password + OTP) + 28-Day Refresh Token Rotation

---

## 1. Quick Setup & Axios Interceptor

### 1.1. Environment Configuration (`.env`)
```bash
# In your frontend project root
VITE_API_BASE_URL=https://student-corporate-matcher.onrender.com/api/v1
# Or for local development:
# VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### 1.2. Complete Axios HTTP Client with 28-Day Token Rotation
```typescript
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://student-corporate-matcher.onrender.com/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// 1. Request Interceptor: Attach Bearer JWT
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('accessToken');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 2. Response Interceptor: Seamless 28-Day Refresh Token Rotation
let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: any) => void }> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest: any = error.config;

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

      const storedRefreshToken = localStorage.getItem('refreshToken');
      if (!storedRefreshToken) {
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const refreshResponse = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken: storedRefreshToken,
        });

        const { accessToken, refreshToken: newRefreshToken } = refreshResponse.data.data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        processQueue(null, accessToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
```

---

## 2. Cold-Start Keep-Alive Background Worker

Render free instances spin down after 15 minutes of inactivity. Add this lightweight background pinger in your frontend entry point (e.g. `App.tsx` or `index.html`):

```typescript
// Keep-Alive Service: Pings every 4 minutes (sub-millisecond zero-DB micro-ping)
export function startKeepAliveWorker() {
  const pingUrl = `${API_BASE_URL}/ping`.replace('/api/v1/ping', '/api/v1/ping');
  setInterval(async () => {
    try {
      await fetch(pingUrl, { method: 'GET', mode: 'cors' });
      console.debug('[Keep-Alive] Server ping successful.');
    } catch {
      // Ignored
    }
  }, 4 * 60 * 1000);
}
```

---

## 3. UI Flows & Implementation Details

### 3.1. Master Admin 2-Step Login (`/admin/login`)

```
+-------------------------------------------------------+
|                 👑 MASTER ADMIN LOGIN                 |
+-------------------------------------------------------+
| Admin Email:    [ amansingh.mothari85@gmail.com     ] |
| Master Password:[ ••••••••••••••••••••••••••••••     ] |
|                                                       |
| [  Request 2-Step OTP Code  ]                         |
+-------------------------------------------------------+
```

1. **Step 1 Request**:
   ```typescript
   await apiClient.post('/auth/admin/otp/send', {
     email: adminEmail,
     password: adminPassword,
     sendToRecoveryEmail: true // Sends to hans31144@gmail.com as backup
   });
   ```
2. **Step 2 Modal**:
   - Prompt: *"A 6-digit OTP has been sent to your admin email and backup recovery inbox."*
   - User inputs 6 digits $\rightarrow$ call `/auth/otp/verify`:
   ```typescript
   const res = await apiClient.post('/auth/otp/verify', {
     email: adminEmail,
     otp: enteredOtp
   });
   // Save res.data.data.accessToken and res.data.data.refreshToken
   // Navigate to /admin/dashboard
   ```

---

### 3.2. Teacher Self-Registration & Verification Waiting State

```
+-------------------------------------------------------+
|               FACULTY SELF-REGISTRATION               |
+-------------------------------------------------------+
| Full Name:       [ Dr. Alan Turing                  ] |
| Email:           [ turing@faculty.edu               ] |
| Employee ID:     [ EMP-FAC-1002                     ] |
| Department:      [ Computer Science & Engineering   ] |
| Designation:     [ Professor                        ] |
| Phone Number:    [ +1987654321                      ] |
| Assigned Subjects (Multi-Select):                     |
|  [ Theory of Computation x ]  [ Algorithms x ]        |
|                                                       |
| [  Submit Faculty Registration  ]                     |
+-------------------------------------------------------+
```

1. **Registration Call**:
   ```typescript
   await apiClient.post('/teachers/register', formData);
   ```
2. **Success Feedback**:
   - Display banner: *"Registration submitted! Your faculty account is pending administrator verification before login."*
3. **Handling Login Attempts While Pending**:
   - If a teacher attempts login before Admin approval, the backend returns:
     `403 Forbidden: "No further action, verification is in waiting"`
   - Catch this exact message in your frontend and display a clean status card:
     > ⏳ **Verification Pending**: No further action is required from you. Your verification is in waiting.

---

### 3.3. Student Portal (`/student/dashboard`)

1. **Profile & Aggregate Score**:
   - `GET /students/profile` $\rightarrow$ Render aggregate percentage badge and verified marks breakdown.
2. **Self-Report Marks**:
   - `POST /students/marks` $\rightarrow$ Submit semester marks. Unverified marks display a *"Verification by Teacher is Required"* badge until verified by faculty.
3. **Matched Companies**:
   - `GET /matches/student` $\rightarrow$ Render eligible hiring companies, direct matches, and subject gap details.

---

### 3.4. Teacher Portal (`/teacher/dashboard`)

1. **Fetch Student by Roll Number**:
   - `GET /teachers/students/{rollNumber}/marks`
2. **Official Marks Verification Form**:
   - Teacher inputs verified marks $\rightarrow$ `POST /teachers/students/{rollNumber}/marks/verify`.
   - The verified marks instantly show a green **"Officially Verified"** badge with the faculty member's name and audit timestamp.

---

### 3.5. Admin Console & Real-Time Diagnostics (`/admin/dashboard`)

1. **Pending Teacher Approvals Tab**:
   - `GET /admin/teachers/pending`
   - Actions: **Approve** (`POST /admin/teachers/{id}/approve`) or **Reject** (`POST /admin/teachers/{id}/reject` with reason modal).
2. **Student & Company Management**:
   - Full CRUD tables with search, edit, and delete actions.
3. **Server Diagnostics Card**:
   - `GET /admin/system/diagnostics`
   - Render RAM usage bar (`usedMemoryMb / totalAllocatedMemoryMb`), daily SMTP email quota counter (`dailyDispatchesCount / 400`), and active database entity counts.

---

## 4. Standard Response Types (TypeScript)

```typescript
export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface AuthResponseData {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  email: string;
  role: 'ROLE_STUDENT' | 'ROLE_TEACHER' | 'ROLE_COMPANY' | 'ROLE_ADMIN';
}

export interface SystemDiagnosticsData {
  serverStatus: string;
  jvmVersion: string;
  uptimeSeconds: number;
  memoryUsage: {
    usedMemoryMb: number;
    freeMemoryMb: number;
    totalAllocatedMemoryMb: number;
    maxAvailableHeapMb: number;
  };
  databaseStats: {
    totalUsers: number;
    totalStudents: number;
    totalTeachers: number;
    totalCompanies: number;
    pendingTeacherApprovals: number;
  };
  mailQuotaStats: {
    dailyDispatchesCount: number;
    dailyQuotaLimit: number;
    remainingDailyQuota: number;
  };
  adminEmail: string;
  adminRecoveryEmail: string;
}
```
