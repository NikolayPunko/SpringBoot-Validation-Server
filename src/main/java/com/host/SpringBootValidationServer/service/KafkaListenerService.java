package com.host.SpringBootValidationServer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaListenerService {

    private final XmlService xmlService;

    @Autowired
    public KafkaListenerService(XmlService xmlService) {
        this.xmlService = xmlService;
    }

    @KafkaListener(topics = "nhtest", groupId = "validServer1")
    void listener1(String data) {
        log.info("Прослушано сообщение из топика nhtest  - {}", data);
        xmlService.processMessage(data);
    }

    @KafkaListener(topics = "NasToHost", groupId = "validServer1")
    void listener2(String data) {
        log.info("Прослушано сообщение из топика NasToHost  - {}", data);
        xmlService.processMessage(data);
    }

    @KafkaListener(topics = "HostToNas", groupId = "validServer1")
    void listener3(String data) {
        log.info("Прослушано сообщение из топика HostToNas - {}", data);
        xmlService.processMessage(data);
    }


}
