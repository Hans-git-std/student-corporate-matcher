package hanswt7.Controller;

import hanswt7.Service.Student;
import org.springframework.web.bind.annotation.*;

// Reply to all Works done by Student
@RestController
@RequestMapping('api/student/')
public class StudentEndpoints {
    @PostMapping
    public void addStudent(Student student) {

    }

    public void updateStudent(Student student) {

    }

    @DeleteMapping
    public void deleteStudent(Student student) {

    }

}
