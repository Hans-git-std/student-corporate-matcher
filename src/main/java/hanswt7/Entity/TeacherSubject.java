package hanswt7.Entity;

import hanswt7.Entity.Embeddable.Subject;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
public class TeacherSubject {
    private int Teacher_id;
    @Embedded
    private Subject teacher_subject_id;
    @Transient
    private final String ROLE = "Teacher";

    public int getTeacher_id() {
        return Teacher_id;
    }

    public void setTeacher_id(int teacher_id) {
        Teacher_id = teacher_id;
    }

    public Subject getTeacher_subject_id() {
        return teacher_subject_id;
    }

    public void setTeacher_subject_id(Subject teacher_subject_id) {
        this.teacher_subject_id = teacher_subject_id;
    }

    public String getROLE() {
        return ROLE;
    }
}
