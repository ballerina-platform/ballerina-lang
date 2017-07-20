import ballerina.lang.xmls;

xmlns "http://ballerina.com/b" as ns1; 

function testElementLiteralWithNamespaces()(xml, xml) {
    xmlns "http://ballerina.com/";
    xmlns "http://ballerina.com/a" as ns0;
    xmlns "http://ballerina.com/c" as ns1; 

    xml x1 = xml `<root ns0:id="456"><foo>123</foo><bar ns1:status="complete"></bar></root>`;
    xml x2 = xmls:children(x1);
    return x1, x2;
}

function testElementWithQualifiedName()(xml, xml, xml, xml, xml) {

    xml x1 = xml `<root>hello</root>`;
    
    xmlns "http://ballerina.com/";
    xml x2 = xml `<root>hello</root>`;

    xml x3 = xml `<ns1:root>hello</ns1:root>`;
 
    xml x4 = xml `<{{"{http://wso2.com}root"}}>hello</{{"{http://wso2.com}root"}}>`;
    
    xml x5 = xml `<{{"{http://ballerina.com/b}root"}}>hello</{{"{http://ballerina.com/b}root"}}>`;
    
    return x1, x2, x3, x4, x5;
}

function testDefineInlineNamespace()(xml) {
    xml x1 = xml `<nsx:foo nsx:id="123" xmlns:nsx="http://wso2.com" >hello</nsx:foo>`;
    return x1;
}

function testDefineInlineDefaultNamespace()(xml, xml, xml) {
    xmlns "http://ballerina.com/default/namespace";

    string defaultNs = "http://ballerina.com";
    
    xml x1 = xml `<foo xmlns:nsx="http://wso2.com/aaa" >hello</foo>`;
    xml x2 = xml `<foo xmlns:nsx="http://wso2.com/aaa" xmlns="http://wso2.com" >hello</foo>`;
    xml x3 = xml `<foo xmlns:nsx="http://wso2.com/aaa" xmlns="{{defaultNs}}" >hello</foo>`;
    return x1, x2, x3;
}
