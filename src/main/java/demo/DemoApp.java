package demo;

import demo.dao.JdbcDao;
import demo.model.Person;

public class DemoApp {
    public static final String URL = "jdbc:postgresql://localhost:5432/postgres?currentSchema=test";
    public static final String USER = "postgres";
    public static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static void main(String[] args) {
        JdbcDao jdbcDao = new JdbcDao(URL, USER, PASSWORD);

        Person person = jdbcDao.getById(Person.class, 1L);

        System.out.println(person);
    }
}
