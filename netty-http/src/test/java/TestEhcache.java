import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.junit.Test;

/**
 * Created by I311352 on 4/24/2017.
 */
public class TestEhcache {

    @Test
    public void testTem() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache("preConfigured",
           CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)))
        .build();

        cacheManager.init();

        Cache<Long, String> preConfigured =
                cacheManager.getCache("preConfigured", Long.class, String.class);

        Cache<Long, String> myCache = cacheManager.createCache("myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)));

        myCache.put(1L, "da one!");
        myCache.putIfAbsent(0L, "ee");
        String value = myCache.get(1L);

        System.out.println("Value is " + value);
        cacheManager.removeCache("preConfigured");
        cacheManager.close();
    }
}
