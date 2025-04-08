package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.host.SpringBootValidationServer.exceptions.XMLParsingException;
import com.host.SpringBootValidationServer.model.LuMove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class XmlService {

    private final MessageService messageService;
    private final XmlValidationService xmlValidationService;
    private final RoutingService routingService;
    private final LuMoveService luMoveService;

    public XmlService(MessageService messageService, XmlValidationService xmlValidationService, RoutingService routingService, LuMoveService luMoveService) {
        this.messageService = messageService;
        this.xmlValidationService = xmlValidationService;
        this.routingService = routingService;
        this.luMoveService = luMoveService;
    }


    public void processXmlMsg(String xml, Map<String, String> documentsForSend) {

        xml = trimXML(xml);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            Document document = null;
            try {
                builder = factory.newDocumentBuilder();
                document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String sender = document.getDocumentElement().getElementsByTagName("SENDER").item(0).getTextContent();
            String msgType = document.getDocumentElement().getElementsByTagName("MSGTYPE").item(0).getTextContent();
            String facility = document.getDocumentElement().getElementsByTagName("FACILITY").item(0).getTextContent();


            if (msgType.equalsIgnoreCase("LU_MOVE")) { //обрабатываем LU_MOVE для сохранения в бд
                NodeList luMoveElements = document.getDocumentElement().getElementsByTagName(msgType);
                List<LuMove> luMoves = parseLuMovesXML(luMoveElements);
                luMoveService.saveLuMoveList(luMoves);
            }

            /* получили искомый knm по которому будем брать нужные поля для валидации */
            String knmMsg = messageService.findKnmMsg(msgType);


            /* валидация */

//            List<String> errorList = xmlValidationService.validate(document, msgType, knmMsg);
            List<String> errorList = new ArrayList<>(); //пропускаем т.к. справочник NS_NNODE не содержит полей для LU_MOVE по knm 0207000000

            /* маршрутизация */

            messageService.checkSender(sender, errorList);

            try {
                if (errorList.isEmpty()) {
                    List<String> receivers = messageService.getListReceivers(facility, knmMsg, sender);
                    generateDocListWithReceivers(document, receivers, documentsForSend);
                }
            } catch (Exception e) {
                errorList.add(e.getMessage());
            }

            if (!errorList.isEmpty()) {
                String docWithError = generateDocWithError(document, msgType, errorList);
                documentsForSend.put(sender, docWithError);

            }

            System.out.println();
//            routingService.sendDocuments(documentsForSend, facility);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }

    }

    private Map<String, String> generateDocListWithReceivers(Document doc, List<String> receivers, Map<String, String> documents) {

        try {

            for (String receiver : receivers) {
                Document newDoc = (Document) doc.cloneNode(true);
                newDoc.getElementsByTagName("SENDER").item(0).setTextContent("SERVER");
                newDoc.getElementsByTagName("RECIEVER").item(0).setTextContent(receiver);
                documents.put(receiver, convertDOMXMLtoString(newDoc));
            }

            return documents;
        } catch (Exception e) {
            throw new XMLParsingException("Ошибка создания сообщений для отправки получателям, проверьте поля RECEIVER и SENDER;");
        }

    }

    private String generateDocWithError(Document doc, String msgType, List<String> errorList) {

        try {

            Document newDocument = (Document) doc.cloneNode(true);
            removeChildsNode(newDocument, msgType);
            Node rootNode = newDocument.getElementsByTagName("MESSAGE").item(0);

            Node sysstatNode = newDocument.createElement("SYSSTAT");
            rootNode.appendChild(sysstatNode);

            Node errorCodeNode = newDocument.createElement("ERROR_CODE");
            Node descriptionNode = newDocument.createElement("DESCRIPTION");

            String msgId = newDocument.getElementsByTagName("MSGID").item(0).getTextContent();
            newDocument.getElementsByTagName("REPLYTO").item(0).setTextContent(msgId);

            errorCodeNode.setTextContent("400");
            descriptionNode.setTextContent(errorList.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .trim());

            sysstatNode.appendChild(errorCodeNode);
            sysstatNode.appendChild(descriptionNode);

            return convertDOMXMLtoString(newDocument);

        } catch (Exception e) {
            throw new XMLParsingException("Ошибка создания сообщения с ошибкой  в формате XML;");
        }
    }

    public static String convertDOMXMLtoString(Document doc) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //выравнивание
            transformer.setOutputProperty("encoding", "UTF-8");

            DOMSource source = new DOMSource(doc);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            return sw.getBuffer().toString().trim();
        } catch (TransformerException e) {
            throw new XMLParsingException("Не удалось распарсить xml файл!");
        }
    }

    private void removeChildsNode(Document doc, String nodeName) {
        int countEl = doc.getElementsByTagName(nodeName).getLength();
        for (int i = 0; i < countEl; i++) {
            Node node = doc.getElementsByTagName(nodeName).item(0);
            node.getParentNode().removeChild(node);
        }
        doc.normalize();
    }

    public static String trimXML(String input) {
        BufferedReader reader = new BufferedReader(new StringReader(input));
        StringBuffer result = new StringBuffer();
        try {
            String line;
            while ((line = reader.readLine()) != null)
                result.append(line.trim());
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<LuMove> parseLuMovesXML(NodeList luMoveElements) {

        List<LuMove> luMoves = new ArrayList<>();

        for (int i = 0; i < luMoveElements.getLength(); i++) {

            LuMove luMove = new LuMove();

            if (luMoveElements.item(i).getNodeType() == Node.ELEMENT_NODE) {

                for (int j = 0; j < luMoveElements.item(i).getChildNodes().getLength(); j++) {
                    Node node = luMoveElements.item(i).getChildNodes().item(j);
                    try {
                        luMove.setFieldByName(node.getNodeName(), node.getTextContent());
                    } catch (Exception e){
                        continue;
                    }
                }
            }

            luMoves.add(luMove);
        }

        return luMoves;
    }




}
