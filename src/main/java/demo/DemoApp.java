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
            Person person1 = session.getById(Person.class, 1L);
            Person person2 = session.getById(Person.class, 1f);
            log.info("Entity from first call of find by id: {}", person1);
            log.info("Entity from second call of find by id: {}", person2);
            log.info("{}", person1 == person2);
        }
        sessionFactory.close();
    }
}
