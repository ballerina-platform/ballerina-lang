import ballerina/lang.xmls;
import ballerina/lang.system;
import ballerina/lang.strings;

xmlns "http://sample.com/wso2/a1" as ns0;
xmlns "http://sample.com/wso2/b1" as ns1;
xmlns "http://sample.com/wso2/c1";
xmlns "http://sample.com/wso2/d1" as ns3;

function testNamespaceDclr() (xml) {
    xmlns "http://sample.com/wso2/a2" as ns0;
    xmlns "http://sample.com/wso2/c2";
    xmlns "http://sample.com/wso2/d2" as ns3;
    
    xml x1 = xmls:parse("<root><foo/></root>");
    
    return x1; 
}

function testAddAttributeWithString() (xml) {
    xml x1 = xmls:parse("<root xmlns:ns4=\"http://sample.com/wso2/f\"></root>");
    
    // without namespaceUri
    x1@["foo1"] = "bar1";
    
    // with a new namespaceUri
    x1@["{http://sample.com/wso2/e}foo2"] = "bar2";
    
    // with an existing namespaceUri
    x1@["{http://sample.com/wso2/f}foo3"] = "bar3";
    
    return x1;
}

function testAddAttributeWithQName() (xml) {
    xml x1 = xmls:parse("<root xmlns:ns3=\"http://sample.com/wso2/f\"></root>");
    
    x1@[ns0:foo1] = "bar1";
    
    // with an existing prefix, but different uri
    x1@[ns3:foo2] = "bar2";
    
    system:println(x1);
    
    return x1;
}

function testAddAttributeWithoutLocalname() (xml) {
    xml x1 = xmls:parse("<root xmlns:ns3=\"http://sample.com/wso2/f\"></root>");
    x1@["{http://sample.com/wso2/e}"] = "bar";
    return x1;
}

function testAddAttributeWithEmptyNamespace() (xml) {
    xml x1 = xmls:parse("<root xmlns:ns3=\"http://sample.com/wso2/f\"></root>");
    x1@["{}foo1"] = "bar";
    return x1;
}

function testAddNamespaceAsAttribute() (xml, xml) {
    xml x1 = xmls:parse("<root xmlns:ns3=\"http://sample.com/wso2/f\"></root>");
    x1@["{http://www.w3.org/2000/xmlns/}ns4"] = "http://wso2.com";
    
    xml x2 = xmls:parse("<root xmlns=\"http://ballerinalang.org/\" xmlns:ns3=\"http://sample.com/wso2/f\"></root>");
    x2@["{http://ballerinalang.org/}ns4"] = "http://wso2.com";
    
    return x1, x2;
}

function testUpdateNamespaceAsAttribute() (xml, xml) {
    xml x1 = xmls:parse("<root xmlns:ns3=\"http://sample.com/wso2/f\"></root>");
    x1@["{http://www.w3.org/2000/xmlns/}ns3"] = "http://wso2.com";
    
    xml x2 = xmls:parse("<root xmlns=\"http://ballerinalang.org/\" xmlns:ns3=\"http://sample.com/wso2/f\"></root>");
    x2@["{http://ballerinalang.org/}ns3"] = "http://wso2.com";
    
    return x1, x2;
}

function testUpdateAttributeWithString() (xml) {
    xml x1 = xmls:parse("<root xmlns:ns4=\"http://sample.com/wso2/f\" xmlns:ns0Kf5j=\"http://sample.com/wso2/e\" foo1=\"bar1\" ns0Kf5j:foo2=\"bar2\" ns4:foo3=\"bar3\"/>");
    
    // without namespaceUri
    x1@["foo1"] = "newbar1";
    
    // with a new namespaceUri
    x1@["{http://sample.com/wso2/e}foo2"] = "newbar2";
    
    return x1;
}

function testUpdateAttributeWithQName() (xml) {
    xml x1 = xmls:parse("<root xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"bar1\" ns3:foo2=\"bar2\"/>");
    
    // with a matching namespaceUri and prefix
    x1@[ns0:foo1] = "newbar1";
    
    // with a matching namespaceUri but different prefix
    xmlns "http://sample.com/wso2/f" as ns4;
    x1@[ns4:foo2] = "newbar2";
    
    system:println(x1);
    
    return x1;
}

function testGetAttributeWithString() (string, string, string) {
    xml x1 = xmls:parse("<root xmlns:ns4=\"http://sample.com/wso2/f\" xmlns:ns0Kf5j=\"http://sample.com/wso2/e\" foo1=\"bar1\" ns0Kf5j:foo2=\"bar2\" ns4:foo3=\"bar3\"/>");
    return x1@["foo1"], x1@["{http://sample.com/wso2/e}foo2"], x1@["{http://sample.com/wso2/e}foo3"];
}

