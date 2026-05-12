package hanswt7.learnsb.demo.Entity.Embeddable;

import jakarta.persistence.Embeddable;

import java.util.HashMap;
@Embeddable
public class Marks {
    private HashMap<Subject,Integer> marks;

    public Marks(HashMap<Subject, Integer> marks) {
        this.marks = marks;
    }

    public HashMap<Subject, Integer> getMarks() {
        return marks;
    }

    public void setMarks(HashMap<Subject, Integer> marks) {
        this.marks = marks;
    }
}
