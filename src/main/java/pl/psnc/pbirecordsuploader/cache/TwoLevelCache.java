package pl.psnc.pbirecordsuploader.cache;

import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * A two-level cache implementation that combines a fast in-memory cache (L1) with a persistent distributed cache (L2).
 * This implementation attempts to read from L1 first (Caffeine), and on a miss, reads from L2 (External).
 * If found in L2, it populates L1 to speed up subsequent access.
 * <p>
 * On writes, data is written to both caches to keep them in sync.
 */
public class TwoLevelCache implements Cache {

    private final Cache caffeineCache;
    private final Cache externalCache;
    private final String name;

    /**
     * Constructs a two-level cache.
     *
     * @param name          the cache name
     * @param caffeineCache the L1 in-memory cache
     * @param externalCache    the L2 distributed cache (external)
     */
    public TwoLevelCache(String name, Cache caffeineCache, Cache externalCache) {
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.externalCache = externalCache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper value = caffeineCache.get(key);
        if (value != null) {
            return value;
        }

        value = externalCache.get(key);
        if (value != null) {
            // Warm up L1 cache
            caffeineCache.put(key, value.get());
        }

        return value;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T value = caffeineCache.get(key, type);
        if (value != null) {
            return value;
        }

        value = externalCache.get(key, type);
        if (value != null) {
            caffeineCache.put(key, value);
        }

        return value;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            // Try L1 first
            ValueWrapper cached = get(key);
            if (cached != null && cached.get() != null) {
                return (T) cached.get();
            }

            // Load from loader
            T value = valueLoader.call();

            if (value != null) {
                // Put in both L1 and L2
                caffeineCache.put(key, value);
                externalCache.put(key, value);
            }

            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (value != null) {
            caffeineCache.put(key, value);
            externalCache.put(key, value);
        }
    }

    @Override
    public void evict(Object key) {
        caffeineCache.evict(key);
        externalCache.evict(key);
    }

    @Override
    public void clear() {
        caffeineCache.clear();
        externalCache.clear();
    }
}