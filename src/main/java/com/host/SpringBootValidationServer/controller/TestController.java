package com.host.SpringBootValidationServer.controller;

import com.host.SpringBootValidationServer.service.JsonService;
import com.host.SpringBootValidationServer.service.MessageService;
import com.host.SpringBootValidationServer.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    private final MessageService messageService;

    private final TestService testService;

    private final JsonService jsonService;

    @Autowired
    public TestController(MessageService messageService, TestService testService, JsonService jsonService) {
        this.messageService = messageService;
        this.testService = testService;
        this.jsonService = jsonService;
    }

    @PostMapping(value = "/test", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> test(@RequestBody String xml) {
        log.info("Входящее сообщение по адресу /test  - {}", xml);
        messageService.processMessage(xml);
        return ResponseEntity.ok("test!");
    }

    @PostMapping(value = "/testJson", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> test2(@RequestBody String json) {
        log.info("Входящее сообщение по адресу /testJson  - {}", json);
        jsonService.processJsonMessage(json);
        return ResponseEntity.ok("test!");
    }

}
