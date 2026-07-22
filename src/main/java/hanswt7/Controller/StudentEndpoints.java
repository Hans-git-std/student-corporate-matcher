package hanswt7.Controller;

import hanswt7.Service.Student;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
