package pl.psnc.pbirecordsuploader.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A CacheManager that combines two levels of cache: Caffeine (L1) and external (L2).
 * For selected cache names, it returns a {@link TwoLevelCache} that first attempts
 * to resolve cache hits from Caffeine before falling back to external.
 */
public class TwoLevelCacheManager implements CacheManager {

    private final CacheManager caffeineCacheManager;
    private final CacheManager externalCacheManager;
    private final Set<String> twoLevelCacheNames;


    public TwoLevelCacheManager(@NonNull CacheManager caffeineCacheManager, @NonNull CacheManager externalCacheManager,
            @NonNull Set<String> twoLevelCacheNames) {
        this.caffeineCacheManager = Objects.requireNonNull(caffeineCacheManager);
        this.externalCacheManager = Objects.requireNonNull(externalCacheManager);
        this.twoLevelCacheNames = Objects.requireNonNull(twoLevelCacheNames);
    }

    /**
     * Returns a {@link TwoLevelCache} if the cache name is configured as two-level
     * and both underlying caches are available. Otherwise, returns either the external
     * or Caffeine cache (preferring external).
     *
     * @param name the cache name
     * @return the resolved {@link Cache}, or {@code null} if not found in either cache manager
     */
    @Override
    @Nullable
    public Cache getCache(@NonNull String name) {
        boolean isTwoLevel = twoLevelCacheNames.contains(name);
        Cache caffeineCache = caffeineCacheManager.getCache(name);
        Cache externalCache = externalCacheManager.getCache(name);

        if (isTwoLevel && caffeineCache != null && externalCache != null) {
            return new TwoLevelCache(name, caffeineCache, externalCache);
        }

        return externalCache != null ? externalCache : caffeineCache;
    }

    /**
     * Returns a union of cache names managed by both Caffeine and external managers.
     *
     * @return a collection of available cache names
     */
    @Override
    @NonNull
    public Collection<String> getCacheNames() {
        Set<String> names = new HashSet<>();
        names.addAll(caffeineCacheManager.getCacheNames());
        names.addAll(externalCacheManager.getCacheNames());
        return names;
    }
}