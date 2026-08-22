package com.matcher.platform.service;

import com.matcher.platform.dto.request.TeacherMarkVerificationEntry;
import com.matcher.platform.dto.request.VerifyMarksRequest;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.entity.StudentAcademicRecord;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.entity.TeacherProfile;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.repository.StudentAcademicRecordRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.repository.TeacherProfileRepository;
import com.matcher.platform.repository.TeacherSubjectRepository;
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
class TeacherServiceTest {

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private TeacherSubjectRepository teacherSubjectRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private StudentAcademicRecordRepository academicRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeacherService teacherService;

    private TeacherProfile teacher;
    private StudentProfile student;

    @BeforeEach
    void setUp() {
        User teacherUser = new User("turing@faculty.edu", RoleType.ROLE_TEACHER);
        teacher = TeacherProfile.builder()
                .id(1L)
                .user(teacherUser)
                .fullName("Dr. Alan Turing")
                .employeeId("EMP-FAC-1002")
                .department("Computer Science")
                .approvalStatus(com.matcher.platform.entity.enums.ApprovalStatus.APPROVED)
                .assignedSubjects(new ArrayList<>())
                .build();

        User studentUser = new User("student@university.edu", RoleType.ROLE_STUDENT);
        student = StudentProfile.builder()
                .id(1L)
                .user(studentUser)
                .fullName("Alex Morgan")
                .rollNumber("CS-2026-089")
                .build();
    }

    @Test
    @DisplayName("Should verify student marks and attach teacher audit information")
    void testVerifyStudentMarks() {
        VerifyMarksRequest request = VerifyMarksRequest.builder()
                .verifiedMarks(List.of(new TeacherMarkVerificationEntry("Data Structures", 92.0, "Semester 2", "Excellent grasp of trees & graphs")))
                .build();

        when(teacherProfileRepository.findByUserEmail("turing@faculty.edu")).thenReturn(Optional.of(teacher));
        when(studentProfileRepository.findByRollNumber("CS-2026-089")).thenReturn(Optional.of(student));
        when(academicRecordRepository.findByStudentIdAndSubjectNameIgnoreCase(1L, "Data Structures")).thenReturn(Optional.empty());
        when(academicRecordRepository.save(any(StudentAcademicRecord.class))).thenAnswer(i -> i.getArgument(0));

        List<SubjectMarkResponse> result = teacherService.verifyStudentMarks("turing@faculty.edu", "CS-2026-089", request);

        assertThat(result).hasSize(1);
        SubjectMarkResponse mark = result.get(0);
        assertThat(mark.getSubjectName()).isEqualTo("Data Structures");
        assertThat(mark.getVerifiedMarks()).isEqualTo(92.0);
        assertThat(mark.getIsVerified()).isTrue();
        assertThat(mark.getVerifiedByTeacherId()).isEqualTo("EMP-FAC-1002");
        assertThat(mark.getVerifiedByTeacherName()).isEqualTo("Dr. Alan Turing");
        assertThat(mark.getVerificationRemark()).isEqualTo("Officially Verified");
    }
}
