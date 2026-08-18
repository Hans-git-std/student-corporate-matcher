package hanswt7.Controller;

import hanswt7.Service.Teacher;
import org.springframework.web.bind.annotation.*;

// Reply to all Works done by Teacher
@RestController
@RequestMapping('/api/teacher')
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
