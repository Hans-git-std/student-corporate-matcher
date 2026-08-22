package com.matcher.platform.service;

import com.matcher.platform.dto.request.CompanyRegisterRequest;
import com.matcher.platform.dto.response.CompanyProfileResponse;
import com.matcher.platform.dto.response.CompanyPublicResponse;
import com.matcher.platform.entity.CompanyProfile;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private HiringCriteriaRepository hiringCriteriaRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private CriteriaRequiredSkillRepository criteriaRequiredSkillRepository;

    @Mock
    private CriteriaSubjectCutoffRepository criteriaSubjectCutoffRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyService companyService;

    private User companyUser;
    private CompanyProfile company;

    @BeforeEach
    void setUp() {
        companyUser = new User("recruiting@acmetech.com", RoleType.ROLE_COMPANY);
        company = CompanyProfile.builder()
                .id(1L)
                .user(companyUser)
                .companyName("Acme Technologies Inc.")
                .industry("Cloud Computing")
                .verificationStatus(CompanyVerificationStatus.NOT_VERIFIED)
                .hiringCriteria(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should self-register company with NOT_VERIFIED status and badge")
    void testRegisterCompany() {
        CompanyRegisterRequest request = CompanyRegisterRequest.builder()
                .email("hr@newcorp.com")
                .companyName("New Corp")
                .industry("Fintech")
                .build();

        User newUser = new User("hr@newcorp.com", RoleType.ROLE_COMPANY);
        when(userRepository.existsByEmail("hr@newcorp.com")).thenReturn(false);
        when(companyProfileRepository.existsByCompanyNameIgnoreCase("New Corp")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(companyProfileRepository.save(any(CompanyProfile.class))).thenAnswer(i -> {
            CompanyProfile p = i.getArgument(0);
            p.setId(2L);
            return p;
        });

        CompanyProfileResponse response = companyService.registerCompany(request);

        assertThat(response.getCompanyName()).isEqualTo("New Corp");
        assertThat(response.getVerificationStatus()).isEqualTo(CompanyVerificationStatus.NOT_VERIFIED);
        assertThat(response.getVerificationBadge()).isEqualTo("Not Verified");
    }

    @Test
    @DisplayName("Should return public company list with verification badge")
    void testGetPublicCompanies() {
        when(companyProfileRepository.findAllWithCriteria()).thenReturn(List.of(company));

        List<CompanyPublicResponse> list = companyService.getPublicCompanies();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCompanyName()).isEqualTo("Acme Technologies Inc.");
        assertThat(list.get(0).getVerificationBadge()).isEqualTo("Not Verified");
    }
}
