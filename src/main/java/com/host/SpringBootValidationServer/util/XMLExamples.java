package com.host.SpringBootValidationServer.util;

public class XMLExamples {

    public static String SYSSTAT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<MESSAGE>"
            + "<MSGID>777</MSGID>"
            + "<MSGTYPE>SYSSTAT</MSGTYPE>"
            + "<REPLYTO></REPLYTO>"
            + "<TIMESTAMP>20240516115447</TIMESTAMP>"
            + "<FACILITY>NAS</FACILITY>"
            + "<ACTION>SET</ACTION>"
            + "<SENDER>HOST</SENDER>"
            + "<RECIEVER>NAS</RECIEVER>"
            + "<SYSSTAT>"
            + "<ERROR_CODE>200</ERROR_CODE>"
            + "<DESCRIPTION>ok</DESCRIPTION>"
            + "</SYSSTAT>"
//            + "<SYSSTAT>"
//            + "<ERROR_CODE>20000000000000000000000000000000</ERROR_CODE>"
//            + "<DESCRIPTION>ok</DESCRIPTION>"
//            + "</SYSSTAT>"
            + "</MESSAGE>";

    public static String ITEM_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<MESSAGE>\n" +
            "    <MSGID>888</MSGID>\n" +
            "    <MSGTYPE>ITEM</MSGTYPE>\n" +
            "    <REPLYTO></REPLYTO>\n" +
            "    <TIMESTAMP>20240516114447</TIMESTAMP>\n" +
            "    <FACILITY>NAS</FACILITY>\n" +
            "    <ACTION>SET</ACTION>\n" +
            "    <SENDER>HOST</SENDER>\n" +
            "    <RECIEVER>NAS</RECIEVER>\n" +
            "    <ITEM>\n" +
            "        <ITEM_ID>0204210168</ITEM_ID>\n" +
            "        <BASIC_UOM>PCE</BASIC_UOM>\n" +
            "        <SKU_BASIC_QUANTITY/>\n" +
            "        <NAME>Сырп/твБЛфин45%фас.н-бр.200г</NAME>\n" +
            "        <EAN>4810268035258</EAN>\n" +
            "        <CATEGORY>0201010000</CATEGORY>\n" +
            "        <CATEGORY_NAME>Категории и сегменты</CATEGORY_NAME>\n" +
//            "        <NAS/>\n" +
            "        <DESCRIPTION>Сыр полутвердый \"Брест-Литовск финский\" массовой долей жира в сухом веществе 45 % фасованный (нарезка-брусок) 200 г</DESCRIPTION>\n" +
//            "        <SPECIFICATION/>\n" +
//            "        <ACTIVE/>\n" +
//            "        <QUALITY_CONTROL/>\n" +
//            "        <NETTO_WEIGHT/>\n" +
            "        <BRUTTO_WEIGHT>0.2</BRUTTO_WEIGHT>\n" +
//            "        <VOLUME/>\n" +
//            "        <SHELF_LIFE/>\n" +
//            "        <FREQUENCY/>\n" +
//            "        <LOT/>\n" +
//            "        <BBDATE/>\n" +
//            "        <SERIAL/>\n" +
//            "        <WRAPPING/>\n" +
//            "        <LU_TYPE/>\n" +
//            "        <PACKINGS/>\n" +
//            "        <TEMPERATURE_REGIME/>\n" +
            "  <PACKINGS>\n" +
            "      <PACKING>\n" +
            "         <ITEM_ID>0204210168</ITEM_ID>\n" +
            "         <EANPACK>0123456789123</EANPACK >\n" +
            "         <PARENT_QUANTITY>10</PARENT_QUANTITY>\n" +
            "         <LU_QUANTITY>100</LU_QUANTITY>\n" +
            "         <NAME/>\n" +
            "      </PACKING>\n" +
//
            " </PACKINGS>\n" +

            "    </ITEM>\n" +
            "</MESSAGE>";

    public static String TEST1_XML = "" +
            "<MESSAGE>\n" +
            "<MSGID>3385</MSGID>\n" +
            "<MSGTYPE>SYSSTAT</MSGTYPE>\n" +
            "<REPLYTO></REPLYTO>\n" +
            "<TIMESTAMP>20240815152950</TIMESTAMP>\n" +
            "<FACILITY>TEST1</FACILITY>\n" +
            "<ACTION>SET</ACTION>\n" +
            "<SENDER>HOST</SENDER>\n" +
            "<RECIEVER>TEST1</RECIEVER>\n" +
            "<SYSSTAT>\n" +
            "<ERROR_CODE>0</ERROR_CODE>\n" +
            "<DESCRIPTION>ok</DESCRIPTION>\n" +
            "</SYSSTAT>\n" +
            "</MESSAGE>";

