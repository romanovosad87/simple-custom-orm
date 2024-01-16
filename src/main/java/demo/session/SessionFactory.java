package demo.session;

import demo.dao.JdbcDao;
import lombok.extern.slf4j.Slf4j;
import java.io.Closeable;

@Slf4j
public class SessionFactory implements Closeable {

    private final JdbcDao jdbcDao;

    public SessionFactory(JdbcDao jdbcDao) {
        this.jdbcDao = jdbcDao;
    }

    public Session getSession() {
        return new SessionImpl(jdbcDao);
    }

    @Override
    public void close() {
        jdbcDao.close();
        log.info("SessionFactory is closing");
    }
}
