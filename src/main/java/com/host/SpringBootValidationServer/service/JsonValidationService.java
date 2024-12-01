package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.host.SpringBootValidationServer.model.NsNnode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.host.SpringBootValidationServer.service.MessageService.NS_NMSG_MAP;
import static com.host.SpringBootValidationServer.service.MessageService.NS_NNODE_MAP;

@Service
public class JsonValidationService {

    private final MessageService messageService;

    @Autowired
    public JsonValidationService(MessageService messageService) {
        this.messageService = messageService;
    }


    public List<String> validate(JsonNode msgNode, String msgType, String knmMsg) {

        List<JsonNode> msgTypesElements = msgNode.findValues(msgType);

        ArrayList<String> errorList = new ArrayList<>();

        validateNodes(msgTypesElements, errorList, knmMsg);

        return errorList;
    }


    private void validateNodes(List<JsonNode> msgTypesElements, List<String> errorList, String knmMsg) {

        int counter;

        if (msgTypesElements.get(0).isArray()){
            counter  = msgTypesElements.get(0).size();
        } else if (msgTypesElements.get(0).isObject()){
            counter = 1;
        } else {
            throw new RuntimeException("MsgTypesElements type not found!");
        }

        for (int i = 0; i < counter; i++) {

            /* словарь заполнения обязательных полей для одного блока msgType */
            Map<String, Boolean> requiredFields = new HashMap<>();

            for (NsNnode x : NS_NNODE_MAP.get(knmMsg)) {
                if (x.getKnm().trim().equals(knmMsg) && x.getObligatory().trim().equalsIgnoreCase("yes")) {
                    requiredFields.put(x.getNode().trim(), false);
                }
            }


            JsonNode msgTypeNode = null;

            if (msgTypesElements.get(0).isArray()){
                msgTypeNode = msgTypesElements.get(0).get(i);
            } else if (msgTypesElements.get(0).isObject()){
                msgTypeNode = msgTypesElements.get(0);
            } else {
                throw new RuntimeException("MsgTypesElements type not found!");
            }


            Iterator<String> fieldNames = msgTypeNode.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldMsgType = msgTypeNode.get(fieldName);

                Optional<NsNnode> optionalNsNnode = NS_NNODE_MAP.get(knmMsg).stream()
                        .filter(x -> x.getKnm().trim().equals(knmMsg) && fieldName.equalsIgnoreCase(x.getNode().trim()))
                        .findFirst();

                if (optionalNsNnode.isEmpty()) {
                    errorList.add(String.format("Ошибка в блоке %s поле %s не соответствует справочнику", "SYSSTAT", fieldName));
                    continue;
                }

                NsNnode x = optionalNsNnode.get();

                if (x.getObligatory().trim().equalsIgnoreCase("yes")) { //отметили что обязательное поле существует
                    requiredFields.put(x.getNode().trim(), true);
                }


                if (x.getType().trim().endsWith("[]")) {
                    String arrField = x.getType().trim().substring(0, x.getType().trim().length()-2);
                    String knm = NS_NMSG_MAP.get(x.getType().trim().substring(0, x.getType().trim().length() - 2)).getKnm().trim();

                    try {
                        validateNodes(fieldMsgType.findValues(arrField), errorList, knm);
                    } catch (Exception e){
                        errorList.add(String.format("Ошибка в блоке %s поле %s не найдено", x.getNode().trim(), arrField));
                        continue;
                    }

                } else {

                    if (x.getObligatory().trim().equalsIgnoreCase("yes") && fieldMsgType.asText().isEmpty()) {
                        errorList.add(String.format("Ошибка в блоке %s поле %s не должно быть пустым", "SYSSTAT", fieldName));
                        continue;
                    } else if(fieldMsgType.asText().isEmpty()){
                        continue;
                    }


                    boolean result = messageService.validateFieldByType(fieldMsgType.asText(), x.getType().trim());

                    if (!result) {
                        errorList.add(String.format("Ошибка в блоке %s поле %s несоответствие типу", "SYSSTAT", fieldName));
                    }
                }


            }

            /* логика когда обязательные поля отсутствуют в словаре requiredFields */
            for (Map.Entry<String, Boolean> entry : requiredFields.entrySet()) {
                if (!entry.getValue()) {
                    errorList.add(String.format("Ошибка в блоке %s поле %s отсутствует",
                            "SYSSTAT", entry.getKey().trim()));
                }
            }


        }

    }

}
