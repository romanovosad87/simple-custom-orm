package demo.util;

import demo.annotation.Column;
import demo.annotation.Entity;
import demo.annotation.Id;
import demo.annotation.Table;
import demo.exception.MissingAnnotationException;
import lombok.experimental.UtilityClass;
import java.lang.reflect.Field;

@UtilityClass
public class EntityUtil {


    public static  <T> Pair processEntity (Class<?> clazz, T id) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
           throw new MissingAnnotationException("The class '%s' is not annotated with Entity annotation"
                   .formatted(clazz.getSimpleName()));
        }

        String tableName = getTableName(clazz);
        String idFieldName = getIdFieldName(clazz, id);
        return new Pair(tableName, idFieldName);
    }

    private static  <T> String getIdFieldName(Class<?> clazz, T id) {
        String idFieldName = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Id.class)) {
                checkIfParameterIdTypeCorrespondsEntityIdType(id, field);
                if (field.isAnnotationPresent(Column.class)) {
                    Column annotation = field.getAnnotation(Column.class);
                    idFieldName = annotation.name();
                } else {
                    throw new MissingAnnotationException(
                            String.format("Field '%s' of Entity class should have @Column annotation",
                                    field.getName()));
                }
            }
        }
        if (idFieldName == null) {
            throw new MissingAnnotationException("The class '%s' doesn't have field annotated with @Id annotation"
                    .formatted(clazz.getSimpleName()));
        }
        return idFieldName;
    }

    private static <T> void checkIfParameterIdTypeCorrespondsEntityIdType(T id, Field field) {
        if (!field.getType().equals(id.getClass())) {
            throw new MissingAnnotationException(("Parameter type '%s' do not match to the "
                    + "type of id field '%s' in Entity class")
                    .formatted(id.getClass().getSimpleName(), field.getType().getSimpleName()));
        }
    }

    private static String getTableName(Class<?> clazz) {
        String tableName;
        if (clazz.isAnnotationPresent(Table.class)) {
            Table annotation = clazz.getAnnotation(Table.class);
            tableName = annotation.name();
        } else {
            throw new MissingAnnotationException("Entity class should have @Table annotation");
        }
        return tableName;
    }

}
