package com.seckill.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();//创建RedisTemplate对象
        template.setConnectionFactory(connectionFactory);//设置连接工厂
        ObjectMapper objectMapper = new ObjectMapper();//创建ObjectMapper对象
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);//设置ObjectMapper对象
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);//设置ObjectMapper对象
        objectMapper.registerModule(new JavaTimeModule());//设置ObjectMapper对象
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);//创建GenericJackson2JsonRedisSerializer对象
        template.setKeySerializer(new StringRedisSerializer());//设置key序列化方式
        template.setValueSerializer(jsonSerializer);//设置value序列化方式
        return template;//返回RedisTemplate对象
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);//返回StringRedisTemplate对象
    }

    @Bean
    public DefaultRedisScript<Long> seckillScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();//创建DefaultRedisScript对象
        redisScript.setLocation(new ClassPathResource("seckill.lua"));//设置lua脚本位置
        redisScript.setResultType(Long.class);//设置返回类型为Long
        return redisScript;//返回DefaultRedisScript对象
    }
}
