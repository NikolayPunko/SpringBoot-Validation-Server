package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.host.SpringBootValidationServer.exceptions.XMLParsingException;
import com.host.SpringBootValidationServer.model.LuMove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JsonService {

    private final MessageService messageService;
    private final JsonValidationService jsonValidationService;
    private final RoutingService routingService;
    private final LuMoveService luMoveService;


    @Autowired
    public JsonService(MessageService messageService, JsonValidationService jsonValidationService, RoutingService routingService, LuMoveService luMoveService) {
        this.messageService = messageService;
        this.jsonValidationService = jsonValidationService;
        this.routingService = routingService;
        this.luMoveService = luMoveService;
    }


    public void processJsonMsg(String json, Map<String, String> documentsForSend) {

        try {

            ObjectMapper jsonMapper = new ObjectMapper();
            List<String> errorList = new ArrayList<>();

            JsonNode jsonNode = null;
            try {
                jsonNode = jsonMapper.readTree(json);
            } catch (JsonProcessingException e) {
                errorList.add("Не удалось распарсить JSON сообщение, проверьте синтаксис.");
                log.error("Не удалось распарсить JSON сообщение, проверьте синтаксис.");
            }

            JsonNode msgNode = jsonNode.get("MESSAGE");

            String sender = msgNode.get("SENDER").asText();
            String msgType = msgNode.get("MSGTYPE").asText();
            String facility = msgNode.get("FACILITY").asText();

            if(msgType.equalsIgnoreCase("LU_MOVE")){ //обрабатываем LU_MOVE для сохранения в бд
                List<JsonNode> luMoveElements = msgNode.findValues(msgType);
                List<LuMove> luMoves = parseLuMovesJSON(luMoveElements);
                luMoveService.saveLuMoveList(luMoves);
            }

            /* получили искомый knm по которому будем брать нужные поля для валидации */
            String knmMsg = messageService.findKnmMsg(msgType);

            /* валидация */

            errorList = jsonValidationService.validate(msgNode, msgType, knmMsg);


            /* маршрутизация */

//            Map<String, String> documentsForSend = new HashMap<>();

            messageService.checkSender(sender, errorList);

            try {
                if (errorList.isEmpty()) {
                    List<String> receivers = messageService.getListReceivers(facility, knmMsg, sender);
                    generateDocListWithReceivers(msgNode, receivers, documentsForSend);
                }
            } catch (Exception e) {
                errorList.add(e.getMessage());
            }

            if (!errorList.isEmpty()) {
                String docWithError = generateDocWithError(msgNode, msgType, errorList);
                documentsForSend.put(sender, docWithError);
            }

            routingService.sendDocuments(documentsForSend, facility);

//            System.out.println(documentsForSend);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    private void generateDocListWithReceivers(JsonNode msgNode, List<String> receivers, Map<String, String> documents) {

        try {

            for (String receiver : receivers) {
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode newDoc = msgNode.deepCopy();
                newDoc.put("SENDER", "SERVER");
                newDoc.put("RECIEVER", receiver);

                String stringJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(newDoc);
                documents.put(receiver, stringJson);
            }

        } catch (Exception e) {
            throw new XMLParsingException("Ошибка создания сообщений для отправки получателям, проверьте поля RECEIVER и SENDER;");
        }

    }

    private String generateDocWithError(JsonNode msgNode, String msgType, List<String> errorList) {

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

            String stringJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
//            System.out.println(stringJson);
            return stringJson;

        } catch (Exception e) {
            e.printStackTrace();
            throw new XMLParsingException("Ошибка создания сообщения с ошибкой в формате JSON;");
        }
    }

    private List<LuMove> parseLuMovesJSON(List<JsonNode> luMoveElements) {

        List<LuMove> luMoves = new ArrayList<>();

        int counter;
        if (luMoveElements.get(0).isArray()){
            counter  = luMoveElements.get(0).size();
        } else if (luMoveElements.get(0).isObject()){
            counter = 1;
        } else {
            throw new RuntimeException("MsgTypesElements type not found!");
        }

        for (int i = 0; i < counter; i++) {

            LuMove luMove = new LuMove();

            JsonNode msgTypeNode = null;

            if (luMoveElements.get(0).isArray()){
                msgTypeNode = luMoveElements.get(0).get(i);
            } else if (luMoveElements.get(0).isObject()){
                msgTypeNode = luMoveElements.get(0);
            } else {
                throw new RuntimeException("MsgTypesElements type not found!");
            }

            Iterator<String> fieldNames = msgTypeNode.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode node = msgTypeNode.get(fieldName);

                try {
                    luMove.setFieldByName(fieldName, node.asText());
                } catch (Exception e){
                    continue;
                }
            }

            luMoves.add(luMove);
        }

        return luMoves;
    }


}
