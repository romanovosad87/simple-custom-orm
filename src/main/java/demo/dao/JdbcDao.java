package demo.dao;

import demo.exception.ConnectionFailedException;
import demo.exception.EntityNotFoundException;
import demo.util.EntityUtil;
import demo.util.Pair;
import lombok.SneakyThrows;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcDao {
    public static final String SELECT_QUERY_TEMPLATE = "select * from %s where %s = ?";
    private final String url;
    private final String user;
    private final String password;
    private final ResultSetParser resultSetParser = new ResultSetParser();

    public JdbcDao(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public <R, T> R getById(Class<R> clazz, T id) {
        Pair pair = EntityUtil.processEntity(clazz, id);
        R result = sendRequest(pair, id, clazz);
        if (result == null) {
            throw new EntityNotFoundException("Can't find entity of class '%s' by id '%s'"
                    .formatted(clazz.getSimpleName(), id));
        }
        return result;
    }

    public <T, R> R sendRequest(Pair pair, T id, Class<R> clazz) {
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
}
