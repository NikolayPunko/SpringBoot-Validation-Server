package com.host.SpringBootValidationServer.controller;

import com.host.SpringBootValidationServer.service.MessageService;
import com.host.SpringBootValidationServer.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    private final MessageService messageService;
    private final TestService testService;

    @Autowired
    public TestController(MessageService messageService, TestService testService) {
        this.messageService = messageService;
        this.testService = testService;
    }

    @PostMapping(value = "/test")
    public ResponseEntity<?> test(@RequestBody String msg) {
        log.info("Входящее сообщение по адресу /test  - {}", msg);
        messageService.processMsgByContentType(msg);
        return ResponseEntity.ok("test!");
    }


}
