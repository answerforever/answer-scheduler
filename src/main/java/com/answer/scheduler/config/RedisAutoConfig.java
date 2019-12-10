package com.answer.scheduler.config;


import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.answer.scheduler.serializer.DefaultStrSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * <p>
 * Redis 的配置
 * </p>
 *
 * @author SunmeSpace
 * @since 2019-07-23
 */

@Configuration
@Slf4j
public class RedisAutoConfig {
    /**
     * 配置fastjson redis的反序列化autotype
     */
    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        ParserConfig.getGlobalInstance().addAccept("com.answer.");
        //ipage
        //ParserConfig.getGlobalInstance().addAccept("com.baomidou.mybatisplus.");
    }

    @Value("${spring.redis.timeout:6000}")
    private long timeOut;

    @Bean("defaultLettuceConnectionFactory")
    public LettuceConnectionFactory defaultLettuceConnectionFactory(@Qualifier("defaultRedisConfig") RedisStandaloneConfiguration defaultRedisConfig,
                                                                    @Qualifier("defaultPoolConfig") GenericObjectPoolConfig defaultPoolConfig) {
        LettuceClientConfiguration clientConfig =
                LettucePoolingClientConfiguration.builder().commandTimeout(Duration.ofMillis(timeOut))
                        .poolConfig(defaultPoolConfig).build();
        return new LettuceConnectionFactory(defaultRedisConfig, clientConfig);
    }

    /**
     * redis utils 依赖此bean,所以这里不能修改,如果有需要,可以重现定义新的bean
     *
     * @param factory
     * @return
     */
    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("defaultLettuceConnectionFactory") LettuceConnectionFactory factory) {
        // 关闭共享链接
        factory.setShareNativeConnection(false);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        DefaultStrSerializer stringRedisSerializer = new DefaultStrSerializer();
        // 使用FastJsonRedisSerializer来序列化和反序列化redis的value值
        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 解决注解方式存放到redis中的值是乱码的情况
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(@Qualifier("defaultLettuceConnectionFactory") LettuceConnectionFactory factory) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();

        // 配置注解方式的序列化
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        RedisCacheConfiguration redisCacheConfiguration =
                config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(fastJsonRedisSerializer))
                        //配置注解默认的过期时间
                        .entryTtl(Duration.ofDays(1));
        return RedisCacheManager.builder(factory).cacheDefaults(redisCacheConfiguration).build();
    }

    @Configuration
    public static class DefaultRedisConfig {
        @Value("${spring.redis.host:127.0.0.1}")
        private String host;
        @Value("${spring.redis.port:6379}")
        private Integer port;
        @Value("${spring.redis.password:}")
        private String password;
        @Value("${spring.redis.database:0}")
        private Integer database;

        @Value("${spring.redis.lettuce.pool.max-active:8}")
        private Integer maxActive;
        @Value("${spring.redis.lettuce.pool.max-idle:8}")
        private Integer maxIdle;
        @Value("${spring.redis.lettuce.pool.max-wait:-1}")
        private Long maxWait;
        @Value("${spring.redis.lettuce.pool.min-idle:0}")
        private Integer minIdle;

        @Bean("defaultPoolConfig")
        public GenericObjectPoolConfig defaultPoolConfig() {
            log.info("===============> staring config redispool.......");
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(maxActive);
            config.setMaxIdle(maxIdle);
            config.setMinIdle(minIdle);
            config.setMaxWaitMillis(maxWait);
            log.info("===============> staring config redispool ok ::::::");
            return config;
        }

        @Bean("defaultRedisConfig")
        public RedisStandaloneConfiguration defaultRedisConfig() {
            log.info("===============> staring config redis.......");
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(host);
            config.setPassword(RedisPassword.of(password));
            config.setPort(port);
            config.setDatabase(database);
            log.info("===============> staring config redis ok ::::::");
            return config;
        }
    }

//    @Bean
//    public RedisTemplate<String, String> defaultRedisTemplate(
//            LettuceConnectionFactory defaultLettuceConnectionFactory) {
//        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(defaultLettuceConnectionFactory);
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }

//    @Bean
//    @ConditionalOnBean(name = "localRedisConfig")
//    public LettuceConnectionFactory localLettuceConnectionFactory(RedisStandaloneConfiguration localRedisConfig,
//            GenericObjectPoolConfig localPoolConfig) {
//        LettuceClientConfiguration clientConfig =
//                LettucePoolingClientConfiguration.builder().commandTimeout(Duration.ofMillis(100))
//                        .poolConfig(localPoolConfig).build();
//        return new LettuceConnectionFactory(localRedisConfig, clientConfig);
//    }
//
//    @Bean
//    @ConditionalOnBean(name = "localLettuceConnectionFactory")
//    public RedisTemplate<String, String> localRedisTemplate(LettuceConnectionFactory localLettuceConnectionFactory) {
//        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(localLettuceConnectionFactory);
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }

//    @Configuration
//    @ConditionalOnProperty(name = "host", prefix = "spring.redis")
//    public static class LocalRedisConfig {
//        @Value("${spring.redis.host:127.0.0.1}")
//        private String host;
//        @Value("${spring.redis.port:6379}")
//        private Integer port;
//        @Value("${spring.redis.password:}")
//        private String password;
//        @Value("${spring.redis.database:0}")
//        private Integer database;
//
//        @Value("${spring.redis.lettuce.pool.max-active:8}")
//        private Integer maxActive;
//        @Value("${spring.redis.lettuce.pool.max-idle:8}")
//        private Integer maxIdle;
//        @Value("${spring.redis.lettuce.pool.max-wait:-1}")
//        private Long maxWait;
//        @Value("${spring.redis.lettuce.pool.min-idle:0}")
//        private Integer minIdle;
//
//        @Bean
//        public GenericObjectPoolConfig localPoolConfig() {
//            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
//            config.setMaxTotal(maxActive);
//            config.setMaxIdle(maxIdle);
//            config.setMinIdle(minIdle);
//            config.setMaxWaitMillis(maxWait);
//            return config;
//        }
//
//        @Bean
//        public RedisStandaloneConfiguration localRedisConfig() {
//            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
//            config.setHostName(host);
//            config.setPassword(RedisPassword.of(password));
//            config.setPort(port);
//            config.setDatabase(database);
//            return config;
//        }
//    }
}