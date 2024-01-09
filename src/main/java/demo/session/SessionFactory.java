package demo.session;

import demo.dao.JdbcDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.Closeable;

@Slf4j
@RequiredArgsConstructor
public class SessionFactory implements Closeable {

    private final JdbcDao jdbcDao;

    public Session getSession() {
        return new SessionImpl(jdbcDao);
    }

    @Override
    public void close() {
        log.info("SessionFactory is closing");
    }
}
