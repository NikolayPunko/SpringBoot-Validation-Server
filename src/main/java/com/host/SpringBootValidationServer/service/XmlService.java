package com.host.SpringBootValidationServer.service;

import com.host.SpringBootValidationServer.exceptions.XMLParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class XmlService {

    private final MessageService messageService;
    private final XmlValidationService xmlValidationService;
    private final RoutingService routingService;

    public XmlService(MessageService messageService, XmlValidationService xmlValidationService, RoutingService routingService) {
        this.messageService = messageService;
        this.xmlValidationService = xmlValidationService;
        this.routingService = routingService;
    }


    public void processMessage(String xml) {

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

            /* получили искомый knm по которому будем брать нужные поля для валидации */
            String knmMsg = messageService.findKnmMsg(msgType);


            /* валидация */

            List<String> errorList = xmlValidationService.validate(document, msgType, knmMsg);


            /* маршрутизация */

            Map<String, String> documentsForSend = new HashMap<>();

            messageService.checkSender(sender, errorList);

            try {
                if (errorList.isEmpty()) {
                    List<String> receivers = messageService.getListReceivers(facility, knmMsg, sender);
                    documentsForSend = generateDocListWithReceivers(document, receivers);
                }
            } catch (Exception e){
                errorList.add(e.getMessage());
            }

            if (!errorList.isEmpty()) {
                String docWithError = generateDocWithError(document, msgType, errorList);
                documentsForSend.put(sender, docWithError);

            }

            routingService.sendDocuments(documentsForSend, facility);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }

    }

    private Map<String, String> generateDocListWithReceivers(Document doc, List<String> receivers) {

        try {
            Map<String, String> documents = new HashMap<>();

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
//            transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); //выравнивание
            transformer.setOutputProperty("encoding", "UTF-8");

            DOMSource source = new DOMSource(doc);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            return sw.toString();
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
}
