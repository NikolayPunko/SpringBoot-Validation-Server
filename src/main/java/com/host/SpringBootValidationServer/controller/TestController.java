package com.host.SpringBootValidationServer.controller;

import com.host.SpringBootValidationServer.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    private final MessageService messageService;

    @Autowired
    public TestController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(value = "/test", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> test(@RequestBody String xml) {
        log.info("Входящее сообщение по адресу /test  - {}", xml);
        messageService.processMessage(xml);
        return ResponseEntity.ok("test!");
    }

}
