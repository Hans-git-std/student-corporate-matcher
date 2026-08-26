package com.matcher.platform.service;

import com.matcher.platform.dto.request.*;
import com.matcher.platform.dto.response.*;
import com.matcher.platform.entity.*;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.exception.BadRequestException;
import com.matcher.platform.exception.ResourceNotFoundException;
import com.matcher.platform.security.XssSanitizer;
import com.matcher.platform.repository.*;
import com.matcher.platform.util.StringNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CompanyService {

    private final CompanyProfileRepository companyProfileRepository;
    private final HiringCriteriaRepository hiringCriteriaRepository;
    private final SkillRepository skillRepository;
    private final CriteriaRequiredSkillRepository criteriaRequiredSkillRepository;
    private final CriteriaSubjectCutoffRepository criteriaSubjectCutoffRepository;
    private final UserRepository userRepository;

    public CompanyService(
            CompanyProfileRepository companyProfileRepository,
            HiringCriteriaRepository hiringCriteriaRepository,
            SkillRepository skillRepository,
            CriteriaRequiredSkillRepository criteriaRequiredSkillRepository,
            CriteriaSubjectCutoffRepository criteriaSubjectCutoffRepository,
            UserRepository userRepository
    ) {
        this.companyProfileRepository = companyProfileRepository;
        this.hiringCriteriaRepository = hiringCriteriaRepository;
        this.skillRepository = skillRepository;
        this.criteriaRequiredSkillRepository = criteriaRequiredSkillRepository;
        this.criteriaSubjectCutoffRepository = criteriaSubjectCutoffRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyPublicResponse> getPublicCompanies() {
        List<CompanyProfile> companies = companyProfileRepository.findAllWithCriteria();
        return companies.stream().map(this::mapToPublicResponse).toList();
    }

    @Transactional(readOnly = true)
    public CompanyPublicResponse getPublicCompanyById(Long id) {
        CompanyProfile company = companyProfileRepository.findWithCriteriaById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        return mapToPublicResponse(company);
    }

    public CompanyProfileResponse registerCompany(CompanyRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }
        if (companyProfileRepository.existsByCompanyNameIgnoreCase(request.getCompanyName())) {
            throw new BadRequestException("Company name '" + request.getCompanyName() + "' is already registered");
        }

        User user = userRepository.save(new User(request.getEmail(), RoleType.ROLE_COMPANY));

        CompanyProfile profile = CompanyProfile.builder()
                .user(user)
                .companyName(XssSanitizer.sanitize(request.getCompanyName()))
                .industry(XssSanitizer.sanitize(request.getIndustry()))
                .websiteUrl(XssSanitizer.sanitize(request.getWebsiteUrl()))
                .location(XssSanitizer.sanitize(request.getLocation()))
                .description(XssSanitizer.sanitize(request.getDescription()))
                .logoUrl(request.getLogoUrl() != null ? XssSanitizer.sanitize(request.getLogoUrl()) : null)
                .verificationStatus(CompanyVerificationStatus.NOT_VERIFIED)
                .build();

        CompanyProfile saved = companyProfileRepository.save(profile);
        return mapToProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public CompanyProfileResponse getProfile(String email) {
        CompanyProfile profile = companyProfileRepository.findWithCriteriaByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "email", email));
        return mapToProfileResponse(profile);
    }

    public CompanyProfileResponse updateProfile(String email, CompanyProfileRequest request) {
        CompanyProfile profile = companyProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "email", email));

        if (!profile.getCompanyName().equalsIgnoreCase(request.getCompanyName().trim()) &&
                companyProfileRepository.existsByCompanyNameIgnoreCase(request.getCompanyName().trim())) {
            throw new BadRequestException("Company name '" + request.getCompanyName() + "' is already in use");
        }

        profile.setCompanyName(XssSanitizer.sanitize(request.getCompanyName()));
        profile.setIndustry(XssSanitizer.sanitize(request.getIndustry()));
        profile.setWebsiteUrl(XssSanitizer.sanitize(request.getWebsiteUrl()));
        profile.setLocation(XssSanitizer.sanitize(request.getLocation()));
        profile.setDescription(XssSanitizer.sanitize(request.getDescription()));
        profile.setLogoUrl(XssSanitizer.sanitize(request.getLogoUrl()));

        CompanyProfile saved = companyProfileRepository.save(profile);
        return mapToProfileResponse(saved);
    }

    public HiringCriteriaResponse defineHiringCriteria(String email, HiringCriteriaRequest request) {
        CompanyProfile company = companyProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "email", email));

        HiringCriteria criteria = HiringCriteria.builder()
                .company(company)
                .roleTitle(XssSanitizer.sanitize(request.getRoleTitle()))
                .jobDescription(XssSanitizer.sanitize(request.getJobDescription()))
                .minOverallPercentage(request.getMinOverallPercentage())
                .isActive(true)
                .build();

        HiringCriteria savedCriteria = hiringCriteriaRepository.save(criteria);

        // Required skills
        List<CriteriaRequiredSkill> requiredSkills = new ArrayList<>();
        if (request.getRequiredSkills() != null) {
            for (RequiredSkillEntry skillEntry : request.getRequiredSkills()) {
                String skillName = StringNormalizer.normalize(skillEntry.getSkillName());
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> skillRepository.save(new Skill(skillName)));

                requiredSkills.add(new CriteriaRequiredSkill(
                        savedCriteria,
                        skill,
                        skillEntry.getMinProficiency(),
                        skillEntry.getIsMandatory(),
                        skillEntry.getWeightage()
                ));
            }
            criteriaRequiredSkillRepository.saveAll(requiredSkills);
            savedCriteria.setRequiredSkills(requiredSkills);
        }

        // Subject cutoffs
        List<CriteriaSubjectCutoff> subjectCutoffs = new ArrayList<>();
        if (request.getSubjectCutoffs() != null) {
            for (SubjectCutoffEntry cutoffEntry : request.getSubjectCutoffs()) {
                String subjectName = StringNormalizer.normalize(cutoffEntry.getSubjectName());
                subjectCutoffs.add(new CriteriaSubjectCutoff(
                        savedCriteria,
                        subjectName,
                        cutoffEntry.getMinMarksCutoff(),
                        cutoffEntry.getIsMandatory()
                ));
            }
            criteriaSubjectCutoffRepository.saveAll(subjectCutoffs);
            savedCriteria.setSubjectCutoffs(subjectCutoffs);
        }

        return mapToCriteriaResponse(savedCriteria);
    }

    @Transactional(readOnly = true)
    public List<HiringCriteriaResponse> getHiringCriteria(String email) {
        CompanyProfile company = companyProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "email", email));

        List<HiringCriteria> list = hiringCriteriaRepository.findByCompanyId(company.getId());
        return list.stream().map(this::mapToCriteriaResponse).toList();
    }

    public HiringCriteriaResponse updateHiringCriteria(String email, Long criteriaId, HiringCriteriaRequest request) {
        CompanyProfile company = companyProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "email", email));

        HiringCriteria criteria = hiringCriteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("HiringCriteria", "id", criteriaId));

        if (!criteria.getCompany().getId().equals(company.getId())) {
            throw new BadRequestException("Hiring criteria ID " + criteriaId + " does not belong to your company");
        }

        criteria.setRoleTitle(XssSanitizer.sanitize(request.getRoleTitle()));
        criteria.setJobDescription(XssSanitizer.sanitize(request.getJobDescription()));
        criteria.setMinOverallPercentage(request.getMinOverallPercentage());

        // Update skills
        criteria.getRequiredSkills().clear();
        if (request.getRequiredSkills() != null) {
            for (RequiredSkillEntry skillEntry : request.getRequiredSkills()) {
                String skillName = StringNormalizer.normalize(skillEntry.getSkillName());
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> skillRepository.save(new Skill(skillName)));

                criteria.getRequiredSkills().add(new CriteriaRequiredSkill(
                        criteria,
                        skill,
                        skillEntry.getMinProficiency(),
                        skillEntry.getIsMandatory(),
                        skillEntry.getWeightage()
                ));
            }
        }

        // Update cutoffs
        criteria.getSubjectCutoffs().clear();
        if (request.getSubjectCutoffs() != null) {
            for (SubjectCutoffEntry cutoffEntry : request.getSubjectCutoffs()) {
                String subjectName = StringNormalizer.normalize(cutoffEntry.getSubjectName());
                criteria.getSubjectCutoffs().add(new CriteriaSubjectCutoff(
                        criteria,
                        subjectName,
                        cutoffEntry.getMinMarksCutoff(),
                        cutoffEntry.getIsMandatory()
                ));
            }
        }

        HiringCriteria saved = hiringCriteriaRepository.save(criteria);
        return mapToCriteriaResponse(saved);
    }

    public void deleteHiringCriteria(String email, Long criteriaId) {
        CompanyProfile company = companyProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "email", email));

        HiringCriteria criteria = hiringCriteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("HiringCriteria", "id", criteriaId));

        if (!criteria.getCompany().getId().equals(company.getId())) {
            throw new BadRequestException("Hiring criteria ID " + criteriaId + " does not belong to your company");
        }

        hiringCriteriaRepository.delete(criteria);
    }

    private CompanyProfileResponse mapToProfileResponse(CompanyProfile company) {
        List<HiringCriteriaResponse> criteriaResponses = new ArrayList<>();
        if (company.getHiringCriteria() != null) {
            for (HiringCriteria hc : company.getHiringCriteria()) {
                criteriaResponses.add(mapToCriteriaResponse(hc));
            }
        }

        CompanyVerificationStatus status = company.getVerificationStatus() != null ? company.getVerificationStatus() : CompanyVerificationStatus.NOT_VERIFIED;
        String badge = status == CompanyVerificationStatus.VERIFIED ? "Verified" : "Not Verified";

        return CompanyProfileResponse.builder()
                .id(company.getId())
                .email(company.getUser() != null ? company.getUser().getEmail() : null)
                .companyName(company.getCompanyName())
                .industry(company.getIndustry())
                .websiteUrl(company.getWebsiteUrl())
                .location(company.getLocation())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .verificationStatus(status)
                .verificationBadge(badge)
                .hiringCriteria(criteriaResponses)
                .build();
    }

    private CompanyPublicResponse mapToPublicResponse(CompanyProfile company) {
        List<HiringCriteriaResponse> criteriaResponses = new ArrayList<>();
        if (company.getHiringCriteria() != null) {
            for (HiringCriteria hc : company.getHiringCriteria()) {
                if (Boolean.TRUE.equals(hc.getIsActive())) {
                    criteriaResponses.add(mapToCriteriaResponse(hc));
                }
            }
        }

        CompanyVerificationStatus status = company.getVerificationStatus() != null ? company.getVerificationStatus() : CompanyVerificationStatus.NOT_VERIFIED;
        String badge = status == CompanyVerificationStatus.VERIFIED ? "Verified" : "Not Verified";

        return CompanyPublicResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .industry(company.getIndustry())
                .websiteUrl(company.getWebsiteUrl())
                .location(company.getLocation())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .verificationStatus(status)
                .verificationBadge(badge)
                .activeCriteria(criteriaResponses)
                .build();
    }

    public HiringCriteriaResponse mapToCriteriaResponse(HiringCriteria hc) {
        List<RequiredSkillResponse> skills = new ArrayList<>();
        if (hc.getRequiredSkills() != null) {
            for (CriteriaRequiredSkill rs : hc.getRequiredSkills()) {
                skills.add(new RequiredSkillResponse(
                        rs.getId(),
                        rs.getSkill() != null ? rs.getSkill().getName() : null,
                        rs.getMinProficiency(),
                        rs.getIsMandatory(),
                        rs.getWeightage()
                ));
            }
        }

        List<SubjectCutoffResponse> cutoffs = new ArrayList<>();
        if (hc.getSubjectCutoffs() != null) {
            for (CriteriaSubjectCutoff sc : hc.getSubjectCutoffs()) {
                cutoffs.add(new SubjectCutoffResponse(
                        sc.getId(),
                        sc.getSubjectName(),
                        sc.getMinMarksCutoff(),
                        sc.getIsMandatory()
                ));
            }
        }

        return HiringCriteriaResponse.builder()
                .id(hc.getId())
                .roleTitle(hc.getRoleTitle())
                .jobDescription(hc.getJobDescription())
                .minOverallPercentage(hc.getMinOverallPercentage())
                .requiredSkills(skills)
                .subjectCutoffs(cutoffs)
                .build();
    }
}
