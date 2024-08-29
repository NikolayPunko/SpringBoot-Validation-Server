package com.host.SpringBootValidationServer;

import com.host.SpringBootValidationServer.service.MessageService;
import com.host.SpringBootValidationServer.service.ValidationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootValidationServerApplication {

//	private static final MessageService messageService = new MessageService(new ValidationService(), nmsgRepository);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootValidationServerApplication.class, args);


//		long durParsing = -1;
//		long startTimeParsing = System.nanoTime();
//		for (int i = 0; i < 1; i++) {
//			messageService.processMessage();
//		}
//		long endTimeParsing = System.nanoTime();
//		System.out.println(durParsing = (endTimeParsing - startTimeParsing));

	}

}
