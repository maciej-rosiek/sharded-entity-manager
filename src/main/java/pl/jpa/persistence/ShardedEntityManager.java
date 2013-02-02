package pl.jpa.persistence;

import com.google.common.base.Preconditions;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.util.List;

public class ShardedEntityManager {

    private static final ShardingStrategyImpl DEFAULT_SHARDING_STRATEGY = new DefaultShardingStrategy();

    public ShardedEntityManager(List<EntityManagerFactory> entityManagerFactories) {
        Preconditions.checkNotNull(entityManagerFactories);
        Preconditions.checkArgument(entityManagerFactories.size() > 0);
        this.entityManagerFactories = entityManagerFactories;
    }

    private final List<EntityManagerFactory> entityManagerFactories;

    public <T> T find(Class<T> entityClass, Object shardedPrimaryKey) {
        return find(entityClass, shardedPrimaryKey, shardedPrimaryKey);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Object shardKey) {
        EntityManagerFactory emf = findShard(entityClass, shardKey);
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(entityClass, primaryKey);
        }
        finally {
            em.close();
        }
    }

    public <T> T createOrUpdate(final T entity) {
        EntityManagerFactory emf = findShard(entity);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        T merged;
        try {
            merged = em.merge(entity);
            em.getTransaction().commit();
        }
        catch (PersistenceException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        }
        finally {
            em.close();
        }
        return merged;
    }

    private EntityManagerFactory findShard(Object entity) {
        Class<?> entityClass = entity.getClass();
        Object shardKey = null;
        if (entity instanceof ShardKeyProvider) {
            shardKey = ((ShardKeyProvider) entity).getShardKey();
        }
        else {
            Class<?> targetClass = entityClass;
            do {
                Field[] fields = targetClass.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(ShardKey.class)) {
                        try {
                            field.setAccessible(true);
                            shardKey = field.get(entity);
                            break;
                        } catch (IllegalAccessException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
                targetClass = targetClass.getSuperclass();
            }
            while (targetClass != null && targetClass != Object.class);
        }

        if (shardKey == null) {
            throw new IllegalArgumentException(String.format("Shard key not found for object: %s", entity));
        }

        return findShard(entityClass, shardKey);
    }

    private EntityManagerFactory findShard(Class<?> entityClass, Object shardKey) {
        int shardIndex = getShardIndex(entityClass, shardKey);

        if (shardIndex >= entityManagerFactories.size()) {
            throw new IllegalStateException(String.format("Could not find entityManagerFactory for index: %s, shardKey: %s, entityClass: %s", shardIndex, shardKey, entityClass));
        }

        return entityManagerFactories.get(shardIndex);
    }

    @SuppressWarnings("unchecked")
    private int getShardIndex(Class<?> entityClass, Object shardKey) {
        if (entityClass.isAnnotationPresent(ShardingStrategy.class)) {
            ShardingStrategy shardingStrategy = entityClass.getAnnotation(ShardingStrategy.class);
            Class<? extends ShardingStrategyImpl> shardingStrategyClass = shardingStrategy.value();
            try {
                return shardingStrategyClass.newInstance().getShardIndex(shardKey);
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        return DEFAULT_SHARDING_STRATEGY.getShardIndex(shardKey);
    }

}
