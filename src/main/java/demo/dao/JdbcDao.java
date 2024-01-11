package demo.dao;

import demo.exception.ConnectionFailedException;
import demo.exception.EntityNotFoundException;
import demo.exception.FailingReadPropertiesException;
import demo.exception.JdbcDaoException;
import demo.session.EntityForUpdateKey;
import demo.util.EntityUtil;
import demo.util.EntityMetadata;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
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
    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String UPDATE_QUERY_TEMPLATE = "UPDATE %s SET %s WHERE id=?";
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

    public void doUpdateOnDirtyChecking(Map<EntityForUpdateKey, Map<String, Object>> entitiesForUpdate) {

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
                        statement.setObject(i+1, list.get(i));
                    }
                    statement.setObject(list.size() + 1, id);
                    statement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new JdbcDaoException("Can't send update request to DB on dirty checking", e);

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
