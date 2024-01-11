package demo.util;

import demo.annotation.Column;
import demo.annotation.Entity;
import demo.annotation.Id;
import demo.annotation.Table;
import demo.exception.MissingAnnotationException;
import demo.session.EntityForUpdateKey;
import demo.session.EntityKey;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
@Slf4j
public class EntityUtil {

    @SneakyThrows
    public static <R> Object[] getSnapshot(R entity) {
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        Object[] snapshots = new Object[declaredFields.length];
        for (int i = 0; i < declaredFields.length; i++) {
            declaredFields[i].setAccessible(true);
            snapshots[i] = declaredFields[i].get(entity);
        }
        return snapshots;
    }

    @SneakyThrows
    public static Map<EntityForUpdateKey, Map<String, Object>> doDirtyChecking(Map<EntityKey, Object> entityKeyToObject,
                                                               Map<EntityKey, Object[]>snapshots) {
        Map<EntityForUpdateKey, Map<String, Object>> entitiesForUpdate = new HashMap<>();
        for (Map.Entry<EntityKey, Object> entry : entityKeyToObject.entrySet()) {
            EntityKey key = entry.getKey();
            Object entity = entry.getValue();
            Class<?> entityClass = entity.getClass();
            String tableName;
            if (entityClass.isAnnotationPresent(Table.class)) {
                Table annotation = entityClass.getAnnotation(Table.class);
                tableName = annotation.name();
            } else {
                tableName = entityClass.getSimpleName().toLowerCase();
            }
            Object[] snapshot = snapshots.get(key);
            Field[] declaredFields = entity.getClass().getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                declaredFields[i].setAccessible(true);
                Object fieldValue = declaredFields[i].get(entity);
                if (!fieldValue.equals(snapshot[i])) {
                    String fieldName;
                    if (declaredFields[i].isAnnotationPresent(Column.class)) {
                        Column annotation = declaredFields[i].getAnnotation(Column.class);
                        fieldName = annotation.name();
                    } else {
                        fieldName = declaredFields[i].getName().toLowerCase();
                    }
                    EntityForUpdateKey entityForUpdateKey = new EntityForUpdateKey(key.getKey(), tableName);

                    entitiesForUpdate.computeIfAbsent(entityForUpdateKey, v -> new HashMap<>())
                            .put(fieldName, fieldValue);
                }
            }
        }
        return entitiesForUpdate;
    }



    public static EntityKey getEntityKey(Class<?> clazz, Object id) {
        String entityName = clazz.getSimpleName();
        String keyName;
        Type keyType;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Id.class)) {
                checkIfParameterIdTypeCorrespondsEntityIdType(id, field);
                keyName = field.getName();
                keyType = field.getType();
                return new EntityKey(clazz, entityName, keyName, id, keyType);
            }
        }
        throw new MissingAnnotationException("The class '%s' doesn't have field annotated with @Id annotation"
                .formatted(clazz.getSimpleName()));
    }


    public static EntityMetadata createEntityMetadata(Class<?> clazz) {
        checkIfClassHasEntityAnnotation(clazz);

        String tableName = getTableName(clazz);
        String idFieldName = getIdFieldName(clazz);
        return new EntityMetadata(tableName, idFieldName);
    }

    private static <T> void checkIfParameterIdTypeCorrespondsEntityIdType(T id, Field field) {
        if (!field.getType().equals(id.getClass())) {
            throw new MissingAnnotationException(("Parameter type '%s' do not match to the "
                    + "type of id field '%s' in Entity class")
                    .formatted(id.getClass().getSimpleName(), field.getType().getSimpleName()));
        }
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
