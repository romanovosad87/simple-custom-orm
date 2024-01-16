package demo.dao;

import static demo.util.PropertyParser.processProperty;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import demo.exception.ConnectionFailedException;
import demo.exception.EntityNotFoundException;
import demo.exception.FailingReadPropertiesException;
import demo.exception.JdbcDaoException;
import demo.session.EntityForUpdateKey;
import demo.util.EntityMetadata;
import demo.util.EntityUtil;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class JdbcDao {
    public static final String SELECT_QUERY_TEMPLATE = "select * from %s where %s = ?";
    public static final String APPLICATION_PROPERTIES = "persistence.properties";
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String UPDATE_QUERY_TEMPLATE = "UPDATE %s SET %s WHERE id=?";
    private String url;
    private String user;
    private String password;
    private HikariConfig config;
    private HikariDataSource hikariDataSource;
    private final ResultSetParser resultSetParser = new ResultSetParser();

    public JdbcDao() {
        initDatabase();
        config = new HikariConfig();
    }

    public JdbcDao(Properties properties) {
        initDatabase(properties);
    }

    public <R> R getById(Class<R> clazz, Object id) {
        EntityMetadata entityMetadata = EntityUtil.createEntityMetadata(clazz);
        R result = sendRequest(entityMetadata, clazz, id);
        if (result == null) {
            throw new EntityNotFoundException("Can't find entity of class '%s' by id '%s'"
                    .formatted(clazz.getSimpleName(), id));
        }
        return result;
    }

    public <R> R sendRequest(EntityMetadata entityMetadata, Class<R> clazz, Object id) {
        String selectQuery = SELECT_QUERY_TEMPLATE.formatted(entityMetadata.tableName(), entityMetadata.idFieldName());
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
            throw new JdbcDaoException("Can't send select request '%s' to DB".formatted(selectQuery), e);
        }
    }

    public void doUpdateOnDirtyChecking(
            Map<EntityForUpdateKey, Map<String, Object>> entitiesForUpdate) {

        try (var connection = getConnection()) {
            for (Map.Entry<EntityForUpdateKey, Map<String, Object>> entry : entitiesForUpdate.entrySet()) {
                EntityForUpdateKey key = entry.getKey();
                String tableName = key.tableName();
                Object id = key.id();
                String fields = entry.getValue().keySet()
                        .stream()
                        .map(field -> field + "=?")
                        .collect(Collectors.joining(", "));


                Collection<Object> values = entry.getValue().values();
                List<Object> list = new ArrayList<>(values);


                String query = UPDATE_QUERY_TEMPLATE.formatted(tableName, fields);
                log.info(query);
                try (var statement = connection.prepareStatement(query)) {
                    for (int i = 0; i < list.size(); i++) {
                        statement.setObject(i + 1, list.get(i));
                    }
                    statement.setObject(list.size() + 1, id);
                    statement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new JdbcDaoException("Can't send update request to DB on dirty checking", e);

        }
    }

    public void close() {
        hikariDataSource.close();
    }

    private Connection getConnection() {
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        hikariDataSource = new HikariDataSource(config);
        Connection connection;
        try {
            connection = hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionFailedException("Can't get connection", e);
        }
        return connection;
    }

    private void initDatabase() {
        Properties properties = getProperties();
        processProperties(properties);
    }

    private void initDatabase(Properties properties) {
        processProperties(properties);
    }

    private void processProperties(Properties properties) {
        String urlProperty = properties.getProperty(DB_URL);
        String userProperty = properties.getProperty(DB_USER);
        String passwordProperty = properties.getProperty(DB_PASSWORD);
        url = processProperty(urlProperty);
        user = processProperty(userProperty);
        password = processProperty(passwordProperty);
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
