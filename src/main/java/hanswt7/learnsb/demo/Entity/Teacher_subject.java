package hanswt7.learnsb.demo.Entity;

import hanswt7.learnsb.demo.Entity.Embeddable.Subject;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Teacher_subject {
    private int Teacher_id;
    @Embedded
    private Subject teacher_subject_id;
}
