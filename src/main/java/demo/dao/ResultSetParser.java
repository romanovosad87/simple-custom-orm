package demo.dao;

import demo.annotation.Column;
import demo.exception.MissingAnnotationException;
import lombok.SneakyThrows;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;

public class ResultSetParser {

    @SneakyThrows
    public <R> R parseToEntity(Class<R> clazz, ResultSet resultSet) {
        Constructor<R> constructor = clazz.getDeclaredConstructor();
        R entity = constructor.newInstance();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column annotation = field.getAnnotation(Column.class);
                String fieldName = annotation.name();
                Object result = resultSet.getObject(fieldName);
                field.setAccessible(true);
                field.set(entity, result);
            } else {
                throw new MissingAnnotationException(
                        String.format("Field '%s' of Entity class should have @Column annotation",
                                field.getName()));
            }

        }
        return entity;
    }
}
