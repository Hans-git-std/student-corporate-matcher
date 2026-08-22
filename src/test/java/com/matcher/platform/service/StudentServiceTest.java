package com.matcher.platform.service;

import com.matcher.platform.dto.request.SelfReportMarksRequest;
import com.matcher.platform.dto.request.StudentProfileRequest;
import com.matcher.platform.dto.request.SubjectMarkEntry;
import com.matcher.platform.dto.response.StudentProfileResponse;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.entity.StudentAcademicRecord;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.repository.SkillRepository;
import com.matcher.platform.repository.StudentAcademicRecordRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.StudentSkillRepository;
import com.matcher.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private StudentAcademicRecordRepository academicRecordRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private StudentSkillRepository studentSkillRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentService studentService;

    private User user;
    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        user = new User(1L, "student@university.edu", RoleType.ROLE_STUDENT, true, null, null);
        profile = StudentProfile.builder()
                .id(1L)
                .user(user)
                .fullName("Alex Morgan")
                .rollNumber("CS-2026-089")
                .academicRecords(new ArrayList<>())
                .skills(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create or update student profile successfully")
    void testUpdateOrCreateProfile() {
        StudentProfileRequest request = StudentProfileRequest.builder()
                .fullName("Alex Morgan Updated")
                .rollNumber("CS-2026-089")
                .phoneNumber("+1234567890")
                .gender("Female")
                .build();

        when(userRepository.findByEmail("student@university.edu")).thenReturn(Optional.of(user));
        when(studentProfileRepository.findByUserEmail("student@university.edu")).thenReturn(Optional.of(profile));
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(i -> i.getArgument(0));

        StudentProfileResponse response = studentService.updateOrCreateProfile("student@university.edu", request);

        assertThat(response.getFullName()).isEqualTo("Alex Morgan Updated");
        assertThat(response.getRollNumber()).isEqualTo("CS-2026-089");
    }

    @Test
    @DisplayName("Should self report subject marks with unverified state")
    void testSelfReportMarks() {
        SelfReportMarksRequest request = SelfReportMarksRequest.builder()
                .marks(List.of(new SubjectMarkEntry("Mathematics", 88.0, "Semester 1")))
                .build();

        StudentAcademicRecord savedRecord = StudentAcademicRecord.builder()
                .id(1L)
                .student(profile)
                .subjectName("Mathematics")
                .selfReportedMarks(88.0)
                .isVerified(false)
                .semester("Semester 1")
                .build();

        when(studentProfileRepository.findByUserEmail("student@university.edu")).thenReturn(Optional.of(profile));
        when(academicRecordRepository.findByStudentIdAndSubjectNameIgnoreCase(1L, "Mathematics")).thenReturn(Optional.empty());
        when(academicRecordRepository.save(any(StudentAcademicRecord.class))).thenReturn(savedRecord);

        List<SubjectMarkResponse> marks = studentService.selfReportMarks("student@university.edu", request);

        assertThat(marks).hasSize(1);
        assertThat(marks.get(0).getSubjectName()).isEqualTo("Mathematics");
        assertThat(marks.get(0).getIsVerified()).isFalse();
        assertThat(marks.get(0).getVerificationRemark()).isEqualTo("Verification by Teacher is Required");
    }
}
