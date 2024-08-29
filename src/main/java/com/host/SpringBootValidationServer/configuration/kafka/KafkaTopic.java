package com.host.SpringBootValidationServer.configuration.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopic {

    @Bean
    public NewTopic test1() {
        return TopicBuilder.name("Test1").partitions(1).build();
    }

    @Bean
    public NewTopic test2() {
        return TopicBuilder.name("Test2").partitions(1).build();
    }


}
