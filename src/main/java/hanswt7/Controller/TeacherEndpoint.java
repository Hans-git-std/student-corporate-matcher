package hanswt7.Controller;

import hanswt7.Service.Teacher;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

// Reply to all Works done by Teacher
@RestController
public class TeacherEndpoint {
    @PostMapping
    public void addTeacher(Teacher teacher) {
    }
    @PutMapping
    public void updateTeacher(Teacher teacher) {

    }
    @DeleteMapping
    public void deleteTeacher(Teacher teacher) {

    }


}