function testGetAttributeWithoutLocalname() (string) {
    xml x1 = xmls:parse("<root xmlns:ns4=\"http://sample.com/wso2/f\" xmlns=\"http://sample.com/wso2/e\" foo1=\"bar1\" ns4:foo3=\"bar3\"/>");
    return x1@["{http://sample.com/wso2/e}"];
}

function testGetAttributeWithEmptyNamespace() (string) {
    xml x1 = xmls:parse("<root xmlns:ns4=\"http://sample.com/wso2/f\" xmlns=\"http://sample.com/wso2/e\" foo1=\"bar1\" ns4:foo3=\"bar3\"/>");
    return x1@["{}foo1"];
}

function testGetNamespaceAsAttribute() (string) {
    xml x1 = xmls:parse("<root xmlns:ns4=\"http://sample.com/wso2/f\" xmlns=\"http://sample.com/wso2/e\" foo1=\"bar1\" ns4:foo3=\"bar3\"/>");
    return x1@["{http://sample.com/wso2/e}ns4"];
}

function testGetAttributeWithQName() (string, string, string) {
    xml x1 = xmls:parse("<root xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"bar1\" ns3:foo2=\"bar2\"/>");
    xmlns "http://sample.com/wso2/f" as ns4;
    return x1@[ns0:foo1], x1@[ns4:foo2], x1@[ns1:foo2];
}

function testUsingQNameAsString () (string, string) {
    string s1 = ns0:wso2;
    string s2 = strings:trim("  " + ns0:ballerina + "  ");
    return s1, s2;
}

function testGetAttributesAsMap()(map, map, string, string) {
    xml x1 = xmls:parse("<root xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"bar1\" foo2=\"bar2\"/>");
    xml x2 = xmls:parse("<root xmlns=\"http://sample.com/default/namepsace\" xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"bar1\" foo2=\"bar2\"/>");
    
    map m1 = <map> x1@;
    map m2 = <map> x2@;
    
    var s1, e1 = (string) m1["{http://sample.com/wso2/a1}foo1"];
    var s2, e2 = (string) m1[ns0:foo1];
    return m1, m2, s1, s2;
}

function testXMLAttributesToAny()(any) {
    xml x1 = xmls:parse("<root xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"bar1\" foo2=\"bar2\"/>");
    
    return foo(x1@);
}

function foo(any a) (any) {
    return a;
}

function testRuntimeNamespaceLookup() (xml) {
    xmlns "http://sample.com/wso2/a1" as ns401;
    xmlns "http://sample.com/wso2/d2" as ns402;
    
    xml x = xmls:parse("<root/>");
    
    x@["{http://sample.com/wso2/a1}foo1"] = "bar1";
    x@["{http://sample.com/wso2/b1}foo2"] = "bar2";
    
    if (true) {
        xmlns "http://sample.com/wso2/e3" as ns403;
        xmlns "http://sample.com/wso2/f3" as ns404;
        
        x@["{http://sample.com/wso2/e3}foo3"] = "bar3";
    }
    
    x@["{http://sample.com/wso2/f3}foo4"] = "bar4";
    
    return x;
}

function testRuntimeNamespaceLookupPriority() (xml) {
    xmlns "http://sample.com/wso2/a1" as ns401;
    
    xml x = xmls:parse("<root xmlns:p1=\"http://wso2.com\" xmlns:p2=\"http://sample.com/wso2/a1\"/>");
    
    x@["{http://sample.com/wso2/a1}foo1"] = "bar1";
    x@["{http://wso2.com}foo2"] = "bar2";
    
    xmlns "http://wso2.com" as ns402;
    
    return x;
}

function testSetAttributes() (xml) {
    map attributesMap = {"foo1":"bar1", "{http://wso2.com}foo2":"bar2"};
    attributesMap[ns0:foo3] = "bar3";
    xml x = xmls:parse("<root xmlns:p1=\"http://wso2.com\" xmlns:p2=\"http://sample.com/wso2/a1\"/>");
    
    xmls:setAttributes(x, attributesMap);
    
    return x;
}

function testGetAttributeFromSingletonSeq() (string) {
    xml x1 = xmls:parse("<root><child xmlns:p1=\"http://wso2.com/\" xmlns:p2=\"http://sample.com/wso2/a1/\" p1:foo=\"bar\"/></root>");
    xml x2 = xmls:children(x1);
    return x2@["{http://wso2.com/}foo"];
}

function testGetAttributeFromLiteral() (string) {
    xmlns "http://sample.com/wso2/a1" as ns0;
    
    xml x = xml `<root ns0:id="5"/>`;
    
    return x@[ns0:id];
}