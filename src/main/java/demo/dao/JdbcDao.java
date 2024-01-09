package demo.dao;

import demo.exception.ConnectionFailedException;
import demo.exception.EntityNotFoundException;
import demo.exception.FailingReadPropertiesException;
import demo.util.EntityUtil;
import demo.util.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcDao {
    public static final String SELECT_QUERY_TEMPLATE = "select * from %s where %s = ?";
    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    private final String url;
    private final String user;
    private final String password;
    private final ResultSetParser resultSetParser = new ResultSetParser();

    public JdbcDao() {
        Properties properties = getProperties();
        this.url = properties.getProperty(DB_URL);
        this.user = properties.getProperty(DB_USER);
        this.password = properties.getProperty(DB_PASSWORD);
    }

    public <R> R getById(Class<R> clazz, Object id) {
        Pair pair = EntityUtil.processEntity(clazz);
        R result = sendRequest(pair, clazz, id);
        if (result == null) {
            throw new EntityNotFoundException("Can't find entity of class '%s' by id '%s'"
                    .formatted(clazz.getSimpleName(), id));
        }
        return result;
    }

    public <R> R sendRequest(Pair pair, Class<R> clazz, Object id) {
        String selectQuery = SELECT_QUERY_TEMPLATE.formatted(pair.tableName(), pair.idFieldName());
        Object entity = null;
        try (var connection = getConnection()) {
            try (var statement = connection.prepareStatement(selectQuery)) {
                statement.setObject(1, id);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    entity = resultSetParser.parseToEntity(clazz, resultSet);
                }
            }
            return clazz.cast(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() {
        Connection connection;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new ConnectionFailedException("Can't get connection", e);
        }
        return connection;
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        InputStream stream = JdbcDao.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new FailingReadPropertiesException("Can't read properties from file: '%s'"
                    .formatted(APPLICATION_PROPERTIES), e);
        }
        return properties;
    }
}
