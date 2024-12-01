package com.host.SpringBootValidationServer;

import com.host.SpringBootValidationServer.configuration.kafka.KafkaProducerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class SpringBootValidationServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootValidationServerApplication.class, args);
	}

}
