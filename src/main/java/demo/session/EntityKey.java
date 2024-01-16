package demo.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.lang.reflect.Type;
import java.util.Objects;

@RequiredArgsConstructor
@Setter
@Getter
public class EntityKey {
    private final Class<?> entityClass;
    private final String entityName;
    private final String entityKeyName;
    private final Object key;
    private final Type keyType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityKey entityKey)) return false;
        return Objects.equals(entityClass, entityKey.entityClass)
                && Objects.equals(entityName, entityKey.entityName)
                && Objects.equals(entityKeyName, entityKey.entityKeyName)
                && Objects.equals(key, entityKey.key) && Objects.equals(keyType, entityKey.keyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityClass, entityName, entityKeyName, key, keyType);
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
