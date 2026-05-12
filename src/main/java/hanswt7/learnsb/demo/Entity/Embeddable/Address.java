package hanswt7.learnsb.demo.Entity.Embeddable;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    String street;
    String city;
    String district;
    String state;
    int zipcode;
    int country_code;

    @Override
    public String toString() {
        return "Address{" +
                "   " + street + '\'' +
                ", from '" + city + '\'' +
                ", in'" + district + '\'' +
                ", " + state + '\'' +
                ", " + zipcode +
                ", country_code=" + country_code +
                '}';
    }
}
