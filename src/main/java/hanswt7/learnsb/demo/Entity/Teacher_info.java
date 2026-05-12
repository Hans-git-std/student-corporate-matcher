package hanswt7.learnsb.demo.Entity;

import hanswt7.learnsb.demo.Entity.Embeddable.Address;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

@Entity
public class Teacher_info {
    private String name;
    private int  age;
    private int teacher_id;
    @Embedded
    private Address address;


}
