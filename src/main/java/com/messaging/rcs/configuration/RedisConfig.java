package com.messaging.rcs.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messaging.rcs.domain.BlackListEntity;
import com.messaging.rcs.domain.CampaignEntity;
import com.messaging.rcs.domain.SMSUrlEntity;
import com.messaging.rcs.domain.UserEntity;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String redisHostName;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Autowired
	ObjectMapper objectMapper;

	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHostName,
				redisPort);
		// redisStandaloneConfiguration.setPassword(RedisPassword.of("yourRedisPasswordIfAny"));
		return new JedisConnectionFactory(redisStandaloneConfiguration);
	}

	@Bean
	RedisTemplate<String, BlackListEntity> blacklistTemplate() {
		final RedisTemplate<String, BlackListEntity> redisTemplate = new RedisTemplate<>();
		Jackson2JsonRedisSerializer<BlackListEntity> valueSerializer = new Jackson2JsonRedisSerializer<BlackListEntity>(
				BlackListEntity.class);
		valueSerializer.setObjectMapper(objectMapper);
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(valueSerializer);
		return redisTemplate;
	}
	
	@Bean
	public RedisTemplate<String, Object> template() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new JdkSerializationRedisSerializer());
		template.setValueSerializer(new JdkSerializationRedisSerializer());
		template.setEnableTransactionSupport(true);
		// template.
		template.afterPropertiesSet();

		return template;
	}

	/*
	 * @Bean public RedisCacheManager redisCacheManager(RedisConnectionFactory
	 * redisConnectionFactory) { //初始化一个redisCacheWriter RedisCacheWriter
	 * redisCacheWriter =
	 * RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
	 * //初始化序列化方式为json RedisSerializer redisSerializer = new
	 * GenericJackson2JsonRedisSerializer();
	 * RedisSerializationContext.SerializationPair pair =
	 * RedisSerializationContext.SerializationPair .fromSerializer(redisSerializer);
	 * RedisCacheConfiguration redisCacheConfiguration =
	 * RedisCacheConfiguration.defaultCacheConfig() .serializeValuesWith(pair);
	 * 
	 * return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration); }
	 */

	@Bean
	RedisTemplate<String, UserEntity> userEntitySendToRedis() {
		final RedisTemplate<String, UserEntity> redisTemplate = new RedisTemplate<>();
		Jackson2JsonRedisSerializer<UserEntity> valueSerializer = new Jackson2JsonRedisSerializer<UserEntity>(
				UserEntity.class);
		valueSerializer.setObjectMapper(objectMapper);
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(valueSerializer);

		return redisTemplate;
	}

	@Bean
	RedisTemplate<String, CampaignEntity> campaignEntitiySendToRedis() {
		final RedisTemplate<String, CampaignEntity> redisTemplate = new RedisTemplate<>();
		Jackson2JsonRedisSerializer<CampaignEntity> valueSerializer = new Jackson2JsonRedisSerializer<CampaignEntity>(
				CampaignEntity.class);
		valueSerializer.setObjectMapper(objectMapper);
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(valueSerializer);
		return redisTemplate;
	}

	@Bean
	RedisTemplate<String, SMSUrlEntity> smsEntitySendToRedis() {
		final RedisTemplate<String, SMSUrlEntity> redisTemplate = new RedisTemplate<>();
		Jackson2JsonRedisSerializer<SMSUrlEntity> valueSerializer = new Jackson2JsonRedisSerializer<SMSUrlEntity>(
				SMSUrlEntity.class);
		valueSerializer.setObjectMapper(objectMapper);
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(valueSerializer);

		return redisTemplate;
	}
	
	
}