    public static String PACKING_XML = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "     <MESSAGE>\n" +
            "       <MSGID>161</MSGID>\n" +
            "       <MSGTYPE>ITEM</MSGTYPE>\n" +
            "       <REPLYTO></REPLYTO>\n" +
            "       <TIMESTAMP>2024-05-15 15:29:41</TIMESTAMP>\n" +
            "       <FACILITY></FACILITY>\n" +
            "       <ACTION>SET</ACTION>\n" +
            "       <SENDER>HOST</SENDER>\n" +
            "       <RECIEVER>NAS</RECIEVER>\n" +
            "         <ITEM>\n" +
            "           <ITEM_ID>0307060137</ITEM_ID>\n" +
            "           <ITEM_REF>3937</ITEM_REF>\n" +
            "           <SKU_UOM/>\n" +
            "           <BASIC_UOM>PCE</BASIC_UOM>\n" +
            "           <SKU_BASIC_QUANTITY/>\n" +
            "           <NAME>Сырок гл. МАКОВКА 20% 40г</NAME>           \n" +
            "           <EAN>4810268055485</EAN>\n" +
            "           <CATEGORY>1803090000</CATEGORY>\n" +
            "           <CATEGORY_NAME>конфетно-десертная линейка</CATEGORY_NAME>\n" +
            "           <NAS/>\n" +
            "           <DESCRIPTION>Сырок творожный глазированный \"Маковка\" массовой долей жира 20,0 %, ФЛОУПАК 40 г</DESCRIPTION>\n" +
            "           <SPECIFICATION/>\n" +
            "           <ACTIVE/>\n" +
            "           <QUALITY_CONTROL>Y</QUALITY_CONTROL>\n" +
            "           <NETTO_WEIGHT/>\n" +
            "           <BRUTTO_WEIGHT>0.04</BRUTTO_WEIGHT>>\n" +
            "           <VOLUME/>\n" +
            "           <SHELF_LIFE>30</SHELF_LIFE>\n" +
            "           <FREQUENCY/>\n" +
            "           <LOT>Y</LOT>\n" +
            "           <BBDATE>Y</BBDATE>\n" +
            "           <SERIAL/>\n" +
            "           <WRAPPING/>\n" +
            "           <LU_TYPE/>\n" +
            "           <TEMPERATURE_REGIME/>\n" +
            "<PACKINGS>\n" +
            "<PACKING>\n" +
            "                              <ITEM_ID>0307060137</ITEM_ID>\n" +
            "                              <EANPACK>14810268055482</EANPACK>\n" +
            "                              <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
            "                              <LU_QUANTITY>315</LU_QUANTITY>\n" +
            "                              <NAME/>\n" +
            "                              <DESCRIPTION/>\n" +
            "                              <WIDTH/>\n" +
            "                              <LENGTH/>\n" +
            "                              <HEIGHT/>\n" +
            "                              <NET_WEIGHT/>\n" +
            "                              <GROSS_WEIGHT/>\n" +
            "                              <VOLUME/>\n" +
            "                              <ZIP/>\n" +
            "                            </PACKING>\n" +
            "          <PACKING>\n" +
            "                              <ITEM_ID>0307060137</ITEM_ID>\n" +
            "                              <EANPACK>24810268055489</EANPACK>\n" +
            "                              <PARENT_QUANTITY>18</PARENT_QUANTITY>\n" +
            "                              <LU_QUANTITY>315</LU_QUANTITY>\n" +
            "                              <NAME/>\n" +
            "                              <DESCRIPTION/>\n" +
            "                              <WIDTH/>\n" +
            "                              <LENGTH/>\n" +
            "                              <HEIGHT/>\n" +
            "                              <NET_WEIGHT/>\n" +
            "                              <GROSS_WEIGHT/>\n" +
            "                              <VOLUME/>\n" +
            "                              <ZIP/>\n" +
            "                            </PACKING>\n" +
            "          </PACKINGS>\n" +
            "         </ITEM>\n" +
            "     </MESSAGE>";
}
