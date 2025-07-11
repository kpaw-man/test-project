package pl.psnc.pbirecordsuploader.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import pl.psnc.pbirecordsuploader.cache.TwoLevelCacheManager;

import java.time.Duration;
import java.util.Set;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.internal.max-size}")
    private int caffeineCacheSize;

    @Value("${cache.internal.expire-after-write-minute}")
    private int caffeineExpireMinutes;

    @Value("${cache.external.ttl-hours}")
    private long externalTtlHours;

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(caffeineCacheSize)
                .expireAfterWrite(Duration.ofMinutes(caffeineExpireMinutes)));
        return manager;
    }

    @Bean
    public CacheManager externalCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(externalTtlHours))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(factory).cacheDefaults(config).build();
    }

    @Bean
    @Primary
    public CacheManager twoLevelCacheManager(
            CacheManager caffeineCacheManager,
            CacheManager externalCacheManager) {

        Set<String> multiLevelCaches = Set.of("bnDescriptor","semanticAnalyzer","researchAreaSanitizer");
        return new TwoLevelCacheManager(caffeineCacheManager, externalCacheManager, multiLevelCaches);
    }
}