package com.matcher.platform.security;

import com.matcher.platform.entity.CompanyProfile;
import com.matcher.platform.entity.HiringCriteria;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.repository.CompanyProfileRepository;
import com.matcher.platform.repository.HiringCriteriaRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.TeacherProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

@Component("securityGuard")
public class SecurityGuard {

    private final StudentProfileRepository studentProfileRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final HiringCriteriaRepository hiringCriteriaRepository;
    private final TeacherProfileRepository teacherProfileRepository;

    @Value("${app.security.admin.email:admin@studentmatcher.com}")
    private String masterAdminEmail;

    public SecurityGuard(
            StudentProfileRepository studentProfileRepository,
            CompanyProfileRepository companyProfileRepository,
            HiringCriteriaRepository hiringCriteriaRepository,
            TeacherProfileRepository teacherProfileRepository
    ) {
        this.studentProfileRepository = studentProfileRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.hiringCriteriaRepository = hiringCriteriaRepository;
        this.teacherProfileRepository = teacherProfileRepository;
    }

    public boolean isMasterAdmin(Object principal) {
        String username = extractUsername(principal);
        if (username == null || masterAdminEmail == null) {
            return false;
        }
        return username.trim().equalsIgnoreCase(masterAdminEmail.trim());
    }

    public boolean isValidTeacher(Object principal) {
        String username = extractUsername(principal);
        if (username == null) {
            return false;
        }
        return teacherProfileRepository.findByUserEmail(username.trim())
                .map(tp -> tp.getApprovalStatus() == com.matcher.platform.entity.enums.ApprovalStatus.APPROVED)
                .orElse(false);
    }

    public boolean isStudentOwner(Object principal, Long studentProfileId) {
        String username = extractUsername(principal);
        if (username == null || studentProfileId == null) {
            return false;
        }
        Optional<StudentProfile> profileOpt = studentProfileRepository.findById(studentProfileId);
        return profileOpt.map(p -> p.getUser() != null && username.equalsIgnoreCase(p.getUser().getEmail()))
                .orElse(false);
    }

    public boolean isCompanyCriteriaOwner(Object principal, Long criteriaId) {
        String username = extractUsername(principal);
        if (username == null || criteriaId == null) {
            return false;
        }
        Optional<HiringCriteria> criteriaOpt = hiringCriteriaRepository.findById(criteriaId);
        if (criteriaOpt.isEmpty()) {
            return false;
        }
        CompanyProfile company = criteriaOpt.get().getCompany();
        return company != null && company.getUser() != null &&
                username.equalsIgnoreCase(company.getUser().getEmail());
    }

    public boolean isCompanyOwner(Object principal, Long companyId) {
        String username = extractUsername(principal);
        if (username == null || companyId == null) {
            return false;
        }
        Optional<CompanyProfile> companyOpt = companyProfileRepository.findById(companyId);
        return companyOpt.map(c -> c.getUser() != null && username.equalsIgnoreCase(c.getUser().getEmail()))
                .orElse(false);
    }

    private String extractUsername(Object principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof Principal p) {
            return p.getName();
        }
        if (principal instanceof String str) {
            return str;
        }
        return principal.toString();
    }
}
