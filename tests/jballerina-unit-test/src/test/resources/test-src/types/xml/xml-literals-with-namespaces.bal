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

// function testComplexXMLLiteral() returns (xml) {
//   xml x = xml `<cre:InputParameters xmlns:cre="http://xmlns.oracle.com/apps/ozf/soaprovider/plsql/ozf_sd_request_pub/create_sd_request/" xmlns:ore="http://xmlns.oracle.com/apps/ozf/soaprovider/plsql/ozf_sd_request_pub/create_sd_request/" xmlns:pre="http://xmlns.oracle.com/apps/ozf/soaprovider/plsql/ozf_sd_request_pub/create_sd_request/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
//    <cre:P_API_VERSION_NUMBER>1.0</cre:P_API_VERSION_NUMBER>
//    <cre:P_INIT_MSG_LIST>T</cre:P_INIT_MSG_LIST>
//    <cre:P_COMMIT>F</cre:P_COMMIT>
//    <cre:P_VALIDATION_LEVEL>100</cre:P_VALIDATION_LEVEL>
//     <cre:P_SDR_HDR_REC>
//         <cre:REQUEST_NUMBER>SDR-CREATE-BPEL1</cre:REQUEST_NUMBER>
//         <cre:REQUEST_START_DATE>2008-08-18T12:00:00</cre:REQUEST_START_DATE>
//         <cre:REQUEST_END_DATE>2008-10-18T12:00:00</cre:REQUEST_END_DATE>
//         <cre:USER_STATUS_ID>1701</cre:USER_STATUS_ID>
//         <cre:REQUEST_OUTCOME>IN_PROGRESS</cre:REQUEST_OUTCOME>
//         <cre:REQUEST_CURRENCY_CODE>USD</cre:REQUEST_CURRENCY_CODE>
//         <cre:SUPPLIER_ID>601</cre:SUPPLIER_ID>
//         <cre:SUPPLIER_SITE_ID>1415</cre:SUPPLIER_SITE_ID>
//         <cre:REQUESTOR_ID>100001499</cre:REQUESTOR_ID>
//         <cre:ASSIGNEE_RESOURCE_ID>100001499</cre:ASSIGNEE_RESOURCE_ID>
//         <cre:ORG_ID>204</cre:ORG_ID>
//         <cre:ACCRUAL_TYPE>SUPPLIER</cre:ACCRUAL_TYPE>
//         <cre:REQUEST_DESCRIPTION>Create</cre:REQUEST_DESCRIPTION>
//         <cre:SUPPLIER_CONTACT_EMAIL_ADDRESS>sdr.supplier@example.com</cre:SUPPLIER_CONTACT_EMAIL_ADDRESS>
//         <cre:SUPPLIER_CONTACT_PHONE_NUMBER>2255</cre:SUPPLIER_CONTACT_PHONE_NUMBER>
//         <cre:REQUEST_TYPE_SETUP_ID>400</cre:REQUEST_TYPE_SETUP_ID>
//         <cre:REQUEST_BASIS>Y</cre:REQUEST_BASIS>
//         <cre:USER_ID>1002795</cre:USER_ID>
//     </cre:P_SDR_HDR_REC>
//     <cre:P_SDR_LINES_TBL>
//         <pre:P_SDR_LINES_TBL_ITEM pre:line="true" ore:tested="yes">
//             <pre:PRODUCT_CONTEXT>PRODUCT</pre:PRODUCT_CONTEXT>
//             <ore:INVENTORY_ITEM_ID>2155</ore:INVENTORY_ITEM_ID>
//             <cre:ITEM_UOM>Ea</cre:ITEM_UOM>
//             <cre:REQUESTED_DISCOUNT_TYPE>%</cre:REQUESTED_DISCOUNT_TYPE>
//             <cre:REQUESTED_DISCOUNT_VALUE>20</cre:REQUESTED_DISCOUNT_VALUE>
//             <cre:COST_BASIS>200</cre:COST_BASIS>
//             <cre:MAX_QTY>200</cre:MAX_QTY>
//             <cre:APPROVED_DISCOUNT_TYPE>%</cre:APPROVED_DISCOUNT_TYPE>
//             <cre:APPROVED_DISCOUNT_VALUE>20</cre:APPROVED_DISCOUNT_VALUE>
//             <cre:APPROVED_MAX_QTY>200</cre:APPROVED_MAX_QTY>
//             <cre:VENDOR_APPROVED_FLAG>Y</cre:VENDOR_APPROVED_FLAG>
//             <cre:PRODUCT_COST_CURRENCY>USD</cre:PRODUCT_COST_CURRENCY>
//             <cre:END_CUSTOMER_CURRENCY>USD</cre:END_CUSTOMER_CURRENCY>
//         </pre:P_SDR_LINES_TBL_ITEM>
//     </cre:P_SDR_LINES_TBL>
//     <cre:P_SDR_CUST_TBL>
//         <cre:P_SDR_CUST_TBL_ITEM>
//             <cre:CUST_ACCOUNT_ID>1290</cre:CUST_ACCOUNT_ID>
//             <cre:PARTY_ID>1290</cre:PARTY_ID>
//             <cre:SITE_USE_ID>10479</cre:SITE_USE_ID>
//             <cre:CUST_USAGE_CODE>BILL_TO</cre:CUST_USAGE_CODE>
//             <cre:END_CUSTOMER_FLAG>N</cre:END_CUSTOMER_FLAG>
//         </cre:P_SDR_CUST_TBL_ITEM>
//     </cre:P_SDR_CUST_TBL>
//     <cre:P_SDR_CUST_TBL>
//         <cre:P_SDR_CUST_TBL_ITEM>
//             <cre:CUST_ACCOUNT_ID>1287</cre:CUST_ACCOUNT_ID>
//             <cre:PARTY_ID>1287</cre:PARTY_ID>
//             <cre:SITE_USE_ID>1418</cre:SITE_USE_ID>
//             <cre:CUST_USAGE_CODE>CUSTOMER</cre:CUST_USAGE_CODE>
//             <cre:END_CUSTOMER_FLAG>Y</cre:END_CUSTOMER_FLAG>
//         </cre:P_SDR_CUST_TBL_ITEM>
//     </cre:P_SDR_CUST_TBL>
//     </cre:InputParameters>`;
//
//   return x;
// }

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
    xml x = xml `<Order xmlns="http://acme.company" xmlns:acme="http://acme.company">
        <OrderLines>
            <OrderLine acme:lineNo="334" itemCode="334-2"></OrderLine>
        </OrderLines>
        <ShippingAddress>
        </ShippingAddress>
    </Order>`;

    return x.toString();
}
