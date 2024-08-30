package com.host.SpringBootValidationServer.controller;

import com.host.SpringBootValidationServer.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final MessageService messageService;

    @Autowired
    public TestController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/valid")
    public ResponseEntity<?> test() {
        messageService.processMessage("xml");
        return ResponseEntity.ok("test!");
    }

}
