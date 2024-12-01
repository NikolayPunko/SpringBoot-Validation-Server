package com.host.SpringBootValidationServer.service;

import com.host.SpringBootValidationServer.exceptions.UnknownFieldTypeException;
import com.host.SpringBootValidationServer.model.NsNnode;
import com.host.SpringBootValidationServer.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDateTime;
import java.util.*;

import static com.host.SpringBootValidationServer.service.MessageService.NS_NMSG_MAP;
import static com.host.SpringBootValidationServer.service.MessageService.NS_NNODE_MAP;

@Slf4j
@Service
public class XmlValidationService {


    public List<String> validate(Document document, String msgType, String knmMsg) {

        NodeList msgTypesElements = document.getDocumentElement().getElementsByTagName(msgType);

        ArrayList<String> errorList = new ArrayList<>();

        validateNodes(msgTypesElements, errorList, knmMsg);

        return errorList;
    }

    private void validateNodes(NodeList nodeList, List<String> errorList, String knmMsg) {

        for (int i = 0; i < nodeList.getLength(); i++) {

            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                /* словарь заполнения обязательных полей для одного блока msgType */
                Map<String, Boolean> requiredFields = new HashMap<>();

                for (NsNnode x : NS_NNODE_MAP.get(knmMsg)) {
                    if (x.getKnm().trim().equals(knmMsg) && x.getObligatory().trim().equalsIgnoreCase("yes")) {
                        requiredFields.put(x.getNode().trim(), false);
                    }
                }

                for (int j = 0; j < nodeList.item(i).getChildNodes().getLength(); j++) {

                    if (nodeList.item(i).getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE) {
                        Node node = nodeList.item(i).getChildNodes().item(j);

                        Optional<NsNnode> optionalNsNnode = NS_NNODE_MAP.get(knmMsg).stream()
                                .filter(x -> x.getKnm().trim().equals(knmMsg) && node.getNodeName().equalsIgnoreCase(x.getNode().trim()))
                                .findFirst();

                        if (optionalNsNnode.isEmpty()) {
                            errorList.add(String.format("Ошибка в блоке %s поле %s не соответствует справочнику",nodeList.item(i).getNodeName(), node.getNodeName()));
                            continue;
                        }

                        NsNnode x = optionalNsNnode.get();


                        if (x.getObligatory().trim().equalsIgnoreCase("yes")) { //отметили что обязательное поле существует
                            requiredFields.put(x.getNode().trim(), true);
                        }

                        if (x.getType().trim().endsWith("[]")) {
                            String knm = NS_NMSG_MAP.get(x.getType().trim().substring(0, x.getType().trim().length() - 2)).getKnm().trim();
                                validateNodes(node.getChildNodes(), errorList, knm);

                        } else {

                            if (x.getObligatory().trim().equalsIgnoreCase("yes") && node.getTextContent().isEmpty()) {
                                errorList.add(String.format("Ошибка в блоке %s поле %s не должно быть пустым", nodeList.item(i).getNodeName(), node.getNodeName()));
                                continue;
                            } else if(node.getTextContent().isEmpty()){
                                continue;
                            }


                            boolean result = validateFieldByType(node.getTextContent(), x.getType().trim());

                            if (!result) {
                                errorList.add(String.format("Ошибка в блоке %s поле %s несоответствие типу", nodeList.item(i).getNodeName(), node.getNodeName()));
                            }
                        }

                    }


                }

                /* логика когда обязательные поля отсутствуют в словаре requiredFields */
                for (Map.Entry<String, Boolean> entry : requiredFields.entrySet()) {
                    if (!entry.getValue()) {
                        errorList.add(String.format("Ошибка в блоке %s поле %s отсутствует",
                                nodeList.item(i).getNodeName(), entry.getKey().trim()));
                    }
                }

            }
        }
    }


    public boolean validateFieldByType(String content, String type) {

        switch (type.toUpperCase()) {
            case "INTEGER" -> {
                return checkStrAsInteger(content);
            }
            case "DATETIME" -> {
                checkStrAsDateTime(content);
            }
            case "Y/N" -> {
                return checkStrAsBool(content);
            }
            case "UOM" -> {
                return checkStrAsUOM(content);
            }
            default -> {
                if (type.toUpperCase().startsWith("CHAR(") && type.toUpperCase().endsWith(")")) {
                    return checkStrAsChar(content, type);
                } else if(type.toUpperCase().startsWith("NUMBER")){
                    return checkStrAsNumber(content, type);
                }
                else {
                    throw new UnknownFieldTypeException(String.format("Неизвестный тип поля \"%s\"", type));
                }
            }
        }

        return false;
    }

    private boolean checkStrAsInteger(String content) {
        try {
            Integer.parseInt(content);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean checkStrAsNumber(String content, String type) {
        if(type.contains(",") && type.contains("(") && type.contains(")")){
            short quantityBeforePoint = Short.parseShort(type.substring(type.indexOf("(")+1, type.indexOf(",")));
            short accuracy = Short.parseShort(type.substring(type.indexOf(",")+1, type.indexOf(")")));
            return  (content.substring(0,content.indexOf(".")).length() <= quantityBeforePoint) &&
                    (content.substring(content.indexOf(".")+1).length() == accuracy);
        } else if(type.contains("(") && type.contains(")")){
            short quantityBeforePoint = Short.parseShort(type.substring(type.indexOf("(")+1, type.indexOf(")")));
            if(content.contains(".")){
                return content.substring(0,content.indexOf(".")).length() <= quantityBeforePoint;
            } else {
                return content.length() <= quantityBeforePoint;
            }
        } else {
            try {
                Double.parseDouble(content);
                return true;
            } catch (NumberFormatException e){
                return false;
            }
        }
    }

    private boolean checkStrAsChar(String content, String type) {
        short permittedLength = Short.parseShort(type.substring(5, type.length() - 1));
        return content.length() <= permittedLength;
    }

    public static boolean checkStrAsDateTime(String content) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(content, DateUtils.DATE_FORMAT);
            return true;
        } catch (Exception e){
            return false;
        }

    }

    private boolean checkStrAsBool(String content) {
        return (content.equalsIgnoreCase("Y")) || (content.equalsIgnoreCase("N"));
    }

    private boolean checkStrAsUOM(String content) {
        return (content.equalsIgnoreCase("PCE")) || (content.equalsIgnoreCase("KG"));
    }

}
