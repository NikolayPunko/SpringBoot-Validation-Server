package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TestService {


    public void test(String msg){

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            JsonNode node = xmlMapper.readTree(msg.getBytes());

            String json = jsonMapper.writeValueAsString(node);

            System.out.println(json);

            JsonNode jsonNode = jsonMapper.readTree(json);
            String xmlString = xmlMapper.writer().withRootName("MESSAGE").writeValueAsString(jsonNode);

            System.out.println(xmlString);



        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

}
