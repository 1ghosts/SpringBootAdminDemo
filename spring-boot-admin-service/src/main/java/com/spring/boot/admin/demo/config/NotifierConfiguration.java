package com.spring.boot.admin.demo.config;

import com.spring.boot.admin.demo.alarm.JvmAlarm;
import com.spring.boot.admin.demo.message.AlarmMessage;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chensihong
 * @version 1.0
 * @date 2023/4/7 3:59 PM
 */
@Configuration
public class NotifierConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "spring.boot.admin.notify.jvm", name = "enabled", havingValue = "true")
    @ConfigurationProperties("spring.boot.admin.notify.jvm")
    public JvmAlarm jvmAlarm(InstanceRepository repository, AlarmMessage alarmMessage, BasicAuthHttpHeaderProvider basicAuthHttpHeaderProvider) {
        return new JvmAlarm(repository, alarmMessage, basicAuthHttpHeaderProvider);
    }

}
