package hanswt7.learnsb.demo.Entity;

import hanswt7.learnsb.demo.Entity.Embeddable.Marks;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table
public class Student_marks {
    @Column(unique = true, nullable = false)
    private int roll_no;
    private Marks mark;


}
