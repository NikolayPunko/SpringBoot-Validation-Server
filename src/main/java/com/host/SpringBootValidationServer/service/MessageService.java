package com.host.SpringBootValidationServer.service;

import com.host.SpringBootValidationServer.exceptions.XMLParsingException;
import com.host.SpringBootValidationServer.model.NsGrNmsg;
import com.host.SpringBootValidationServer.model.NsNmsg;
import com.host.SpringBootValidationServer.model.NsNnode;
import com.host.SpringBootValidationServer.model.NsNrule;
import com.host.SpringBootValidationServer.repositories.GRNMSGRepository;
import com.host.SpringBootValidationServer.repositories.NMSGRepository;
import com.host.SpringBootValidationServer.repositories.NNODERepository;
import com.host.SpringBootValidationServer.repositories.NRULERepository;
import com.host.SpringBootValidationServer.util.XMLExamples;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.*;

@Service
public class MessageService {

    public static Map<String, NsNmsg> NS_NMSG_MAP = new HashMap<>();
    public static Map<String, List<NsNnode>> NS_NNODE_MAP = new HashMap<>();
    public static Map<String, List<NsNrule>> NS_NRULE_MAP = new HashMap<>();
    public static Map<String, NsGrNmsg> NS_GRNMSG_MAP = new HashMap<>();

    private final ValidationService validationService;
    private final RoutingService routingService;
    private final NMSGRepository nmsgRepository;
    private final NNODERepository nnodeRepository;
    private final NRULERepository nruleRepository;
    private final GRNMSGRepository grnmsgRepository;

    @Autowired
    public MessageService(ValidationService validationService, RoutingService routingService, NMSGRepository nmsgRepository, NNODERepository nnodeRepository, NRULERepository nruleRepository, GRNMSGRepository grnmsgRepository) {
        this.validationService = validationService;
        this.routingService = routingService;
        this.nmsgRepository = nmsgRepository;
        this.nnodeRepository = nnodeRepository;
        this.nruleRepository = nruleRepository;
        this.grnmsgRepository = grnmsgRepository;
    }

    @PostConstruct
    private void postConstruct() {
        for (NsNmsg obj: nmsgRepository.findAll()) {
            NS_NMSG_MAP.put(obj.getMsgType().trim(), obj);
        }

        for (NsNnode obj: nnodeRepository.findAll()) {
            List<NsNnode> nnodeList = NS_NNODE_MAP.get(obj.getKnm().trim()) == null? new ArrayList<>(): NS_NNODE_MAP.get(obj.getKnm().trim());
            nnodeList.add(obj);
            NS_NNODE_MAP.put(obj.getKnm().trim(), nnodeList);
        }

        for (NsNrule obj: nruleRepository.findAll()) {
            List<NsNrule> nruleList = NS_NRULE_MAP.get(obj.getKnm().trim()) == null? new ArrayList<>(): NS_NRULE_MAP.get(obj.getKnm().trim());
            nruleList.add(obj);
            NS_NRULE_MAP.put(obj.getKnm().trim(), nruleList);
        }

        for (NsGrNmsg obj: grnmsgRepository.findAll()) {
            NS_GRNMSG_MAP.put(obj.getKgr().trim(), obj);
        }
    }

    public void processMessage(){

//        String xml = XMLExamples.SYSSTAT_XML;
//        String xml = XMLExamples.ITEM_XML;
        String xml = XMLExamples.TEST1_XML;
//        String xml = XMLExamples.PACKING_XML;


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
        String knmMsg = findKnmMsg(sender, msgType);



        List<String> errorList = validationService.validate(document, msgType, knmMsg);

        System.out.println(errorList);

        /* Маршрутизация */

        Map<String, Document> documentsForSend = new HashMap<>();

        if(!errorList.isEmpty()){
            Document docWithError = generateDocWithError(document, msgType, errorList);
            documentsForSend.put(sender, docWithError);

        } else {
            List<String> receivers = routingService.getListReceivers(document, facility, knmMsg, sender);
            documentsForSend = generateDocListWithReceivers(document, receivers);
        }

        routingService.sendDocuments(documentsForSend);



    }

    private Map<String, Document> generateDocListWithReceivers(Document doc, List<String> receivers){

        Map<String, Document> documents = new HashMap<>();

        for (String receiver: receivers) {
            Document newDoc = (Document) doc.cloneNode(true);
            newDoc.getElementsByTagName("SENDER").item(0).setTextContent("SERVER");
            newDoc.getElementsByTagName("RECEIVER").item(0).setTextContent(receiver);
            documents.put(receiver, newDoc);
        }

        return documents;
    }

    private Document generateDocWithError(Document doc, String msgType, List<String> errorList){

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

        return newDocument;
    }


    private String findKnmMsg(String sender, String msgType){
        return NS_NMSG_MAP.get(msgType).getKnm().trim();
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

    private void removeChildsNode(Document doc, String nodeName){
        int countEl = doc.getElementsByTagName(nodeName).getLength();
        for (int i = 0; i < countEl; i++) {
            Node node = doc.getElementsByTagName(nodeName).item(0);
            node.getParentNode().removeChild(node);
        }
        doc.normalize();
    }

}
