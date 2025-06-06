package com.host.SpringBootValidationServer.service;

import com.host.SpringBootValidationServer.model.NsNnode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

import static com.host.SpringBootValidationServer.service.MessageService.NS_NMSG_MAP;
import static com.host.SpringBootValidationServer.service.MessageService.NS_NNODE_MAP;

@Slf4j
@Service
public class XmlValidationService {

    private final MessageService messageService;

    @Autowired
    public XmlValidationService(MessageService messageService) {
        this.messageService = messageService;
    }


    public List<String> validate(Document document, String msgType, String knmMsg) {

        NodeList msgTypesElements = document.getDocumentElement().getElementsByTagName(msgType);

        ArrayList<String> errorList = new ArrayList<>();

        validateNodes(msgTypesElements, errorList, knmMsg);

        return errorList;
    }

    private void validateNodes(NodeList nodeList, List<String> errorList, String knmMsg) {
        try {
            for (int i = 0; i < nodeList.getLength(); i++) {

                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {

                    /* словарь заполнения обязательных полей для одного блока msgType */
                    Map<String, Boolean> requiredFields = messageService.defineRequiredFields(knmMsg, errorList);


                    for (int j = 0; j < nodeList.item(i).getChildNodes().getLength(); j++) {

                        if (nodeList.item(i).getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Node node = nodeList.item(i).getChildNodes().item(j);

                            Optional<NsNnode> optionalNsNnode = NS_NNODE_MAP.get(knmMsg).stream()
                                    .filter(x -> x.getKnm().trim().equals(knmMsg) && node.getNodeName().equalsIgnoreCase(x.getNode().trim()))
                                    .findFirst();

                            if (optionalNsNnode.isEmpty()) {
                                errorList.add(String.format("Поле %s не соответствует справочнику", node.getNodeName()));
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
                                    errorList.add(String.format("Поле %s не должно быть пустым", node.getNodeName()));
                                    continue;
                                } else if (node.getTextContent().isEmpty()) {
                                    continue;
                                }


                                boolean result = messageService.validateFieldByType(node.getTextContent(), x.getType().trim());

                                if (!result) {
                                    errorList.add(String.format("Поле %s несоответствие типу", node.getNodeName()));
                                }
                            }

                        }


                    }

                    /* логика когда обязательные поля отсутствуют в словаре requiredFields */
                    messageService.writeMissingFields(requiredFields, errorList);

                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }

    }


}
