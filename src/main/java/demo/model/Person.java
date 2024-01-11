package demo.model;

import demo.annotation.Column;
import demo.annotation.Entity;
import demo.annotation.Id;
import demo.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "persons")
@ToString
@Setter
@Getter
public class Person {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "age")
    private int age;
}
