package demo.dao;

import demo.annotation.Column;
import lombok.SneakyThrows;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;

public class ResultSetParser {
    @SneakyThrows
    public <R> R parseToEntity(Class<R> clazz, ResultSet resultSet) {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        Object entity = constructor.newInstance();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Column annotation = field.getAnnotation(Column.class);
            String fieldName = annotation.name();
            Object result = resultSet.getObject(fieldName);
            field.setAccessible(true);
            field.set(entity, result);
        }
        return clazz.cast(entity);
    }

}
