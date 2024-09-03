package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.host.SpringBootValidationServer.model.Connection;
import com.host.SpringBootValidationServer.model.NsGrNmsg;
import com.host.SpringBootValidationServer.model.NsNrule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.host.SpringBootValidationServer.service.MessageService.*;

@Slf4j
@Service
public class RoutingService {

    private final RestTemplate restTemplate;
    private final KafkaService kafkaService;

    public RoutingService(RestTemplate restTemplate, KafkaService kafkaService) {
        this.restTemplate = restTemplate;
        this.kafkaService = kafkaService;
    }


    public List<String> getListReceivers(Document document, String facility, String knmMsg, String sender) {

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

    public void sendDocuments(Map<String, Document> documents, String facility) {


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);


        for (Map.Entry<String, Document> entry : documents.entrySet()) {
            NsGrNmsg obj = NS_GRNMSG_MAP.get(entry.getKey());

            Connection connection = null;
            try {
                connection = objectMapper.readValue(obj.getConnection(), Connection.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Ошибка сопоставления со справочником NS_GRNMSG!");
            }

            String msg = convertDOMXMLtoString(entry.getValue());

            if (connection.getType().trim().equalsIgnoreCase("API")) {

                if (!facility.equalsIgnoreCase("NAS") && !facility.equalsIgnoreCase("HOST")) {
                    sendToHttp(msg, connection.getUrl(), connection.getBearer());
                    log.info("Отправили по Http '{}': \n {}", connection.getUrl(), msg);
                } else {
                    log.info("Отработала заглушка, Http '{}': \n {}", connection.getUrl(), msg);
                }

            } else if (connection.getType().trim().equalsIgnoreCase("Kafka")) {

                if (!facility.equalsIgnoreCase("NAS") && !facility.equalsIgnoreCase("HOST")) {
                sendToKafka(msg, connection.getTopic());
                    log.info("Отправили в Kafka '{}': \n {}", connection.getTopic(), msg);
                } else {
                    log.info("Отработала заглушка, Kafka '{}': \n {}", connection.getTopic(), msg);
                }

            } else {
                log.error("Тип маршрутизации {} не определен!", connection.getType().trim());
            }

        }

        log.info("Сообщение успешно обработано!");


    }

    private void sendToHttp(String message, String url, String bearer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setBearerAuth(bearer);
        HttpEntity<String> request = new HttpEntity<>(message, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, message, String.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Ошибка при отправке сообщения по адресу %s; ", url) + e);
        }

    }

    private void sendToKafka(String message, String topic) {
        kafkaService.sendMessage(message, topic);
    }


}
