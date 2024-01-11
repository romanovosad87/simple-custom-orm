package demo;

import demo.dao.JdbcDao;
import demo.model.Person;
import demo.session.Session;
import demo.session.SessionFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoApp {

    @SneakyThrows
    public static void main(String[] args) {
        JdbcDao jdbcDao = new JdbcDao();
        SessionFactory sessionFactory = new SessionFactory(jdbcDao);
        try (Session session = sessionFactory.getSession()) {
            Person martin = session.getById(Person.class, 1L);
            Person joshua = session.getById(Person.class, 2L);
            log.info("Person from DB: {}", martin);
            log.info("Person from DB: {}", joshua);

            martin.setLastName("Fowler Super");
            joshua.setAge(60);
        }
        sessionFactory.close();
    }
}
