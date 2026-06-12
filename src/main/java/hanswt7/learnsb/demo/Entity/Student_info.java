package hanswt7.learnsb.demo.Entity;
import hanswt7.learnsb.demo.Entity.Embeddable.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.Date;
@Entity
public class Student_info {
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private int roll_no;
    private boolean gender;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(unique = true, nullable = false)
    private long phone_number;
    private int age;
    @Transient
    private final String ROLE = "Student";

    public String getROLE() {
        return ROLE;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    private Date date_of_birth;
    private enum category {Gen,ST,SC,OBC}
    private category category;
    @Embedded
    private Address address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(int roll_no) {
        this.roll_no = roll_no;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public Date getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(Date date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public category getCategory() {
        return category;
    }

    public void setCategory(category category) {
        this.category = category;
    }
//    private String gender_value {
//
//    }
    @Override
    public String toString() {
        return "Student_info{" +
                "name='" + name + '\'' +
                ", roll_no=" + roll_no +
                ", gender=" + gender +
                ", date_of_birth=" + date_of_birth +
                ", category=" + category +
                ", address=" + address +
                ", email='" + email + '\'' +
                ", phone_number=" + phone_number +
                ", age=" + age +
                '}';
    }
}
