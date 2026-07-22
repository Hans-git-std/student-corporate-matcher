package hanswt7.Entity;

import hanswt7.Entity.Embeddable.Marks;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table
public class StudentMarks {
    @Column(unique = true, nullable = false)
    private int roll_no;
    private Marks mark;
    @Transient
    private final String ROLE = "Student";

    public int getRoll_no() {
        return roll_no;
    }

    public Marks getMark() {
        return mark;
    }

    public void setMark(Marks mark) {
        this.mark = mark;
    }

    public String getROLE() {
        return ROLE;
    }
}
