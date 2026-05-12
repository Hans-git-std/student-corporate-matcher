package hanswt7.learnsb.demo.Entity;

import jakarta.persistence.Entity;

import java.util.ArrayList;
@Entity
public class Course {
    private int course_id;
    private ArrayList<Integer> subject_id;
}
