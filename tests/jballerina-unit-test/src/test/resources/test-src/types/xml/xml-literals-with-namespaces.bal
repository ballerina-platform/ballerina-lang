import ballerina/test;

xmlns "http://ballerina.com/b" as ns1; 

function testElementLiteralWithNamespaces() returns [xml, xml] {
    xmlns "http://ballerina.com/";
    xmlns "http://ballerina.com/a" as ns0;
    xmlns "http://ballerina.com/c" as ns1;

    xml x1 = xml `<root ns0:id="456"><foo>123</foo><bar ns1:status="complete"></bar></root>`;
    xml x2 = x1/*;
    return [x1, x2];
}

function testElementWithQualifiedName() returns [xml, xml, xml] {

    xml x1 = xml `<root>hello</root>`;

    xmlns "http://ballerina.com/";
    xml x2 = xml `<root>hello</root>`;

    xml x3 = xml `<ns1:root>hello</ns1:root>`;

    return [x1, x2, x3];
}

function testDefineInlineNamespace() returns (xml) {
    xml x1 = xml `<nsx:foo nsx:id="123" xmlns:nsx="http://wso2.com" >hello</nsx:foo>`;
    return x1;
}

function testDefineInlineDefaultNamespace() returns [xml, xml] {
    xmlns "http://ballerina.com/default/namespace";

    xml x1 = xml `<foo xmlns:nsx="http://wso2.com/aaa" >hello</foo>`;
    xml x2 = xml `<foo xmlns:nsx="http://wso2.com/aaa" xmlns="http://wso2.com" >hello</foo>`;
    return [x1, x2];
}

function testUsingNamespcesOfParent() returns (xml) {
    xml x = xml `<root xmlns:ns0="http://ballerinalang.com/"><ns0:foo>hello</ns0:foo></root>`;
    return x;
}

function testComplexXMLLiteral() returns (xml) {
  xml x = xml `<cre:InputParameters xmlns:cre="http://xmlns.oracle.com/apps/ozf/soaprovider/plsql/ozf_sd_request_pub/create_sd_request/" xmlns:ore="http://xmlns.oracle.com/apps/ozf/soaprovider/plsql/ozf_sd_request_pub/create_sd_request/" xmlns:pre="http://xmlns.oracle.com/apps/ozf/soaprovider/plsql/ozf_sd_request_pub/create_sd_request/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <cre:P_API_VERSION_NUMBER>1.0</cre:P_API_VERSION_NUMBER>
   <cre:P_INIT_MSG_LIST>T</cre:P_INIT_MSG_LIST>
   <cre:P_COMMIT>F</cre:P_COMMIT>
   <cre:P_VALIDATION_LEVEL>100</cre:P_VALIDATION_LEVEL>
    <cre:P_SDR_HDR_REC>
        <cre:REQUEST_NUMBER>SDR-CREATE-BPEL1</cre:REQUEST_NUMBER>
        <cre:REQUEST_START_DATE>2008-08-18T12:00:00</cre:REQUEST_START_DATE>
        <cre:REQUEST_END_DATE>2008-10-18T12:00:00</cre:REQUEST_END_DATE>
        <cre:USER_STATUS_ID>1701</cre:USER_STATUS_ID>
        <cre:REQUEST_OUTCOME>IN_PROGRESS</cre:REQUEST_OUTCOME>
        <cre:REQUEST_CURRENCY_CODE>USD</cre:REQUEST_CURRENCY_CODE>
        <cre:SUPPLIER_ID>601</cre:SUPPLIER_ID>
        <cre:SUPPLIER_SITE_ID>1415</cre:SUPPLIER_SITE_ID>
        <cre:REQUESTOR_ID>100001499</cre:REQUESTOR_ID>
        <cre:ASSIGNEE_RESOURCE_ID>100001499</cre:ASSIGNEE_RESOURCE_ID>
        <cre:ORG_ID>204</cre:ORG_ID>
        <cre:ACCRUAL_TYPE>SUPPLIER</cre:ACCRUAL_TYPE>
        <cre:REQUEST_DESCRIPTION>Create</cre:REQUEST_DESCRIPTION>
        <cre:SUPPLIER_CONTACT_EMAIL_ADDRESS>sdr.supplier@example.com</cre:SUPPLIER_CONTACT_EMAIL_ADDRESS>
        <cre:SUPPLIER_CONTACT_PHONE_NUMBER>2255</cre:SUPPLIER_CONTACT_PHONE_NUMBER>
        <cre:REQUEST_TYPE_SETUP_ID>400</cre:REQUEST_TYPE_SETUP_ID>
        <cre:REQUEST_BASIS>Y</cre:REQUEST_BASIS>
        <cre:USER_ID>1002795</cre:USER_ID>
    </cre:P_SDR_HDR_REC>
    <cre:P_SDR_LINES_TBL>
        <pre:P_SDR_LINES_TBL_ITEM pre:line="true" ore:tested="yes">
            <pre:PRODUCT_CONTEXT>PRODUCT</pre:PRODUCT_CONTEXT>
            <ore:INVENTORY_ITEM_ID>2155</ore:INVENTORY_ITEM_ID>
            <cre:ITEM_UOM>Ea</cre:ITEM_UOM>
            <cre:REQUESTED_DISCOUNT_TYPE>%</cre:REQUESTED_DISCOUNT_TYPE>
            <cre:REQUESTED_DISCOUNT_VALUE>20</cre:REQUESTED_DISCOUNT_VALUE>
            <cre:COST_BASIS>200</cre:COST_BASIS>
            <cre:MAX_QTY>200</cre:MAX_QTY>
            <cre:APPROVED_DISCOUNT_TYPE>%</cre:APPROVED_DISCOUNT_TYPE>
            <cre:APPROVED_DISCOUNT_VALUE>20</cre:APPROVED_DISCOUNT_VALUE>
            <cre:APPROVED_MAX_QTY>200</cre:APPROVED_MAX_QTY>
            <cre:VENDOR_APPROVED_FLAG>Y</cre:VENDOR_APPROVED_FLAG>
            <cre:PRODUCT_COST_CURRENCY>USD</cre:PRODUCT_COST_CURRENCY>
            <cre:END_CUSTOMER_CURRENCY>USD</cre:END_CUSTOMER_CURRENCY>
        </pre:P_SDR_LINES_TBL_ITEM>
    </cre:P_SDR_LINES_TBL>
    <cre:P_SDR_CUST_TBL>
        <cre:P_SDR_CUST_TBL_ITEM>
            <cre:CUST_ACCOUNT_ID>1290</cre:CUST_ACCOUNT_ID>
            <cre:PARTY_ID>1290</cre:PARTY_ID>
            <cre:SITE_USE_ID>10479</cre:SITE_USE_ID>
            <cre:CUST_USAGE_CODE>BILL_TO</cre:CUST_USAGE_CODE>
            <cre:END_CUSTOMER_FLAG>N</cre:END_CUSTOMER_FLAG>
        </cre:P_SDR_CUST_TBL_ITEM>
    </cre:P_SDR_CUST_TBL>
    <cre:P_SDR_CUST_TBL>
        <cre:P_SDR_CUST_TBL_ITEM>
            <cre:CUST_ACCOUNT_ID>1287</cre:CUST_ACCOUNT_ID>
            <cre:PARTY_ID>1287</cre:PARTY_ID>
            <cre:SITE_USE_ID>1418</cre:SITE_USE_ID>
            <cre:CUST_USAGE_CODE>CUSTOMER</cre:CUST_USAGE_CODE>
            <cre:END_CUSTOMER_FLAG>Y</cre:END_CUSTOMER_FLAG>
        </cre:P_SDR_CUST_TBL_ITEM>
    </cre:P_SDR_CUST_TBL>
    </cre:InputParameters>`;

  return x;
}

function testNamespaceDclr() returns [string, string, string] {
    xmlns "http://sample.com/wso2/a2" as ns0;
    xmlns "http://sample.com/wso2/b2" as ns1;
    xmlns "http://sample.com/wso2/c2";
    xmlns "http://sample.com/wso2/d2" as ns3;

    return [ns0:foo, ns1:foo, ns3:foo];
}

function testInnerScopeNamespaceDclr() returns [string, string, string] {
    string s1 = "";
    string s2 = "";
    string s3 = "";

    if (true) {
        s1 = ns1:foo;

        xmlns "http://sample.com/wso2/a3" as ns1;
        s2 = ns1:foo;
    }

    s3 = ns1:foo;

    return [s1, s2, s3];
}

class Person {
    xml info = xml `<p:person xmlns:p="foo" xmlns:q="bar">hello</p:person>`;
}

function testObjectLevelXML() returns xml {
    Person p = new();
    return p.info;
}

function getXML() returns xml {
    xml x = xml `<foo xmlns="http://wso2.com/">hello</foo>`;
    return x;
}

function XMLWithDefaultNamespaceToString() returns string {
    xml x = xml `<Order xmlns="http://acme.company" xmlns:acme="http://acme.company.nondefault">
        <OrderLines>
            <OrderLine acme:lineNo="334" itemCode="334-2"></OrderLine>
        </OrderLines>
        <ShippingAddress>
        </ShippingAddress>
    </Order>`;

    return x.toString();
}

function testXmlLiteralUsingXmlNamespacePrefix() {
    xml x1 = xml `<entry xml:base="https://namespace.servicebus.windows.net/$Resources/Eventhubs"></entry>`;
    string s = x1.toString();
    string expectedStr = "<entry xml:base=\"https://namespace.servicebus.windows.net/$Resources/Eventhubs\"/>";
    test:assertEquals(s, expectedStr, "XML literal with xml namespace prefix failed");
}

xmlns "http://www.so2w.org" as globalNS;

function testXmlInterpolationWithQuery() returns error? {
    xml x1 = xml `<empRecords xmlns:inlineNS="http://www.so2w.org">
         ${from int i in 1 ... 3
        select xml `<empRecord employeeId="${i}"><inlineNS:id>${i}</inlineNS:id></empRecord>`}
      </empRecords>;`;
    xml x2 = x1/<empRecord>[0];
    string s1 = x2.toString();
    string expectedStr1 = "<empRecord employeeId=\"1\"><inlineNS:id xmlns:inlineNS=\"http://www.so2w.org\">1</inlineNS:id></empRecord>";
    test:assertEquals(s1, expectedStr1, "XML interpolation with query failed");

    xmlns "http://www.so2w.org" as localNS;
    xml x3 = xml `<empRecords>
         ${from int i in 1 ... 3
        select xml `<empRecord employeeId="${i}"><localNS:id>${i}</localNS:id></empRecord>`}
      </empRecords>;`;
    xml x4 = x3/<empRecord>[0]/<localNS:id>;
    string s2 = x4.toString();
    string expectedStr2 = "<localNS:id xmlns:localNS=\"http://www.so2w.org\">1</localNS:id>";
    test:assertEquals(s2, expectedStr2, "XML interpolation with query failed");

    xml x5 = from int i in [1]
        select xml `<empRecord employeeId="${i}"><localNS:id>${i}</localNS:id></empRecord>`;
    string s3 = x5.toString();
    string expectedStr3 = "<empRecord employeeId=\"1\"><localNS:id xmlns:localNS=\"http://www.so2w.org\">1</localNS:id></empRecord>";
    test:assertEquals(s3, expectedStr3, "XML interpolation with query failed");

    xml x6 = xml `<empRecords>
         ${from int i in 1 ... 3
        select xml `<empRecord employeeId="${i}"><globalNS:id>${i}</globalNS:id></empRecord>`}
      </empRecords>;`;
    xml x7 = x6/<empRecord>[0]/<globalNS:id>;
    string s4 = x7.toString();
    string expectedStr4 = "<globalNS:id xmlns:globalNS=\"http://www.so2w.org\">1</globalNS:id>";
    test:assertEquals(s4, expectedStr4, "XML interpolation with query failed");

    xml x8 = xml ``;
    from int i in [1]
    do {
        x8 = xml `<empRecord employeeId="${i}"><localNS:id>${i}</localNS:id></empRecord>`;
    };
    string s5 = x8.toString();
    test:assertEquals(s5, expectedStr3, "XML interpolation with query failed");

    xml x9 = xml ``;
    from int i in [1]
    do {
        from int j in [1]
        do {
            x9 = xml `<empRecord employeeId="${j}"><localNS:id>${j}</localNS:id></empRecord>`;
        };
    };
    string s6 = x9.toString();
    test:assertEquals(s6, expectedStr3, "XML interpolation with query failed");

    xml x10 = from int i in [1]
        let xml y = xml `<empRecord employeeId="${i}"><localNS:id>${i}</localNS:id></empRecord>`
        select xml `<record>${y}</record>`;
    string s7 = x10.toString();
    string expectedStr5 = "<record><empRecord employeeId=\"1\"><localNS:id xmlns:localNS=\"http://www.so2w.org\">1</localNS:id></empRecord></record>";
    test:assertEquals(s7, expectedStr5, "XML interpolation with query failed");

    xml x11 = from xml x in (from int j in [1]
            select xml `<empRecord employeeId="${j}"><localNS:id>${j}</localNS:id></empRecord>`)
        select xml `<record>${x}</record>`;
    string s8 = x11.toString();
    test:assertEquals(s8, expectedStr5, "XML interpolation with query failed");

    xml x12 = from int i in [1]
        join int j in [1]
    on xml `<empRecord employeeId="${i}"><localNS:id>${i}</localNS:id></empRecord>`
    equals xml `<empRecord employeeId="${j}"><localNS:id>${j}</localNS:id></empRecord>`
        select xml `<record>${i}</record>`;
    string s9 = x12.toString();
    string expectedStr6 = "<record>1</record>";
    test:assertEquals(s9, expectedStr6, "XML interpolation with query failed");

    xml expectedXml = xml `<empRecord employeeId="1"><localNS:id>1</localNS:id></empRecord>`;
    xml x13 = from int i in [1]
        where xml `<empRecord employeeId="${i}"><localNS:id>${i}</localNS:id></empRecord>` == expectedXml
        select xml `<record>${expectedXml}</record>`;
    string s10 = x13.toString();
    test:assertEquals(s10, expectedStr5, "XML interpolation with query failed");

    do {
        xmlns "http://www.so2w1.org" as doNS;
        xml x14 = from int i in [1]
            select xml `<localNS:empRecord employeeId="${i}"><doNS:id>${i}</doNS:id></localNS:empRecord>`;
        string s11 = x14.toString();
        string expectedStr7 = "<localNS:empRecord xmlns:localNS=\"http://www.so2w.org\" employeeId=\"1\"><doNS:id xmlns:doNS=\"http://www.so2w1.org\">1</doNS:id></localNS:empRecord>";
        test:assertEquals(s11, expectedStr7, "XML interpolation with query failed");
    }

    do {
        xmlns "http://www.so2w2.org" as doNS;
        xml x15 = from int i in [1]
            select xml `<localNS:empRecord employeeId="${i}"><doNS:id>${i}</doNS:id></localNS:empRecord>`;
        string s12 = x15.toString();
        string expectedStr8 = "<localNS:empRecord xmlns:localNS=\"http://www.so2w.org\" employeeId=\"1\"><doNS:id xmlns:doNS=\"http://www.so2w2.org\">1</doNS:id></localNS:empRecord>";
        test:assertEquals(s12, expectedStr8, "XML interpolation with query failed");
    }
}

function testAddAttributeToDefaultNS() {
    xml x1 = xml `<root xmlns="http://sample.com/wso2/c1" xmlns:ns3="http://sample.com/wso2/f"></root>`;
    var xAttr = let var x2 = <'xml:Element>x1 in x2.getAttributes();
    //adding attribute with default namespace
    xAttr["{http://sample.com/wso2/c1}foo1"] = "bar1";
    string s = x1.toString();
    string expectedStr = string `<root xmlns="http://sample.com/wso2/c1" xmlns:ns3="http://sample.com/wso2/f" foo1="bar1"/>`;
    test:assertEquals(s, expectedStr, "XML add attribute with default namespace failed");
    
    s = xAttr.toString();
    expectedStr = string `{"{http://www.w3.org/2000/xmlns/}xmlns":"http://sample.com/wso2/c1","{http://www.w3.org/2000/xmlns/}ns3":"http://sample.com/wso2/f","{http://sample.com/wso2/c1}foo1":"bar1"}`;
    test:assertEquals(s, expectedStr, "XML add attribute with default namespace failed");
}
