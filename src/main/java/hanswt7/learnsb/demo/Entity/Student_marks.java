package hanswt7.learnsb.demo.Entity;

import hanswt7.learnsb.demo.Entity.Embeddable.Marks;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table
public class Student_marks {
    @Column(unique = true, nullable = false)
    private int roll_no;
    private Marks mark;
    @Transient
    private final String ROLE = "Student";

    public int getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(int roll_no) {
        this.roll_no = roll_no;
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
