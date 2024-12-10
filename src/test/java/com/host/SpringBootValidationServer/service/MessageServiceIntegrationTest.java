package com.host.SpringBootValidationServer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.host.SpringBootValidationServer.SpringBootValidationServerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

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


    @Test
    public void testProcessJsonMsg1() throws Exception {

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

        var actualStr1 = documentsForSend.get("TEST2");
        JSONAssert.assertEquals(expStr1, actualStr1, false);// Сравнение JSON (true - строгая проверка, false - игнорирование порядка полей)

        var actualStr2 = documentsForSend.get("TEST1");
        JSONAssert.assertEquals(expStr2, actualStr2, false);
    }

    @Test
    public void testProcessJsonMsg2() throws Exception {

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

        var actualStr1 = documentsForSend.get("HOST");
        JSONAssert.assertEquals(expStr1, actualStr1, false);
    }

    @Test
    public void testProcessJsonMsg3() throws Exception {

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

        var actualStr1 = documentsForSend.get("HOST");
        JSONAssert.assertEquals(expStr1, actualStr1, false);
    }

    @Test
    public void testProcessJsonMsg4() throws Exception {

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

        var actualStr1 = documentsForSend.get("NAS");
        JSONAssert.assertEquals(expStr1, actualStr1, false);
    }

    @Test
    public void testProcessJsonMsg5() throws Exception {

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

        var actualStr1 = documentsForSend.get("HOST");
        JSONAssert.assertEquals(expStr1, actualStr1, false);
    }

    @Test
    public void testProcessXmlMsg1() {

        String msg = "<MESSAGE>\n" +
                "<MSGID>3385</MSGID>\n" +
                "<MSGTYPE>SYSSTAT</MSGTYPE>\n" +
                "<REPLYTO></REPLYTO>\n" +
                "<TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "<FACILITY>TEST1</FACILITY>\n" +
                "<ACTION>SET</ACTION>\n" +
                "<SENDER>HOST</SENDER>\n" +
                "<RECIEVER>TEST1</RECIEVER>\n" +
                "<SYSSTAT>\n" +
                "   <ERROR_CODE>0</ERROR_CODE>\n" +
                "   <DESCRIPTION>ok</DESCRIPTION>\n" +
                "</SYSSTAT>\n" +
                "</MESSAGE>";

        String expStr1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>3385</MSGID>\n" +
                "    <MSGTYPE>SYSSTAT</MSGTYPE>\n" +
                "    <REPLYTO/>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>TEST1</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>SERVER</SENDER>\n" +
                "    <RECIEVER>TEST2</RECIEVER>\n" +
                "    <SYSSTAT>\n" +
                "        <ERROR_CODE>0</ERROR_CODE>\n" +
                "        <DESCRIPTION>ok</DESCRIPTION>\n" +
                "    </SYSSTAT>\n" +
                "</MESSAGE>";

        String expStr2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>3385</MSGID>\n" +
                "    <MSGTYPE>SYSSTAT</MSGTYPE>\n" +
                "    <REPLYTO/>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>TEST1</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>SERVER</SENDER>\n" +
                "    <RECIEVER>TEST1</RECIEVER>\n" +
                "    <SYSSTAT>\n" +
                "        <ERROR_CODE>0</ERROR_CODE>\n" +
                "        <DESCRIPTION>ok</DESCRIPTION>\n" +
                "    </SYSSTAT>\n" +
                "</MESSAGE>";


        Map<String, String> documentsForSend = new HashMap<>();

        //подменяем метод sendDocuments, чтобы он ничего не делал
        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(2, documentsForSend.size());

        var actualStr1 = documentsForSend.get("TEST2");
        Diff diff1 = DiffBuilder.compare(expStr1)
                .withTest(actualStr1)
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertFalse(diff1.hasDifferences(), "XML должны быть идентичны");

        var actualStr2 = documentsForSend.get("TEST1");

        Diff diff2 = DiffBuilder.compare(expStr2)
                .withTest(actualStr2)
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertFalse(diff2.hasDifferences(), "XML должны быть идентичны");
    }

    @Test
    public void testProcessXmlMsg2() {

        String msg = "<MESSAGE>\n" +
                "<MSGID>3385</MSGID>\n" +
                "<MSGTYPE>SYSSTAT</MSGTYPE>\n" +
                "<REPLYTO></REPLYTO>\n" +
                "<TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "<FACILITY>TEST1</FACILITY>\n" +
                "<ACTION>SET</ACTION>\n" +
                "<SENDER>HOST</SENDER>\n" +
                "<RECIEVER>TEST1</RECIEVER>\n" +
                "<SYSSTAT>\n" +
//                "   <ERROR_CODE>0</ERROR_CODE>\n" +
                "   <DESCRIPTION>ok</DESCRIPTION>\n" +
                "</SYSSTAT>\n" +
                "</MESSAGE>";

        String expStr1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>3385</MSGID>\n" +
                "    <MSGTYPE>SYSSTAT</MSGTYPE>\n" +
                "    <REPLYTO>3385</REPLYTO>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>TEST1</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>HOST</SENDER>\n" +
                "    <RECIEVER>TEST1</RECIEVER>\n" +
                "    <SYSSTAT>\n" +
                "        <ERROR_CODE>400</ERROR_CODE>\n" +
                "        <DESCRIPTION>Поле ERROR_CODE отсутствует</DESCRIPTION>\n" +
                "    </SYSSTAT>\n" +
                "</MESSAGE>";


        Map<String, String> documentsForSend = new HashMap<>();

        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(1, documentsForSend.size());

        var actualStr1 = documentsForSend.get("HOST");

        Diff diff1 = DiffBuilder.compare(expStr1)
                .withTest(actualStr1)
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertFalse(diff1.hasDifferences(), "XML должны быть идентичны");
    }

    @Test
    public void testProcessXmlMsg3() {

        String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>161</MSGID>\n" +
                "    <MSGTYPE>ITEM</MSGTYPE>\n" +
                "    <REPLYTO></REPLYTO>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>TEST19</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>HOST</SENDER>\n" +
                "    <RECIEVER>TEST19</RECIEVER>\n" +
                "    <ITEM>\n" +
                "        <ITEM_ID>0307060137</ITEM_ID>\n" +
                "        <ITEM_REF>3937</ITEM_REF>\n" +
                "        <SKU_UOM/>\n" +
                "        <BASIC_UOM>PCE</BASIC_UOM>\n" +
                "        <SKU_BASIC_QUANTITY/>\n" +
                "        <NAME>Сырок гл. МАКОВКА 20% 40г</NAME>\n" +
                "        <EAN>4810268055485</EAN>\n" +
                "        <CATEGORY>1803090000</CATEGORY>\n" +
                "        <CATEGORY_NAME>конфетно-десертная линейка</CATEGORY_NAME>\n" +
                "        <NAS/>\n" +
                "        <DESCRIPTION>Сырок творожный глазированный \"Маковка\" массовой долей жира 20,0 %, ФЛОУПАК 40 г</DESCRIPTION>\n" +
                "        <SPECIFICATION/>\n" +
                "        <ACTIVE/>\n" +
                "        <QUALITY_CONTROL>Y</QUALITY_CONTROL>\n" +
                "        <NETTO_WEIGHT/>\n" +
                "        <BRUTTO_WEIGHT>0.04</BRUTTO_WEIGHT>>\n" +
                "        <VOLUME/>\n" +
                "        <SHELF_LIFE>30</SHELF_LIFE>\n" +
                "        <FREQUENCY/>\n" +
                "        <LOT>Y</LOT>\n" +
                "        <BBDATE>Y</BBDATE>\n" +
                "        <SERIAL/>\n" +
                "        <WRAPPING/>\n" +
                "        <LU_TYPE/>\n" +
                "        <TEMPERATURE_REGIME/>\n" +
                "        <PACKINGS>\n" +
                "            <PACKING>\n" +
                "                <ITEM_ID>0307060137</ITEM_ID>\n" +
                "                <EANPACK>14810268055482</EANPACK>\n" +
                "                <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
                "                <LU_QUANTITY>315</LU_QUANTITY>\n" +
                "                <NAME/>\n" +
                "                <DESCRIPTION/>\n" +
                "                <WIDTH/>\n" +
                "                <LENGTH/>\n" +
                "                <HEIGHT/>\n" +
                "                <NET_WEIGHT/>\n" +
                "                <GROSS_WEIGHT/>\n" +
                "                <VOLUME/>\n" +
                "                <ZIP/>\n" +
                "            </PACKING>\n" +
                "            <PACKING>\n" +
                "                <ITEM_ID>0307060137</ITEM_ID>\n" +
                "                <EANPACK>24810268055489</EANPACK>\n" +
                "                <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
                "                <LU_QUANTITY>315</LU_QUANTITY>\n" +
                "                <NAME/>\n" +
                "                <DESCRIPTION/>\n" +
                "                <WIDTH/>\n" +
                "                <LENGTH/>\n" +
                "                <HEIGHT/>\n" +
                "                <NET_WEIGHT/>\n" +
                "                <GROSS_WEIGHT/>\n" +
                "                <VOLUME/>\n" +
                "                <ZIP/>\n" +
                "            </PACKING>\n" +
                "        </PACKINGS>\n" +
                "    </ITEM>\n" +
                "</MESSAGE>";

        String expStr1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>161</MSGID>\n" +
                "    <MSGTYPE>ITEM</MSGTYPE>\n" +
                "    <REPLYTO>161</REPLYTO>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>TEST19</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>HOST</SENDER>\n" +
                "    <RECIEVER>TEST19</RECIEVER>\n" +
                "    <SYSSTAT>\n" +
                "        <ERROR_CODE>400</ERROR_CODE>\n" +
                "        <DESCRIPTION>Cообщение не прописано в правилах маршрутизации!</DESCRIPTION>\n" +
                "    </SYSSTAT>\n" +
                "</MESSAGE>";


        Map<String, String> documentsForSend = new HashMap<>();

        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(1, documentsForSend.size());

        var actualStr1 = documentsForSend.get("HOST");

        Diff diff1 = DiffBuilder.compare(expStr1)
                .withTest(actualStr1)
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertFalse(diff1.hasDifferences(), "XML должны быть идентичны");
    }

    @Test
    public void testProcessXmlMsg4() {

        String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>161</MSGID>\n" +
                "    <MSGTYPE>ITEM</MSGTYPE>\n" +
                "    <REPLYTO></REPLYTO>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>NAS</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>HOST</SENDER>\n" +
                "    <RECIEVER>HOST</RECIEVER>\n" +
                "    <ITEM>\n" +
                "        <ITEM_ID>0307060137</ITEM_ID>\n" +
                "        <ITEM_REF>3937</ITEM_REF>\n" +
                "        <SKU_UOM/>\n" +
                "        <BASIC_UOM>PCE</BASIC_UOM>\n" +
                "        <SKU_BASIC_QUANTITY/>\n" +
                "        <NAME>Сырок гл. МАКОВКА 20% 40г</NAME>\n" +
                "        <EAN>4810268055485</EAN>\n" +
                "        <CATEGORY>1803090000</CATEGORY>\n" +
                "        <CATEGORY_NAME>конфетно-десертная линейка</CATEGORY_NAME>\n" +
                "        <NAS/>\n" +
                "        <DESCRIPTION>Сырок творожный глазированный \"Маковка\" массовой долей жира 20,0 %, ФЛОУПАК 40 г</DESCRIPTION>\n" +
                "        <SPECIFICATION/>\n" +
                "        <ACTIVE/>\n" +
                "        <QUALITY_CONTROL>Y</QUALITY_CONTROL>\n" +
                "        <NETTO_WEIGHT/>\n" +
                "        <BRUTTO_WEIGHT>0.04</BRUTTO_WEIGHT>>\n" +
                "        <VOLUME/>\n" +
                "        <SHELF_LIFE>30</SHELF_LIFE>\n" +
                "        <FREQUENCY/>\n" +
                "        <LOT>Y</LOT>\n" +
                "        <BBDATE>Y</BBDATE>\n" +
                "        <SERIAL/>\n" +
                "        <WRAPPING/>\n" +
                "        <LU_TYPE/>\n" +
                "        <TEMPERATURE_REGIME/>\n" +
                "        <PACKINGS>\n" +
                "            <PACKING>\n" +
                "                <ITEM_ID>0307060137</ITEM_ID>\n" +
                "                <EANPACK>14810268055482</EANPACK>\n" +
                "                <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
                "                <LU_QUANTITY>315</LU_QUANTITY>\n" +
                "                <NAME/>\n" +
                "                <DESCRIPTION/>\n" +
                "                <WIDTH/>\n" +
                "                <LENGTH/>\n" +
                "                <HEIGHT/>\n" +
                "                <NET_WEIGHT/>\n" +
                "                <GROSS_WEIGHT/>\n" +
                "                <VOLUME/>\n" +
                "                <ZIP/>\n" +
                "            </PACKING>\n" +
                "            <PACKING>\n" +
                "                <ITEM_ID>0307060137</ITEM_ID>\n" +
                "                <EANPACK>24810268055489</EANPACK>\n" +
                "                <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
                "                <LU_QUANTITY>315</LU_QUANTITY>\n" +
                "                <NAME/>\n" +
                "                <DESCRIPTION/>\n" +
                "                <WIDTH/>\n" +
                "                <LENGTH/>\n" +
                "                <HEIGHT/>\n" +
                "                <NET_WEIGHT/>\n" +
                "                <GROSS_WEIGHT/>\n" +
                "                <VOLUME/>\n" +
                "                <ZIP/>\n" +
                "            </PACKING>\n" +
                "        </PACKINGS>\n" +
                "    </ITEM>\n" +
                "</MESSAGE>";

        String expStr1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>161</MSGID>\n" +
                "    <MSGTYPE>ITEM</MSGTYPE>\n" +
                "    <REPLYTO/>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>NAS</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>SERVER</SENDER>\n" +
                "    <RECIEVER>NAS</RECIEVER>\n" +
                "    <ITEM>\n" +
                "        <ITEM_ID>0307060137</ITEM_ID>\n" +
                "        <ITEM_REF>3937</ITEM_REF>\n" +
                "        <SKU_UOM/>\n" +
                "        <BASIC_UOM>PCE</BASIC_UOM>\n" +
                "        <SKU_BASIC_QUANTITY/>\n" +
                "        <NAME>Сырок гл. МАКОВКА 20% 40г</NAME>\n" +
                "        <EAN>4810268055485</EAN>\n" +
                "        <CATEGORY>1803090000</CATEGORY>\n" +
                "        <CATEGORY_NAME>конфетно-десертная линейка</CATEGORY_NAME>\n" +
                "        <NAS/>\n" +
                "        <DESCRIPTION>Сырок творожный глазированный \"Маковка\" массовой долей жира 20,0 %, ФЛОУПАК 40 г</DESCRIPTION>\n" +
                "        <SPECIFICATION/>\n" +
                "        <ACTIVE/>\n" +
                "        <QUALITY_CONTROL>Y</QUALITY_CONTROL>\n" +
                "        <NETTO_WEIGHT/>\n" +
                "        <BRUTTO_WEIGHT>0.04</BRUTTO_WEIGHT>\n" +
                "        &gt;\n" +
                "        <VOLUME/>\n" +
                "        <SHELF_LIFE>30</SHELF_LIFE>\n" +
                "        <FREQUENCY/>\n" +
                "        <LOT>Y</LOT>\n" +
                "        <BBDATE>Y</BBDATE>\n" +
                "        <SERIAL/>\n" +
                "        <WRAPPING/>\n" +
                "        <LU_TYPE/>\n" +
                "        <TEMPERATURE_REGIME/>\n" +
                "        <PACKINGS>\n" +
                "            <PACKING>\n" +
                "                <ITEM_ID>0307060137</ITEM_ID>\n" +
                "                <EANPACK>14810268055482</EANPACK>\n" +
                "                <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
                "                <LU_QUANTITY>315</LU_QUANTITY>\n" +
                "                <NAME/>\n" +
                "                <DESCRIPTION/>\n" +
                "                <WIDTH/>\n" +
                "                <LENGTH/>\n" +
                "                <HEIGHT/>\n" +
                "                <NET_WEIGHT/>\n" +
                "                <GROSS_WEIGHT/>\n" +
                "                <VOLUME/>\n" +
                "                <ZIP/>\n" +
                "            </PACKING>\n" +
                "            <PACKING>\n" +
                "                <ITEM_ID>0307060137</ITEM_ID>\n" +
                "                <EANPACK>24810268055489</EANPACK>\n" +
                "                <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
                "                <LU_QUANTITY>315</LU_QUANTITY>\n" +
                "                <NAME/>\n" +
                "                <DESCRIPTION/>\n" +
                "                <WIDTH/>\n" +
                "                <LENGTH/>\n" +
                "                <HEIGHT/>\n" +
                "                <NET_WEIGHT/>\n" +
                "                <GROSS_WEIGHT/>\n" +
                "                <VOLUME/>\n" +
                "                <ZIP/>\n" +
                "            </PACKING>\n" +
                "        </PACKINGS>\n" +
                "    </ITEM>\n" +
                "</MESSAGE>";


        Map<String, String> documentsForSend = new HashMap<>();

        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(1, documentsForSend.size());

        var actualStr1 = documentsForSend.get("NAS");

        Diff diff1 = DiffBuilder.compare(expStr1)
                .withTest(actualStr1)
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertFalse(diff1.hasDifferences(), "XML должны быть идентичны");
    }

    @Test
    public void testProcessXmlMsg5() {

        String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>161</MSGID>\n" +
                "    <MSGTYPE>ITEM</MSGTYPE>\n" +
                "    <REPLYTO></REPLYTO>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>TEST19</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>HOST</SENDER>\n" +
                "    <RECIEVER>TEST19</RECIEVER>\n" +
                "    <ITEM>\n" +
                "        <ITEM_ID>0307060137</ITEM_ID>\n" +
                "        <ITEM_REF>3937</ITEM_REF>\n" +
                "        <SKU_UOM/>\n" +
                "        <BASIC_UOM>PCE</BASIC_UOM>\n" +
                "        <SKU_BASIC_QUANTITY/>\n" +
                "        <NAME>Сырок гл. МАКОВКА 20% 40г</NAME>\n" +
                "        <EAN>4810268055485</EAN>\n" +
                "        <CATEGORY>1803090000</CATEGORY>\n" +
                "        <CATEGORY_NAME>конфетно-десертная линейка</CATEGORY_NAME>\n" +
                "        <NAS/>\n" +
                "        <DESCRIPTION>Сырок творожный глазированный \"Маковка\" массовой долей жира 20,0 %, ФЛОУПАК 40 г</DESCRIPTION>\n" +
                "        <SPECIFICATION/>\n" +
                "        <ACTIVE/>\n" +
                "        <QUALITY_CONTROL>Y</QUALITY_CONTROL>\n" +
                "        <NETTO_WEIGHT/>\n" +
                "        <BRUTTO_WEIGHT>0.04</BRUTTO_WEIGHT>>\n" +
                "        <VOLUME/>\n" +
//                "        <SHELF_LIFE>30</SHELF_LIFE>\n" +
                "        <SHELF_LIFE>TEST</SHELF_LIFE>\n" +
                "        <FREQUENCY/>\n" +
//                "        <LOT>Y</LOT>\n" +
                "        <LOT>YES</LOT>\n" +
                "        <BBDATE>Y</BBDATE>\n" +
                "        <SERIAL/>\n" +
                "        <WRAPPING/>\n" +
                "        <LU_TYPE/>\n" +
                "        <TEMPERATURE_REGIME/>\n" +
                "        <PACKINGS>\n" +
                "            <PACKING>\n" +
                "                <ITEM_ID>0307060137</ITEM_ID>\n" +
//                "                <EANPACK>14810268055482</EANPACK>\n" +
                "                <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
                "                <LU_QUANTITY>315</LU_QUANTITY>\n" +
                "                <NAME/>\n" +
                "                <DESCRIPTION/>\n" +
                "                <WIDTH/>\n" +
                "                <LENGTH/>\n" +
                "                <HEIGHT/>\n" +
                "                <NET_WEIGHT/>\n" +
                "                <GROSS_WEIGHT/>\n" +
                "                <VOLUME/>\n" +
                "                <ZIP/>\n" +
                "            </PACKING>\n" +
                "            <PACKING>\n" +
//                "                <ITEM_ID>0307060137</ITEM_ID>\n" +
                "                <EANPACK>24810268055489</EANPACK>\n" +
//                "                <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
                "                <PARENT_QUANTITY>TEST</PARENT_QUANTITY>\n" +
                "                <LU_QUANTITY>315</LU_QUANTITY>\n" +
                "                <NAME/>\n" +
                "                <DESCRIPTION/>\n" +
                "                <WIDTH/>\n" +
                "                <LENGTH/>\n" +
                "                <HEIGHT/>\n" +
                "                <NET_WEIGHT/>\n" +
                "                <GROSS_WEIGHT/>\n" +
                "                <VOLUME/>\n" +
                "                <ZIP/>\n" +
                "            </PACKING>\n" +
                "        </PACKINGS>\n" +
                "    </ITEM>\n" +
                "</MESSAGE>";

        String expStr1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<MESSAGE>\n" +
                "    <MSGID>161</MSGID>\n" +
                "    <MSGTYPE>ITEM</MSGTYPE>\n" +
                "    <REPLYTO>161</REPLYTO>\n" +
                "    <TIMESTAMP>20240815152950</TIMESTAMP>\n" +
                "    <FACILITY>TEST19</FACILITY>\n" +
                "    <ACTION>SET</ACTION>\n" +
                "    <SENDER>HOST</SENDER>\n" +
                "    <RECIEVER>TEST19</RECIEVER>\n" +
                "    <SYSSTAT>\n" +
                "        <ERROR_CODE>400</ERROR_CODE>\n" +
                "        <DESCRIPTION>Поле SHELF_LIFE несоответствие типу, Поле LOT несоответствие типу, Поле EANPACK отсутствует, Поле PARENT_QUANTITY несоответствие типу, Поле ITEM_ID отсутствует</DESCRIPTION>\n" +
                "    </SYSSTAT>\n" +
                "</MESSAGE>";


        Map<String, String> documentsForSend = new HashMap<>();

        doNothing().when(routingService).sendDocuments(any(),any());

        messageService.processMsgByContentType(msg, documentsForSend);

        assertEquals(1, documentsForSend.size());

        var actualStr1 = documentsForSend.get("HOST");

        Diff diff1 = DiffBuilder.compare(expStr1)
                .withTest(actualStr1)
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        assertFalse(diff1.hasDifferences(), "XML должны быть идентичны");
    }




}
