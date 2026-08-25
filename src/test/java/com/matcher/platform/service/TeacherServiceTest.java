package com.matcher.platform.service;

import com.matcher.platform.dto.request.TeacherMarkVerificationEntry;
import com.matcher.platform.dto.request.VerifyMarksRequest;
import com.matcher.platform.dto.response.SubjectMarkResponse;
import com.matcher.platform.entity.StudentAcademicRecord;
import com.matcher.platform.entity.StudentProfile;
import com.matcher.platform.entity.TeacherProfile;
import com.matcher.platform.entity.TeacherSubject;
import com.matcher.platform.entity.User;
import com.matcher.platform.entity.enums.RoleType;
import com.matcher.platform.exception.BadRequestException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @DisplayName("Should verify student marks and attach teacher audit information when unrestricted")
    void testVerifyStudentMarks() {
        VerifyMarksRequest request = VerifyMarksRequest.builder()
                .verifiedMarks(List.of(new TeacherMarkVerificationEntry("Data Structures", 92.0, "Semester 2", "Excellent grasp of trees & graphs")))
                .build();

        when(teacherProfileRepository.findWithSubjectsByEmail("turing@faculty.edu")).thenReturn(Optional.of(teacher));
        when(studentProfileRepository.findByRollNumber("CS-2026-089")).thenReturn(Optional.of(student));
        when(academicRecordRepository.findByStudentId(1L)).thenReturn(List.of());
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

    @Test
    @DisplayName("Should successfully verify mark when subject matches teacher's assigned subjects (with spacing/case normalization)")
    void testVerifyAuthorizedSubject() {
        teacher.setAssignedSubjects(List.of(
                new TeacherSubject(teacher, "Data Structures & Algorithms"),
                new TeacherSubject(teacher, "Operating Systems")
        ));

        VerifyMarksRequest request = VerifyMarksRequest.builder()
                .verifiedMarks(List.of(new TeacherMarkVerificationEntry("  Data   Structures & Algorithms  ", 95.0, "Semester 3", "Top performer")))
                .build();

        when(teacherProfileRepository.findWithSubjectsByEmail("turing@faculty.edu")).thenReturn(Optional.of(teacher));
        when(studentProfileRepository.findByRollNumber("CS-2026-089")).thenReturn(Optional.of(student));
        when(academicRecordRepository.findByStudentId(1L)).thenReturn(List.of());
        when(academicRecordRepository.save(any(StudentAcademicRecord.class))).thenAnswer(i -> i.getArgument(0));

        List<SubjectMarkResponse> result = teacherService.verifyStudentMarks("turing@faculty.edu", "CS-2026-089", request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubjectName()).isEqualTo("Data Structures & Algorithms");
        assertThat(result.get(0).getVerifiedMarks()).isEqualTo(95.0);
    }

    @Test
    @DisplayName("Should throw BadRequestException when teacher attempts to verify unauthorized subject")
    void testVerifyUnauthorizedSubjectThrowsException() {
        teacher.setAssignedSubjects(List.of(
                new TeacherSubject(teacher, "Data Structures"),
                new TeacherSubject(teacher, "Operating Systems")
        ));

        VerifyMarksRequest request = VerifyMarksRequest.builder()
                .verifiedMarks(List.of(new TeacherMarkVerificationEntry("Thermodynamics", 85.0, "Semester 3", "Good")))
                .build();

        when(teacherProfileRepository.findWithSubjectsByEmail("turing@faculty.edu")).thenReturn(Optional.of(teacher));
        when(studentProfileRepository.findByRollNumber("CS-2026-089")).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> teacherService.verifyStudentMarks("turing@faculty.edu", "CS-2026-089", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not authorized to verify marks for subject 'Thermodynamics'");
    }

    @Test
    @DisplayName("Should update existing student academic record when teacher enters minor typo in subject name")
    void testFuzzyMatchUpdatesExistingRecord() {
        teacher.setAssignedSubjects(List.of(
                new TeacherSubject(teacher, "Operating Systems")
        ));

        StudentAcademicRecord existingRecord = StudentAcademicRecord.builder()
                .id(10L)
                .student(student)
                .subjectName("Operating Systems")
                .selfReportedMarks(78.0)
                .isVerified(false)
                .build();

        // Teacher enters with slight typo / spacing difference "Operating  System"
        VerifyMarksRequest request = VerifyMarksRequest.builder()
                .verifiedMarks(List.of(new TeacherMarkVerificationEntry("Operating System", 88.0, "Semester 4", "Verified against answer script")))
                .build();

        when(teacherProfileRepository.findWithSubjectsByEmail("turing@faculty.edu")).thenReturn(Optional.of(teacher));
        when(studentProfileRepository.findByRollNumber("CS-2026-089")).thenReturn(Optional.of(student));
        when(academicRecordRepository.findByStudentId(1L)).thenReturn(List.of(existingRecord));
        when(academicRecordRepository.save(any(StudentAcademicRecord.class))).thenAnswer(i -> i.getArgument(0));

        List<SubjectMarkResponse> result = teacherService.verifyStudentMarks("turing@faculty.edu", "CS-2026-089", request);

        assertThat(result).hasSize(1);
        assertThat(existingRecord.getVerifiedMarks()).isEqualTo(88.0);
        assertThat(existingRecord.getIsVerified()).isTrue();
        assertThat(existingRecord.getVerifiedByTeacher()).isEqualTo(teacher);
    }
}
