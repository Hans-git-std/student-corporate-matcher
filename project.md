# Student-Corporate Matcher Platform - System Architecture & Internal Engineering Guide

> **Enterprise Grade**: Spring Boot 3.4.2 + Java 21 LTS + TiDB Cloud (Serverless MySQL Dialect)  
> **Security Protocol**: Zero-Trust, Master Admin 2-Step (Password + OTP), 28-Day Refresh Rotation, IDOR Protection, Faculty Approval Queue  
> **Server Efficiency Profile**: Optimized for Low-RAM (~250MB JVM on 500MB Host) with Serial GC & Tiered C1 JIT

---

## 1. System Architecture & ER Diagram

```mermaid
erDiagram
    USERS ||--o| STUDENT_PROFILES : "has one (1:1)"
    USERS ||--o| TEACHER_PROFILES : "has one (1:1)"
    USERS ||--o| COMPANY_PROFILES : "has one (1:1)"
    USERS ||--o{ REFRESH_TOKENS : "has many (1:N)"

    STUDENT_PROFILES ||--o{ STUDENT_ACADEMIC_RECORDS : "has records (1:N)"
    STUDENT_PROFILES ||--o{ STUDENT_SKILLS : "has skills (1:N)"
    SKILLS ||--o{ STUDENT_SKILLS : "categorized by (1:N)"

    TEACHER_PROFILES ||--o{ TEACHER_SUBJECTS : "teaches (1:N)"
    TEACHER_PROFILES ||--o{ STUDENT_ACADEMIC_RECORDS : "verifies marks (1:N)"

    COMPANY_PROFILES ||--o{ HIRING_CRITERIA : "defines criteria (1:N)"
    HIRING_CRITERIA ||--o{ CRITERIA_REQUIRED_SKILLS : "requires (1:N)"
    SKILLS ||--o{ CRITERIA_REQUIRED_SKILLS : "specified in (1:N)"

    USERS {
        bigint id PK
        varchar email UK "unique, indexed"
        varchar password_hash "BCrypt (admin/password accounts)"
        varchar role "ROLE_STUDENT, ROLE_TEACHER, ROLE_COMPANY, ROLE_ADMIN"
        boolean enabled "default true"
        timestamp created_at
        timestamp updated_at
    }

    TEACHER_PROFILES {
        bigint id PK
        bigint user_id FK "unique"
        varchar employee_id UK "unique, indexed"
        varchar full_name
        varchar department
        varchar designation
        varchar phone_number
        varchar approval_status "PENDING, APPROVED, REJECTED"
        varchar rejection_reason
        timestamp verified_by_admin_at
        timestamp created_at
        timestamp updated_at
    }

    STUDENT_PROFILES {
        bigint id PK
        bigint user_id FK "unique"
        varchar roll_number UK "unique, indexed"
        varchar full_name
        varchar phone_number
        varchar gender
        text bio
        varchar github_url
        varchar linkedin_url
        timestamp created_at
        timestamp updated_at
    }

    STUDENT_ACADEMIC_RECORDS {
        bigint id PK
        bigint student_id FK
        varchar subject_name
        double self_reported_marks
        double verified_marks
        boolean is_verified
        varchar semester
        bigint verified_by_teacher_id FK "nullable"
        timestamp verified_at
        varchar remarks
    }

    COMPANY_PROFILES {
        bigint id PK
        bigint user_id FK "unique"
        varchar company_name UK
        varchar industry
        varchar website_url
        varchar location
        text description
        varchar logo_url
        varchar verification_status "VERIFIED, NOT_VERIFIED, REJECTED"
        varchar admin_remarks
    }

    HIRING_CRITERIA {
        bigint id PK
        bigint company_id FK
        varchar role_title
        text job_description
        double min_overall_percentage
        timestamp created_at
    }

    REFRESH_TOKENS {
        bigint id PK
        bigint user_id FK
        varchar token_hash UK "SHA-256 indexed"
        timestamp expires_at "28 days"
        boolean is_revoked
        timestamp created_at
    }
```

---

## 2. Core Security & Lifecycle Workflows

### 2.1. Master Admin 2-Step Login & Emergency Recovery
1. Admin initiates login at `POST /api/v1/auth/admin/otp/send` with `email` + `password`.
2. Server validates password against BCrypt `password_hash`.
3. Server generates 6-digit OTP and dispatches it to `amansingh.mothari85@gmail.com` and mirror security copy to `hans31144@gmail.com`.
4. Admin submits OTP at `POST /api/v1/auth/otp/verify` to receive JWT + 28-day Refresh Token.
5. All administrative routes (`/api/v1/admin/**`) are guarded via SpEL:
   `@PreAuthorize("hasRole('ADMIN') and @securityGuard.isMasterAdmin(principal)")`

### 2.2. Teacher Self-Registration & Approval Lifecycle
1. Faculty self-register at `POST /api/v1/teachers/register`.
2. Account is provisioned with `approvalStatus = PENDING`.
3. If teacher attempts to log in before Admin verification, `AuthService` throws:
   `ForbiddenException("No further action, verification is in waiting")`.
4. Admin inspects pending queue via `GET /api/v1/admin/teachers/pending`.
5. Admin approves via `POST /api/v1/admin/teachers/{id}/approve`.
6. Teacher can now log in and perform official mark verifications.

### 2.3. Anti-Flood Mail Quota Limiter
- **Daily Quota**: 400 emails/day, resetting at 00:00 UTC.
- **Per-Email Anti-Flood**: 60-second cooldown between resend requests.

---

## 3. Low-RAM Deployment Configuration (500MB Host Target)

### JVM Tuning
- **Garbage Collector**: `-XX:+UseSerialGC` (Eliminates parallel GC worker thread stacks, saving ~60MB).
- **Heap Limits**: `-Xms96m -Xmx256m` (Caps JVM heap at 256MB).
- **Metaspace Limit**: `-XX:MaxMetaspaceSize=128m`.
- **JIT Compilation**: `-XX:+TieredCompilation -XX:TieredStopAtLevel=1` (C1 JIT only; fast startup in <3s, saving ~60MB code cache).
- **Database Connection Pool**: HikariCP `maximum-pool-size=3` and `minimum-idle=1`.

### Micro Ping Keep-Alive
- Endpoint: `GET /api/v1/ping` or `GET /ping`
- Characteristics: Zero database queries, zero security filter overhead, instant sub-millisecond response for external ping services (e.g. UptimeRobot, cron jobs) to prevent cloud cold starts.
