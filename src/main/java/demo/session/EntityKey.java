package demo.session;

import lombok.RequiredArgsConstructor;
import java.lang.reflect.Type;
import java.util.Objects;

@RequiredArgsConstructor
public class EntityKey<T> {
    private final String entityName;
    private final String entityKeyName;
    private final T key;
    private final Type keyType;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !o.getClass().equals(this.getClass())) {
            return false;
        }
        EntityKey<?> entityKey = (EntityKey<?>) o;
        return Objects.equals(entityName, entityKey.entityName)
                && Objects.equals(entityKeyName, entityKey.entityKeyName)
                && Objects.equals(keyType, entityKey.keyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityName, entityKeyName, keyType);
    }

    @Override
    public String toString() {
        return "EntityKey {"
                + "entityName='" + entityName + '\''
                + ", entityKeyName='" + entityKeyName + '\''
                + ", keyType=" + keyType
                + '}';
    }
}
