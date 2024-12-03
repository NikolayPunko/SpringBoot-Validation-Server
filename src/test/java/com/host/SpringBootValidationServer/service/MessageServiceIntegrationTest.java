package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.host.SpringBootValidationServer.SpringBootValidationServerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SpringBootValidationServerApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.properties")
class MessageServiceIntegrationTest {

    @Autowired
    private MessageService messageService;

    @MockBean
    private RoutingService routingService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void before(){
        objectMapper = new ObjectMapper();
    }


    @Test
    public void testProcessMsgByContentType1() throws Exception {

        String msg = "{\n" +
                "  \"MESSAGE\": {\n" +
                "    \"MSGID\": 3385,\n" +
                "    \"MSGTYPE\": \"SYSSTAT\",\n" +
                "    \"REPLYTO\": \"\",\n" +
                "    \"TIMESTAMP\": 20240815152950,\n" +
                "    \"FACILITY\": \"TEST1\",\n" +
                "    \"ACTION\": \"SET\",\n" +
                "    \"SENDER\": \"HOST\",\n" +
                "    \"RECIEVER\": \"TEST1\",\n" +
                "    \"SYSSTAT\": {\n" +
                "      \"ERROR_CODE\": 0,\n" +
                "      \"DESCRIPTION\": \"ok\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String expStr1 = "{\n" +
                "  \"MSGID\" : 3385,\n" +
                "  \"MSGTYPE\" : \"SYSSTAT\",\n" +
                "  \"REPLYTO\" : \"\",\n" +
                "  \"TIMESTAMP\" : 20240815152950,\n" +
                "  \"FACILITY\" : \"TEST1\",\n" +
                "  \"ACTION\" : \"SET\",\n" +
                "  \"SENDER\" : \"SERVER\",\n" +
                "  \"RECIEVER\" : \"TEST2\",\n" +
                "  \"SYSSTAT\" : {\n" +
                "    \"ERROR_CODE\" : 0,\n" +
                "    \"DESCRIPTION\" : \"ok\"\n" +
                "  }\n" +
                "}";

        String expStr2 = "{\n" +
                "  \"MSGID\" : 3385,\n" +
                "  \"MSGTYPE\" : \"SYSSTAT\",\n" +
                "  \"REPLYTO\" : \"\",\n" +
                "  \"TIMESTAMP\" : 20240815152950,\n" +
                "  \"FACILITY\" : \"TEST1\",\n" +
                "  \"ACTION\" : \"SET\",\n" +
                "  \"SENDER\" : \"SERVER\",\n" +
                "  \"RECIEVER\" : \"TEST1\",\n" +
                "  \"SYSSTAT\" : {\n" +
                "    \"ERROR_CODE\" : 0,\n" +
                "    \"DESCRIPTION\" : \"ok\"\n" +
                "  }\n" +
                "}";


        Map<String, String> documentsForSend = new HashMap<>();

        //подменяем метод sendDocuments, чтобы он ничего не делал
        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(2, documentsForSend.size());

        var expJson1 = objectMapper.readTree(expStr1);
        var actualJson1 = objectMapper.readTree(documentsForSend.get("TEST2"));
        assertEquals(expJson1, actualJson1);

        var expJson2 = objectMapper.readTree(expStr2);
        var actualJson2 = objectMapper.readTree(documentsForSend.get("TEST1"));
        assertEquals(expJson2, actualJson2);
    }

    @Test
    public void testProcessMsgByContentType2() throws Exception {

        String msg = "{\n" +
                "  \"MESSAGE\": {\n" +
                "    \"MSGID\": 3385,\n" +
                "    \"MSGTYPE\": \"SYSSTAT\",\n" +
                "    \"REPLYTO\": \"\",\n" +
                "    \"TIMESTAMP\": 20240815152950,\n" +
                "    \"FACILITY\": \"TEST1\",\n" +
                "    \"ACTION\": \"SET\",\n" +
                "    \"SENDER\": \"HOST\",\n" +
                "    \"RECIEVER\": \"TEST1\",\n" +
                "    \"SYSSTAT\": {\n" +
//                "      \"ERROR_CODE\" : 400,\n" +
                "      \"DESCRIPTION\": \"ok\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String expStr1 = "{\n" +
                "  \"MSGID\" : 3385,\n" +
                "  \"MSGTYPE\" : \"SYSSTAT\",\n" +
                "  \"REPLYTO\" : 3385,\n" +
                "  \"TIMESTAMP\" : 20240815152950,\n" +
                "  \"FACILITY\" : \"TEST1\",\n" +
                "  \"ACTION\" : \"SET\",\n" +
                "  \"SENDER\" : \"HOST\",\n" +
                "  \"RECIEVER\" : \"TEST1\",\n" +
                "  \"SYSSTAT\" : {\n" +
                "    \"ERROR_CODE\" : 400,\n" +
                "    \"DESCRIPTION\" : \"Поле ERROR_CODE отсутствует\"\n" +
                "  }\n" +
                "}";


        Map<String, String> documentsForSend = new HashMap<>();

        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(1, documentsForSend.size());

        var expJson1 = objectMapper.readTree(expStr1);
        var actualJson1 = objectMapper.readTree(documentsForSend.get("HOST"));
        assertEquals(expJson1, actualJson1);
    }

    @Test
    public void testProcessMsgByContentType3() throws Exception {

        String msg = "{\n" +
                "  \"MESSAGE\": {\n" +
                "    \"MSGID\": 161,\n" +
                "    \"MSGTYPE\": \"ITEM\",\n" +
                "    \"REPLYTO\": \"\",\n" +
                "    \"TIMESTAMP\": 20240815152950,\n" +
                "    \"FACILITY\": \"TEST19\",\n" +
                "    \"ACTION\": \"SET\",\n" +
                "    \"SENDER\": \"HOST\",\n" +
                "    \"RECIEVER\": \"TEST19\",\n" +
                "    \"ITEM\": {\n" +
                "      \"ITEM_ID\": 307060137,\n" +
                "      \"ITEM_REF\": 3937,\n" +
                "      \"SKU_UOM\": \"\",\n" +
                "      \"BASIC_UOM\": \"PCE\",\n" +
                "      \"SKU_BASIC_QUANTITY\": \"\",\n" +
                "      \"NAME\": \"Сырок гл. МАКОВКА 20% 40г\",\n" +
                "      \"EAN\": 4810268055485,\n" +
                "      \"CATEGORY\": 1803090000,\n" +
                "      \"CATEGORY_NAME\": \"конфетно-десертная линейка\",\n" +
                "      \"NAS\": \"\",\n" +
                "      \"DESCRIPTION\": \"Сырок творожный глазированный \\\"Маковка\\\" массовой долей жира 20,0 %, ФЛОУПАК 40 г\",\n" +
                "      \"SPECIFICATION\": \"\",\n" +
                "      \"ACTIVE\": \"\",\n" +
                "      \"QUALITY_CONTROL\": \"Y\",\n" +
                "      \"NETTO_WEIGHT\": \"\",\n" +
                "      \"BRUTTO_WEIGHT\": 0.04,\n" +
                "      \"VOLUME\": \"\",\n" +
                "      \"SHELF_LIFE\": 30,\n" +
                "      \"FREQUENCY\": \"\",\n" +
                "      \"LOT\": \"Y\",\n" +
                "      \"BBDATE\": \"Y\",\n" +
                "      \"SERIAL\": \"\",\n" +
                "      \"WRAPPING\": \"\",\n" +
                "      \"LU_TYPE\": \"\",\n" +
                "      \"TEMPERATURE_REGIME\": \"\",\n" +
                "      \"PACKINGS\": {\n" +
                "        \"PACKING\": [\n" +
                "          {\n" +
                "            \"ITEM_ID\": 307060137,\n" +
                "            \"EANPACK\": 14810268055482,\n" +
                "            \"PARENT_QUANTITY\": 18,\n" +
                "            \"LU_QUANTITY\": \"315\",\n" +
                "            \"NAME\": \"\",\n" +
                "            \"DESCRIPTION\": \"\",\n" +
                "            \"WIDTH\": \"\",\n" +
                "            \"LENGTH\": \"\",\n" +
                "            \"HEIGHT\": \"\",\n" +
                "            \"NET_WEIGHT\": \"\",\n" +
                "            \"GROSS_WEIGHT\": \"\",\n" +
                "            \"VOLUME\": \"\",\n" +
                "            \"ZIP\": \"\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"ITEM_ID\": 307060137,\n" +
                "            \"EANPACK\": 24810268055489,\n" +
                "            \"PARENT_QUANTITY\": 18,\n" +
                "            \"LU_QUANTITY\": 315,\n" +
                "            \"NAME\": \"\",\n" +
                "            \"DESCRIPTION\": \"\",\n" +
                "            \"WIDTH\": \"\",\n" +
                "            \"LENGTH\": \"\",\n" +
                "            \"HEIGHT\": \"\",\n" +
                "            \"NET_WEIGHT\": \"\",\n" +
                "            \"GROSS_WEIGHT\": \"\",\n" +
                "            \"VOLUME\": \"\",\n" +
                "            \"ZIP\": \"\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String expStr1 = "{\n" +
                "  \"MSGID\" : 161,\n" +
                "  \"MSGTYPE\" : \"ITEM\",\n" +
                "  \"REPLYTO\" : 161,\n" +
                "  \"TIMESTAMP\" : 20240815152950,\n" +
                "  \"FACILITY\" : \"TEST19\",\n" +
                "  \"ACTION\" : \"SET\",\n" +
                "  \"SENDER\" : \"HOST\",\n" +
                "  \"RECIEVER\" : \"TEST19\",\n" +
                "  \"SYSSTAT\" : {\n" +
                "    \"ERROR_CODE\" : 400,\n" +
                "    \"DESCRIPTION\" : \"Cообщение не прописано в правилах маршрутизации!\"\n" +
                "  }\n" +
                "}";


        Map<String, String> documentsForSend = new HashMap<>();

        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(1, documentsForSend.size());

        var expJson1 = objectMapper.readTree(expStr1);
        var actualJson1 = objectMapper.readTree(documentsForSend.get("HOST"));
        assertEquals(expJson1, actualJson1);
    }

    @Test
    public void testProcessMsgByContentType4() throws Exception {

        String msg = "{\n" +
                "  \"MESSAGE\": {\n" +
                "    \"MSGID\": 161,\n" +
                "    \"MSGTYPE\": \"ITEM\",\n" +
                "    \"REPLYTO\": \"\",\n" +
                "    \"TIMESTAMP\": 20240815152950,\n" +
                "    \"FACILITY\": \"NAS\",\n" +
                "    \"ACTION\": \"SET\",\n" +
                "    \"SENDER\": \"HOST\",\n" +
                "    \"RECIEVER\": \"NAS\",\n" +
                "    \"ITEM\": {\n" +
                "      \"ITEM_ID\": 307060137,\n" +
                "      \"ITEM_REF\": 3937,\n" +
                "      \"SKU_UOM\": \"\",\n" +
                "      \"BASIC_UOM\": \"PCE\",\n" +
                "      \"SKU_BASIC_QUANTITY\": \"\",\n" +
                "      \"NAME\": \"Сырок гл. МАКОВКА 20% 40г\",\n" +
                "      \"EAN\": 4810268055485,\n" +
                "      \"CATEGORY\": 1803090000,\n" +
                "      \"CATEGORY_NAME\": \"конфетно-десертная линейка\",\n" +
                "      \"NAS\": \"\",\n" +
                "      \"DESCRIPTION\": \"Сырок творожный глазированный \\\"Маковка\\\" массовой долей жира 20,0 %, ФЛОУПАК 40 г\",\n" +
                "      \"SPECIFICATION\": \"\",\n" +
                "      \"ACTIVE\": \"\",\n" +
                "      \"QUALITY_CONTROL\": \"Y\",\n" +
                "      \"NETTO_WEIGHT\": \"\",\n" +
                "      \"BRUTTO_WEIGHT\": 0.04,\n" +
                "      \"VOLUME\": \"\",\n" +
                "      \"SHELF_LIFE\": 30,\n" +
                "      \"FREQUENCY\": \"\",\n" +
                "      \"LOT\": \"Y\",\n" +
                "      \"BBDATE\": \"Y\",\n" +
                "      \"SERIAL\": \"\",\n" +
                "      \"WRAPPING\": \"\",\n" +
                "      \"LU_TYPE\": \"\",\n" +
                "      \"TEMPERATURE_REGIME\": \"\",\n" +
                "      \"PACKINGS\": {\n" +
                "        \"PACKING\": [\n" +
                "          {\n" +
                "            \"ITEM_ID\": 307060137,\n" +
                "            \"EANPACK\": 14810268055482,\n" +
                "            \"PARENT_QUANTITY\": 18,\n" +
                "            \"LU_QUANTITY\": \"315\",\n" +
                "            \"NAME\": \"\",\n" +
                "            \"DESCRIPTION\": \"\",\n" +
                "            \"WIDTH\": \"\",\n" +
                "            \"LENGTH\": \"\",\n" +
                "            \"HEIGHT\": \"\",\n" +
                "            \"NET_WEIGHT\": \"\",\n" +
                "            \"GROSS_WEIGHT\": \"\",\n" +
                "            \"VOLUME\": \"\",\n" +
                "            \"ZIP\": \"\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"ITEM_ID\": 307060137,\n" +
                "            \"EANPACK\": 24810268055489,\n" +
                "            \"PARENT_QUANTITY\": 18,\n" +
                "            \"LU_QUANTITY\": 315,\n" +
                "            \"NAME\": \"\",\n" +
                "            \"DESCRIPTION\": \"\",\n" +
                "            \"WIDTH\": \"\",\n" +
                "            \"LENGTH\": \"\",\n" +
                "            \"HEIGHT\": \"\",\n" +
                "            \"NET_WEIGHT\": \"\",\n" +
                "            \"GROSS_WEIGHT\": \"\",\n" +
                "            \"VOLUME\": \"\",\n" +
                "            \"ZIP\": \"\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String expStr1 = "{\n" +
                "  \"MSGID\" : 161,\n" +
                "  \"MSGTYPE\" : \"ITEM\",\n" +
                "  \"REPLYTO\" : \"\",\n" +
                "  \"TIMESTAMP\" : 20240815152950,\n" +
                "  \"FACILITY\" : \"NAS\",\n" +
                "  \"ACTION\" : \"SET\",\n" +
                "  \"SENDER\" : \"SERVER\",\n" +
                "  \"RECIEVER\" : \"NAS\",\n" +
                "  \"ITEM\" : {\n" +
                "    \"ITEM_ID\" : 307060137,\n" +
                "    \"ITEM_REF\" : 3937,\n" +
                "    \"SKU_UOM\" : \"\",\n" +
                "    \"BASIC_UOM\" : \"PCE\",\n" +
                "    \"SKU_BASIC_QUANTITY\" : \"\",\n" +
                "    \"NAME\" : \"Сырок гл. МАКОВКА 20% 40г\",\n" +
                "    \"EAN\" : 4810268055485,\n" +
                "    \"CATEGORY\" : 1803090000,\n" +
                "    \"CATEGORY_NAME\" : \"конфетно-десертная линейка\",\n" +
                "    \"NAS\" : \"\",\n" +
                "    \"DESCRIPTION\" : \"Сырок творожный глазированный \\\"Маковка\\\" массовой долей жира 20,0 %, ФЛОУПАК 40 г\",\n" +
                "    \"SPECIFICATION\" : \"\",\n" +
                "    \"ACTIVE\" : \"\",\n" +
                "    \"QUALITY_CONTROL\" : \"Y\",\n" +
                "    \"NETTO_WEIGHT\" : \"\",\n" +
                "    \"BRUTTO_WEIGHT\" : 0.04,\n" +
                "    \"VOLUME\" : \"\",\n" +
                "    \"SHELF_LIFE\" : 30,\n" +
                "    \"FREQUENCY\" : \"\",\n" +
                "    \"LOT\" : \"Y\",\n" +
                "    \"BBDATE\" : \"Y\",\n" +
                "    \"SERIAL\" : \"\",\n" +
                "    \"WRAPPING\" : \"\",\n" +
                "    \"LU_TYPE\" : \"\",\n" +
                "    \"TEMPERATURE_REGIME\" : \"\",\n" +
                "    \"PACKINGS\" : {\n" +
                "      \"PACKING\" : [ {\n" +
                "        \"ITEM_ID\" : 307060137,\n" +
                "        \"EANPACK\" : 14810268055482,\n" +
                "        \"PARENT_QUANTITY\" : 18,\n" +
                "        \"LU_QUANTITY\" : \"315\",\n" +
                "        \"NAME\" : \"\",\n" +
                "        \"DESCRIPTION\" : \"\",\n" +
                "        \"WIDTH\" : \"\",\n" +
                "        \"LENGTH\" : \"\",\n" +
                "        \"HEIGHT\" : \"\",\n" +
                "        \"NET_WEIGHT\" : \"\",\n" +
                "        \"GROSS_WEIGHT\" : \"\",\n" +
                "        \"VOLUME\" : \"\",\n" +
                "        \"ZIP\" : \"\"\n" +
                "      }, {\n" +
                "        \"ITEM_ID\" : 307060137,\n" +
                "        \"EANPACK\" : 24810268055489,\n" +
                "        \"PARENT_QUANTITY\" : 18,\n" +
                "        \"LU_QUANTITY\" : 315,\n" +
                "        \"NAME\" : \"\",\n" +
                "        \"DESCRIPTION\" : \"\",\n" +
                "        \"WIDTH\" : \"\",\n" +
                "        \"LENGTH\" : \"\",\n" +
                "        \"HEIGHT\" : \"\",\n" +
                "        \"NET_WEIGHT\" : \"\",\n" +
                "        \"GROSS_WEIGHT\" : \"\",\n" +
                "        \"VOLUME\" : \"\",\n" +
                "        \"ZIP\" : \"\"\n" +
                "      } ]\n" +
                "    }\n" +
                "  }\n" +
                "}";


        Map<String, String> documentsForSend = new HashMap<>();

        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(1, documentsForSend.size());

        var expJson1 = objectMapper.readTree(expStr1);
        var actualJson1 = objectMapper.readTree(documentsForSend.get("NAS"));
        assertEquals(expJson1, actualJson1);
    }

    @Test
    public void testProcessMsgByContentType5() throws Exception {

        String msg = "{\n" +
                "  \"MESSAGE\": {\n" +
                "    \"MSGID\": 161,\n" +
                "    \"MSGTYPE\": \"ITEM\",\n" +
                "    \"REPLYTO\": \"\",\n" +
                "    \"TIMESTAMP\": 20240815152950,\n" +
                "    \"FACILITY\": \"TEST19\",\n" +
                "    \"ACTION\": \"SET\",\n" +
                "    \"SENDER\": \"HOST\",\n" +
                "    \"RECIEVER\": \"TEST19\",\n" +
                "    \"ITEM\": {\n" +
                "      \"ITEM_ID\": 307060137,\n" +
                "      \"ITEM_REF\": 3937,\n" +
                "      \"SKU_UOM\": \"\",\n" +
                "      \"BASIC_UOM\": \"PCE\",\n" +
                "      \"SKU_BASIC_QUANTITY\": \"\",\n" +
                "      \"NAME\": \"Сырок гл. МАКОВКА 20% 40г\",\n" +
                "      \"EAN\": 4810268055485,\n" +
                "      \"CATEGORY\": 1803090000,\n" +
                "      \"CATEGORY_NAME\": \"конфетно-десертная линейка\",\n" +
                "      \"NAS\": \"\",\n" +
                "      \"DESCRIPTION\": \"Сырок творожный глазированный \\\"Маковка\\\" массовой долей жира 20,0 %, ФЛОУПАК 40 г\",\n" +
                "      \"SPECIFICATION\": \"\",\n" +
                "      \"ACTIVE\": \"\",\n" +
                "      \"QUALITY_CONTROL\": \"Y\",\n" +
                "      \"NETTO_WEIGHT\": \"\",\n" +
                "      \"BRUTTO_WEIGHT\": 0.04,\n" +
                "      \"VOLUME\": \"\",\n" +
//                "      \"SHELF_LIFE\": 30,\n" +
                "      \"SHELF_LIFE\": \"TEST\",\n" +
                "      \"FREQUENCY\": \"\",\n" +
//                "      \"LOT\": \"Y\",\n" +
                "      \"LOT\": \"YES\",\n" +
                "      \"BBDATE\": \"Y\",\n" +
                "      \"SERIAL\": \"\",\n" +
                "      \"WRAPPING\": \"\",\n" +
                "      \"LU_TYPE\": \"\",\n" +
                "      \"TEMPERATURE_REGIME\": \"\",\n" +
                "      \"PACKINGS\": {\n" +
                "        \"PACKING\": [\n" +
                "          {\n" +
                "            \"ITEM_ID\": 307060137,\n" +
//                "            \"EANPACK\": 14810268055482,\n" +
                "            \"PARENT_QUANTITY\": 18,\n" +
                "            \"LU_QUANTITY\": \"315\",\n" +
                "            \"NAME\": \"\",\n" +
                "            \"DESCRIPTION\": \"\",\n" +
                "            \"WIDTH\": \"\",\n" +
                "            \"LENGTH\": \"\",\n" +
                "            \"HEIGHT\": \"\",\n" +
                "            \"NET_WEIGHT\": \"\",\n" +
                "            \"GROSS_WEIGHT\": \"\",\n" +
                "            \"VOLUME\": \"\",\n" +
                "            \"ZIP\": \"\"\n" +
                "          },\n" +
                "          {\n" +
//                "            \"ITEM_ID\": 307060137,\n" +
                "            \"EANPACK\": 24810268055489,\n" +
//                "            \"PARENT_QUANTITY\": 18,\n" +
                "            \"PARENT_QUANTITY\": \"TEST\",\n" +
                "            \"LU_QUANTITY\": 315,\n" +
                "            \"NAME\": \"\",\n" +
                "            \"DESCRIPTION\": \"\",\n" +
                "            \"WIDTH\": \"\",\n" +
                "            \"LENGTH\": \"\",\n" +
                "            \"HEIGHT\": \"\",\n" +
                "            \"NET_WEIGHT\": \"\",\n" +
                "            \"GROSS_WEIGHT\": \"\",\n" +
                "            \"VOLUME\": \"\",\n" +
                "            \"ZIP\": \"\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String expStr1 = "{\n" +
                "  \"MSGID\" : 161,\n" +
                "  \"MSGTYPE\" : \"ITEM\",\n" +
                "  \"REPLYTO\" : 161,\n" +
                "  \"TIMESTAMP\" : 20240815152950,\n" +
                "  \"FACILITY\" : \"TEST19\",\n" +
                "  \"ACTION\" : \"SET\",\n" +
                "  \"SENDER\" : \"HOST\",\n" +
                "  \"RECIEVER\" : \"TEST19\",\n" +
                "  \"SYSSTAT\" : {\n" +
                "    \"ERROR_CODE\" : 400,\n" +
                "    \"DESCRIPTION\" : \"Поле SHELF_LIFE несоответствие типу, Поле LOT несоответствие типу," +
                " Поле EANPACK отсутствует, Поле PARENT_QUANTITY несоответствие типу, Поле ITEM_ID отсутствует\"\n" +
                "  }\n" +
                "}";


        Map<String, String> documentsForSend = new HashMap<>();

        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(1, documentsForSend.size());

        var expJson1 = objectMapper.readTree(expStr1);
        var actualJson1 = objectMapper.readTree(documentsForSend.get("HOST"));
        assertEquals(expJson1, actualJson1);
    }




}
