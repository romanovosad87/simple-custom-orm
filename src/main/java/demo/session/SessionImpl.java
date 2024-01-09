package demo.session;

import static demo.util.EntityUtil.getEntityKey;

import demo.dao.JdbcDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SessionImpl implements Session {
    private final Map<EntityKey, Object> entityKeyObjectMap = new HashMap<>();

    private final JdbcDao jdbcDao;
    @Override
    public <R> R getById(Class<R> clazz, Object id) {
        EntityKey entityKey = getEntityKey(clazz, id);
        Object cachedEntity = entityKeyObjectMap.get(entityKey);
        if (cachedEntity == null) {
            R loadedEntity = jdbcDao.getById(clazz, id);
            entityKeyObjectMap.put(entityKey, loadedEntity);
            log.info("Entity {} loaded from DB by id {}", clazz.getSimpleName(), entityKey);
            return loadedEntity;
        }
        log.info("Entity {} loaded from first level cache by id {}", clazz.getSimpleName(), entityKey);
        return clazz.cast(cachedEntity);
    }

    @Override
    public void close() {
        log.info("Session is closing");
    }
}
