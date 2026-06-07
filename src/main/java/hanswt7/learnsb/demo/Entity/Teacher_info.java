package hanswt7.learnsb.demo.Entity;

import hanswt7.learnsb.demo.Entity.Embeddable.Address;
import jakarta.persistence.*;

@Entity
public class Teacher_info {
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(unique = true, nullable = false)
    private long phone_number;
    private int  age;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int teacher_id;
    private boolean gender;

    @Override
    public String toString() {
        return "Teacher_info{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone_number=" + phone_number +
                ", age=" + age +
                ", teacher_id=" + teacher_id +
                ", gender=" + gender +
                ", address=" + address +
                '}';
    }

    @Embedded
    private Address address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(long phone_number) {
        this.phone_number = phone_number;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getTeacher_id() {
        return teacher_id;
    }

    public void setTeacher_id(int teacher_id) {
        this.teacher_id = teacher_id;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
