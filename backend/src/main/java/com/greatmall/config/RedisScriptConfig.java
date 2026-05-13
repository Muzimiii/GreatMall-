package com.greatmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisScriptConfig {

    @Bean("seckillDeductScript")
    public DefaultRedisScript<Long> seckillDeductScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill_deduct.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean("seckillCompensateScript")
    public DefaultRedisScript<Long> seckillCompensateScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill_compensate.lua"));
        script.setResultType(Long.class);
        return script;
    }
}

