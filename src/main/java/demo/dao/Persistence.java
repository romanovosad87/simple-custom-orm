package demo.dao;

import demo.session.SessionFactory;
import java.util.Properties;

public class Persistence {
    private final JdbcDao jdbcDao;

    public Persistence() {
        jdbcDao = new JdbcDao();
    }

    public Persistence(Properties properties) {
        jdbcDao = new JdbcDao(properties);
    }

    public SessionFactory getSessionFactory() {
        return new SessionFactory(jdbcDao);
    }
}
