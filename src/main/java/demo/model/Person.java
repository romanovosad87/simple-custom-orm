package demo.model;

import demo.annotation.Column;
import demo.annotation.Entity;
import demo.annotation.Id;
import demo.annotation.Table;

@Entity
@Table(name = "persons")
public class Person {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "age")
    private Integer age;

    @Override
    public String toString() {
        return "Person{"
                + "id=" + id
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", age=" + age
                + '}';
    }
}
