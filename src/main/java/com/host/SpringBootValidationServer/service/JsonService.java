package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JsonService {

    private final MessageService messageService;

    private final JsonValidationService jsonValidationService;

    public JsonService(MessageService messageService, JsonValidationService jsonValidationService) {
        this.messageService = messageService;
        this.jsonValidationService = jsonValidationService;
    }


    public void processJsonMessage(String json) {

        ObjectMapper jsonMapper = new ObjectMapper();

        JsonNode jsonNode = null;
        try {
            jsonNode = jsonMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        JsonNode msgNode = jsonNode.get("MESSAGE");

        String sender = msgNode.get("SENDER").asText();
        String msgType = msgNode.get("MSGTYPE").asText();
        String facility = msgNode.get("FACILITY").asText();

        /* получили искомый knm по которому будем брать нужные поля для валидации */
        String knmMsg = messageService.findKnmMsg(sender, msgType);

        /* валидация */

        List<String> errorList = jsonValidationService.validate(msgNode, msgType, knmMsg);


        System.out.println("end.");
    }


}
