package com.host.SpringBootValidationServer.service;

import com.host.SpringBootValidationServer.exceptions.UnknownFieldTypeException;
import com.host.SpringBootValidationServer.model.NsGrNmsg;
import com.host.SpringBootValidationServer.model.NsNmsg;
import com.host.SpringBootValidationServer.model.NsNnode;
import com.host.SpringBootValidationServer.model.NsNrule;
import com.host.SpringBootValidationServer.repositories.GRNMSGRepository;
import com.host.SpringBootValidationServer.repositories.NMSGRepository;
import com.host.SpringBootValidationServer.repositories.NNODERepository;
import com.host.SpringBootValidationServer.repositories.NRULERepository;
import com.host.SpringBootValidationServer.util.DateUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class MessageService {

    public static Map<String, NsNmsg> NS_NMSG_MAP = new HashMap<>();
    public static Map<String, List<NsNnode>> NS_NNODE_MAP = new HashMap<>();
    public static Map<String, List<NsNrule>> NS_NRULE_MAP = new HashMap<>();
    public static Map<String, NsGrNmsg> NS_GRNMSG_MAP = new HashMap<>();


    private final NMSGRepository nmsgRepository;
    private final NNODERepository nnodeRepository;
    private final NRULERepository nruleRepository;
    private final GRNMSGRepository grnmsgRepository;

    private final JsonService jsonService;
    private final XmlService xmlService;

    @Autowired
    public MessageService(NMSGRepository nmsgRepository, NNODERepository nnodeRepository, NRULERepository nruleRepository,
                          GRNMSGRepository grnmsgRepository, @Lazy JsonService jsonService, @Lazy XmlService xmlService) {
        this.nmsgRepository = nmsgRepository;
        this.nnodeRepository = nnodeRepository;
        this.nruleRepository = nruleRepository;
        this.grnmsgRepository = grnmsgRepository;
        this.jsonService = jsonService;
        this.xmlService = xmlService;
    }

    @PostConstruct
    private void postConstruct() {
        for (NsNmsg obj : nmsgRepository.findAll()) {
            NS_NMSG_MAP.put(obj.getMsgType().trim(), obj);
        }

        for (NsNnode obj : nnodeRepository.findAll()) {
            List<NsNnode> nnodeList = NS_NNODE_MAP.get(obj.getKnm().trim()) == null ? new ArrayList<>() : NS_NNODE_MAP.get(obj.getKnm().trim());
            nnodeList.add(obj);
            NS_NNODE_MAP.put(obj.getKnm().trim(), nnodeList);
        }

        for (NsNrule obj : nruleRepository.findAll()) {
            List<NsNrule> nruleList = NS_NRULE_MAP.get(obj.getKnm().trim()) == null ? new ArrayList<>() : NS_NRULE_MAP.get(obj.getKnm().trim());
            nruleList.add(obj);
            NS_NRULE_MAP.put(obj.getKnm().trim(), nruleList);
        }

        for (NsGrNmsg obj : grnmsgRepository.findAll()) {
            NS_GRNMSG_MAP.put(obj.getKgr().trim(), obj);
        }
    }

    public void processMsgByContentType(String msg){

        Map<String, String> documentsForSend = new HashMap<>();

        if(isJson(msg)){
            jsonService.processJsonMsg(msg,documentsForSend);
        } else if(isXml(msg)){
            xmlService.processXmlMsg(msg, documentsForSend);
        } else {
            log.error("Unable to determine message type:\n {}", msg);
            throw new RuntimeException("Unable to determine message type!");
        }
    }

    public void processMsgByContentType(String msg, Map<String, String> documentsForSend){

        if(isJson(msg)){
            jsonService.processJsonMsg(msg,documentsForSend);
        } else if(isXml(msg)) {
            xmlService.processXmlMsg(msg, documentsForSend);
        } else {
            log.error("Unable to determine message type:\n {}", msg);
            throw new RuntimeException("Unable to determine message type!");
        }
    }

    public static boolean isJson(String str) {
        try {
            new JSONObject(str);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean isXml(String str) {
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
//            builder.parse(new ByteArrayInputStream(str.getBytes()));
            builder.parse(new InputSource(new StringReader(str.trim())));
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public void checkSender(String sender,  List<String> errorList){
        if(!NS_GRNMSG_MAP.containsKey(sender)){
            errorList.add("Sender не прописан в правилах марштрутизации;");
            throw new RuntimeException("Sender not specified in the routing rules;");
        }
    }

    public String findKnmMsg(String msgType) {
        return NS_NMSG_MAP.get(msgType).getKnm().trim();
    }

    public List<String> getListReceivers(String facility, String knmMsg, String sender) {

        List<String> receiverList = new ArrayList<>();

        boolean isValidMsg = false;

        for (NsNrule x : NS_NRULE_MAP.get(knmMsg)) {
            if (x.getFacility().trim().equalsIgnoreCase(facility)) {
                if (x.getSender().trim().equalsIgnoreCase(sender)) {
                    isValidMsg = true;
                }

                receiverList.add(x.getReceiver().trim());
            }
        }

        if (!isValidMsg) {
            throw new RuntimeException("Cообщение не прописано в правилах маршрутизации!");
        }

        return receiverList;
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
