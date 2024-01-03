package demo;

import demo.annotation.Column;
import demo.annotation.Entity;
import demo.annotation.Id;
import demo.annotation.Table;
import demo.exception.EntityNotFoundException;
import demo.exception.MissingAnnotationException;
import lombok.SneakyThrows;
import org.postgresql.ds.PGSimpleDataSource;
import org.reflections.Reflections;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;

public class DemoApp {
    public static final String URL = "jdbc:postgresql://localhost:5432/postgres?currentSchema=test";
    public static final String USER = "postgres";
    public static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static void main(String[] args) {
        Object byId = getById(1L);
        System.out.println(byId);
    }

    public static Object getById(Long id) {
        Reflections reflections = new Reflections("demo");
        Set<Class<?>> annotatedWithEntity = reflections.getTypesAnnotatedWith(Entity.class);

        return annotatedWithEntity.stream()
                .map(entity -> {
                    Pair pair = processEntity(entity, id);
                    return sendRequest(pair, id, entity);
                })
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Can't find Entity"));
    }

    public static <T> Pair processEntity(Class<?> clazz, T id) {
        String tableName;
        if (clazz.isAnnotationPresent(Table.class)) {
            Table annotation = clazz.getAnnotation(Table.class);
            tableName = annotation.name();
        } else {
            throw new MissingAnnotationException("Entity class should have Table annotation");
        }

        String idFieldName = "";
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Id.class)) {
                if (!field.getType().equals(id.getClass())) {
                    throw new MissingAnnotationException(("Parameter type '%s' do not match to the "
                            + "type of id field '%s' in Entity class")
                            .formatted(field.getType(), id.getClass()));
                }
                if (field.isAnnotationPresent(Column.class)) {
                    Column annotation = field.getAnnotation(Column.class);
                    idFieldName = annotation.name();
                } else {
                    throw new MissingAnnotationException(
                            String.format("Field '%s' of Entity class should have Column annotation",
                                    field.getName()));
                }
            }
        }
        return new Pair(tableName, idFieldName);
    }

    @SneakyThrows
    public static <T> Object sendRequest(Pair pair, T id, Class<?> clazz) {
        String selectQuery = "select * from %s where %s = ?".formatted(pair.tableName, pair.idFieldName);
        Object entity = null;
        try (var connection = getDataSource().getConnection()) {
            try (var statement = connection.prepareStatement(selectQuery)) {
                statement.setObject(1, id);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    entity = constructor.newInstance();
                    Field[] declaredFields = clazz.getDeclaredFields();
                    for (Field field : declaredFields) {
                        Column annotation = field.getAnnotation(Column.class);
                        String fieldName = annotation.name();
                        Object result = resultSet.getObject(fieldName, field.getType());
                        field.setAccessible(true);
                        field.set(entity, result);
                    }
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static DataSource getDataSource() {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setURL(URL);
        pgSimpleDataSource.setUser(USER);
        pgSimpleDataSource.setPassword(PASSWORD);
        return pgSimpleDataSource;
    }

    public record Pair(String tableName, String idFieldName) {
    }

}
