package ignite.beginner;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data grid is an abstraction of distributed cache as a data store
 */
public class InMemoryDataGrids101 {
    private static final Logger logger = LogManager.getLogger(InMemoryDataGrids101.class);
    private static Map<Key, Value> dummyData = getDummyDataMap(10);

    private InMemoryDataGrids101() {}

    public static Map<Key, Value> getDummyDataMap(int numOfItems) {
        Map<Key, Value> dummyDataMap = new HashMap<>(numOfItems);
        for (int i = 0; i < numOfItems; i++) {
            dummyDataMap.put(new Key(i), new Value(String.format("Value %s", i)));
        }
        return dummyDataMap;
    }

    /**
     * Adheres to JCache spec JSR 107
     * - CachingProvider: Defines the API to create, manage and configure CacheManagers
     * - CacheManager: Defines APIs to create, manage and configure "Cache"s
     * - Cache: Stores key-value pairs
     * - Entry: Single key-value pair stored in a cache
     * - ExpiryPolicy: Each cache "Entry" has a time to live. During this time, you can access, update, or remove the entry, but after that, the entry expires. The ExpiryPolicy defines when an Entry will expire.
     */
    public static class JCacheBasedDataGrid {
        private JCacheBasedDataGrid() {}

        public static void runReplicatedAtomicLRUCacheServerNode() throws Exception {
            final String cacheName = "ReplicatedAtomicOnHeapLRUCache";
            new IgniteNode()
                    .with(IgniteHelperFactory
                            .igniteConfigWithLog4j2("ReplicatedAtomicLRUCacheServerNode", false, true)
                            .setCacheConfiguration(getReplicatedAtomicOnHeapLRUCacheConfig(cacheName)))
                    .execute(ignite -> {
                        IgniteCache<Key, Value> cache = ignite.getOrCreateCache(cacheName);
                        cache.putAll(Map.copyOf(getDummyDataMap(10)));
                        for (int i = 0; i < 10; i++) {
                            logger.log(Level.INFO, String.format("Key: %d, Value: %s", i, cache.get(new Key(i))));
                        }
                        Thread.sleep(999999999);
                    });
        }

        private static CacheConfiguration getReplicatedAtomicOnHeapLRUCacheConfig(String cacheName) {
            CacheConfiguration<Key, Value> cacheConfig = new CacheConfiguration<>();
            cacheConfig.setName(cacheName)
                    .setCacheMode(CacheMode.REPLICATED)
                    .setAtomicityMode(CacheAtomicityMode.ATOMIC)
                    .setOnheapCacheEnabled(true)
                    .setEvictionPolicyFactory(new LruEvictionPolicyFactory<Key, Value>(8));
            return cacheConfig;
        }
    }

    public static class Key implements Serializable {
        private Integer key;

        public Key(Integer key) {
            super();
            this.key = key;
        }

        public Integer getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key1 = (Key) o;
            return key.equals(key1.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

    public static class Value implements Serializable {
        private String value;

        public Value(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Value value1 = (Value) o;
            return Objects.equals(value, value1.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return String.format("Value - value: %s", value);
        }
    }
}
