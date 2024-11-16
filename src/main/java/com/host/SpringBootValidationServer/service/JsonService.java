package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.host.SpringBootValidationServer.exceptions.XMLParsingException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


        /* Маршрутизация */

        Map<String, JsonNode> documentsForSend = new HashMap<>();

        messageService.checkSender(sender, errorList);

        try {
            if (errorList.isEmpty()) {
//                List<String> receivers = routingService.getListReceivers(document, facility, knmMsg, sender);
//                documentsForSend = generateDocListWithReceivers(document, receivers);
            }
        } catch (Exception e){
            errorList.add(e.getMessage());
        }

        if (!errorList.isEmpty()) {
            JsonNode docWithError = generateDocWithError(msgNode, msgType, errorList);
            documentsForSend.put(sender, docWithError);

        }

//        routingService.sendDocuments(documentsForSend, facility);


        System.out.println("end.");
    }

    private JsonNode generateDocWithError(JsonNode msgNode, String msgType, List<String> errorList) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = (ObjectNode) msgNode;

            objectNode.remove(msgType);

            ObjectNode sysstatNode = objectMapper.createObjectNode();
            sysstatNode.put("ERROR_CODE", 400);
            sysstatNode.put("DESCRIPTION", errorList.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .trim());

            objectNode.set("SYSSTAT", sysstatNode);
            objectNode.put("REPLYTO", objectNode.get("MSGID").asLong());

            String updatedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
            System.out.println(updatedJson);

            return objectNode;

        } catch (Exception e) {
            e.printStackTrace();
            throw new XMLParsingException("Ошибка создания сообщения с ошибкой в формате JSON;");
        }
    }



}
