package demo.session;

import static demo.util.EntityUtil.doDirtyChecking;
import static demo.util.EntityUtil.getEntityKey;
import static demo.util.EntityUtil.getSnapshot;

import demo.dao.JdbcDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SessionImpl implements Session {
    private final Map<EntityKey, Object> entityKeyToObject = new HashMap<>();
    private final Map<EntityKey, Object[]> snapshots = new HashMap<>();

    private final JdbcDao jdbcDao;
    @Override
    public <R> R getById(Class<R> clazz, Object id) {
        EntityKey entityKey = getEntityKey(clazz, id);
        Object cachedEntity = entityKeyToObject.get(entityKey);
        if (cachedEntity == null) {
            R loadedEntity = jdbcDao.getById(clazz, id);
            entityKeyToObject.put(entityKey, loadedEntity);
            Object[] snapshotCopy = getSnapshot(loadedEntity);
            snapshots.put(entityKey, snapshotCopy);
            log.info("Entity {} loaded from DB by id {}", clazz.getSimpleName(), entityKey);
            return loadedEntity;
        }
        log.info("Entity {} loaded from first level cache by id {}", clazz.getSimpleName(), entityKey);
        return clazz.cast(cachedEntity);
    }

    @Override
    public void flush() {
        checkEntities();
    }

    @Override
    public void close() {
        checkEntities();
        entityKeyToObject.clear();
        snapshots.clear();
        log.info("Session is closing");
    }

    private void checkEntities() {
        Map<EntityForUpdateKey, Map<String, Object>> entityKeyList
                = doDirtyChecking(entityKeyToObject, snapshots);
        if (!entityKeyList.isEmpty()) {
            jdbcDao.doUpdateOnDirtyChecking(entityKeyList);
        }
    }
}
