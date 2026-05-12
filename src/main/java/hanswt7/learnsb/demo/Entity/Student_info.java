package hanswt7.learnsb.demo.Entity;
import hanswt7.learnsb.demo.Entity.Embeddable.Address;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

import java.util.Date;
@Entity
public class Student_info {
    private String name;
    private int roll_no;
    private boolean gender;
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
                '}';
    }
}
