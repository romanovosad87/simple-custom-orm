package demo.util;

import demo.annotation.Column;
import demo.annotation.Entity;
import demo.annotation.Id;
import demo.annotation.Table;
import demo.exception.MissingAnnotationException;
import demo.session.EntityKey;
import lombok.experimental.UtilityClass;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

@UtilityClass
public class EntityUtil {

    public static <T> EntityKey<T> getEntityKey(Class<?> clazz, T id) {
        String entityName = clazz.getSimpleName();
        String keyName;
        Type keyType;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Id.class)) {
                keyName = field.getName();
                keyType = field.getType();
                return new EntityKey<>(entityName, keyName, id, keyType);
            }
        }
        throw new MissingAnnotationException("The class '%s' doesn't have field annotated with @Id annotation"
                .formatted(clazz.getSimpleName()));
    }


    public static Pair processEntity (Class<?> clazz) {
        checkIfClassHasEntityAnnotation(clazz);

        String tableName = getTableName(clazz);
        String idFieldName = getIdFieldName(clazz);
        return new Pair(tableName, idFieldName);
    }

    private static void checkIfClassHasEntityAnnotation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
           throw new MissingAnnotationException("The class '%s' is not annotated with Entity annotation"
                   .formatted(clazz.getSimpleName()));
        }
    }

    private static String getIdFieldName(Class<?> clazz) {
        String idFieldName = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Id.class)) {
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
