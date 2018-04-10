
import ballerina/math as x;

function testRestrictedElementPrefix() returns (xml) {
    xml x = xml `<xmlns:foo>hello</xmlns:foo>`;
    return x;
}

function xmlUndeclaredElementPrefix() returns (xml) {
    xml x = xml `<ns1:foo>hello</ns1:foo>`;
    return x;
}

function xmlTemplateWithNonXML() {
    map m ;
    xml x = xml `<root xmlns="http://default/namespace">{{m}}</root>`;
}

function defineEmptyNamespaceInline() {
    xml x = xml `<root xmlns:ns0=""/>`;
}

function testExpressionAsElementName() {
    int v1 = 11;
    // xml x1 = xml `<ns0:{{v1}}>hello</ns0:{{v1}}>`;
}

function testTextWithMultiTypeExpressions() {
    int v1 = 11;
    string v2 = "world";
    xml v3 = xml `<foo>apple</foo>`;
    
    xml x = xml `hello {{v1}} {{v2}}. How are you {{v3}} ?`;
}

function testRedeclareNamespaces() {
    xmlns "http://sample.com/wso2/a2" as ns0;
    xmlns "http://sample.com/wso2/c2";
    xmlns "http://sample.com/wso2/d2" as ns3;
    
    if (true) {
        xmlns "http://sample.com/wso2/a3" as ns0;
    }
}

function testXMlAttributesMapInvalidUsage() {
    var x1 = xml `<root foo1="bar1" foo2="bar2"/>`;
    map m1 = x1@;
}

function foo() {
    xmlns "http:wso2.com/" as x;
}

function getAttributesFromNonXml() {
    map m ;
    string s = m@["foo"];
}

function updateAttributesMap() {
    xml x;
    x@ = "new attributes";
}

function updateQname() {
    xmlns "http://wso2.com/" as ns0;
    ns0:foo = "{uri}localname";
}

function undefinedNamespace() {
    xml x;
    if (true) {
        xmlns "http://wso2.com/" as ns0;
    }
    string s = x@[ns0:foo];
}

function defineEmptyNamespace() {
    xmlns "" as ns0;
}