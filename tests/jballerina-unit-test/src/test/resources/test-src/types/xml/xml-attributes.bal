import ballerina/io;

xmlns "http://sample.com/wso2/a1" as ns0;
xmlns "http://sample.com/wso2/b1" as ns1;
xmlns "http://sample.com/wso2/c1";
xmlns "http://sample.com/wso2/d1" as ns3;

function testNamespaceDclr() returns (xml) {
    xmlns "http://sample.com/wso2/a2" as ns0;
    xmlns "http://sample.com/wso2/c2";
    xmlns "http://sample.com/wso2/d2" as ns3;
    
    var x1 = xml `<root><foo/></root>`;
    
    return x1; 
}

function testAddAttributeWithString() returns (xml) {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f"></root>`;
    
    // without namespaceUri
    x1@["foo1"] = "bar1";
    
    // with a new namespaceUri
    x1@["{http://sample.com/wso2/e}foo2"] = "bar2";
    
    // with an existing namespaceUri
    x1@["{http://sample.com/wso2/f}foo3"] = "bar3";
    
    return x1;
}

function testAddAttributeWithQName() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    
    x1@[ns0:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_1() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    //same uri, non existant prefix
    xmlns "http://sample.com/wso2/f" as pre;
    x1@[pre:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_2() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    //same uri, existing, non used prefix
    xmlns "http://sample.com/wso2/f/" as ns5;
    x1@[ns5:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_3() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    //same uri, existing, used prefix
    xmlns "http://sample.com/wso2/f/" as ns4;
    x1@[ns4:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_4() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    //different uri, existing, non used prefix
    xmlns "http://sample.com/wso2/f/t" as ns5;
    x1@[ns5:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_5() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    //adding attribute with default namespace
    x1@["{http://sample.com/wso2/c1}foo1"] = "bar1";
    //adding attribute with empty namepsace
    x1@["{}foo2"] = "bar2";
    //adding attribute without a namespace
    x1@["foo3"] = "bar3";
    return x1;
}

function testAddAttributeWithoutLocalname() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    x1@["{http://sample.com/wso2/e}"] = "bar";
    return x1;
}

function testAddAttributeWithEmptyNamespace() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    x1@["{}foo1"] = "bar";
    return x1;
}

function testAddNamespaceAsAttribute() returns [xml, xml] {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    x1@["{http://www.w3.org/2000/xmlns/}ns4"] = "http://wso2.com";

    var x2 = xml `<root xmlns="http://ballerinalang.org/" xmlns:ns3="http://sample.com/wso2/f"></root>`;
    x2@["{http://ballerinalang.org/}att"] = "http://wso2.com";

    return [x1, x2];
}

function testUpdateNamespaceAsAttribute() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    x1@["{http://www.w3.org/2000/xmlns/}ns3"] = "http://wso2.com";
    
    return (x1);
}

function testUpdateAttributeWithString() returns (xml) {
    xmlns "http://defaultNs/";
    
    var x1 = xml `<root xmlns:ns0="http://sample.com/wso2/e" foo1="bar1" ns0:foo2="bar2" foo3="bar3"/>`;
    
    // with empty namespaceUri
    x1@["{}foo1"] = "newbar1";
    
    // with a new namespaceUri
    x1@["{http://sample.com/wso2/e}foo2"] = "newbar2";
    
    // without namespaceUri
    x1@["foo3"] = "newbar3";
    
    return x1;
}

function testUpdateAttributeWithString_1() returns (xml) {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns:ns0Kf5j="http://sample.com/wso2/e" foo1="bar1" ns0Kf5j:foo2="bar2" ns4:foo3="bar3"/>`;

    //with a new uri than the assigned uri for the attribute
    x1@["{http://sample.com/wso2/f/t}foo3"] = "newbar3";

    return x1;
}

function testUpdateAttributeWithQName() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" ns3:foo2="bar2"/>`;
    
    // with a matching namespaceUri and prefix
    x1@[ns0:foo1] = "newbar1";
    
    // with a matching namespaceUri but different prefix
    xmlns "http://sample.com/wso2/f" as ns4;
    x1@[ns4:foo2] = "newbar2";

    return x1;
}

function testUpdateAttributeWithQName_1() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns0="http://sample.com/wso2/a1" xmlns:ns5="http://sample.com/wso2/a1" ns0:foo1="bar1" ns3:foo2="bar2"/>`;

    // with a matching namespaceUri but different prefix
    xmlns "http://sample.com/wso2/a1" as pre;
    x1@[pre:foo1] = "newaddedbar1";

    return x1;
}

function testGetAttributeWithString() returns [string?, string?, string?] {
    xmlns "http://sample.com/wso2/f";
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns:ns0="http://sample.com/wso2/e" foo1="bar1" ns0:foo2="bar2" ns4:foo3="bar3"/>`;
    return [x1@["foo1"], x1@["{http://sample.com/wso2/e}foo2"], x1@["{http://sample.com/wso2/eee}foo2"]];
}

function testGetAttributeWithoutLocalname() returns (string?) {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns="http://sample.com/wso2/e" foo1="bar1" ns4:foo3="bar3"/>`;
    return x1@["{http://sample.com/wso2/e}"];
}

function testGetAttributeWithEmptyNamespace() returns [string?, string?] {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns="http://sample.com/wso2/e" foo1="bar1" ns4:foo3="bar3"/>`;
    return [x1@["{}foo1"], x1@["foo1"]];
}

function testGetNamespaceAsAttribute() returns (string?) {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns="http://sample.com/wso2/e" foo1="bar1" ns4:foo3="bar3"/>`;
    return x1@["{http://www.w3.org/2000/xmlns/}ns4"];
}

function testGetAttributeWithQName() returns [string?, string?, string?] {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" ns3:foo2="bar2"/>`;
    xmlns "http://sample.com/wso2/f" as ns4;
    return [x1@[ns0:foo1], x1@[ns4:foo2], x1@[ns1:foo2]];
}

function testUsingQNameAsString () returns [string, string] {
    string s1 = ns0:wso2;
    string temp = "  " + ns0:ballerina + "  ";
    string s2 = temp.trim();
    return [s1, s2];
}

function testGetAttributesAsMap() returns [map<string>?, map<string>?, string, string] {
    var x1 = xml `<root xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" foo2="bar2"/>`;
    var x2 = xml `<root xmlns="http://sample.com/default/namepsace" xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" foo2="bar2"/>`;
    
    map<string>? m1 = x1@;
    map<string>? m2 = x2@;

    var a = m1["{http://sample.com/wso2/a1}foo1"];
    var s1 = a is string ?  a : "";
    a = m1[ns0:foo1];
    var s2 =  a is string ? a : "";
    return [m1, m2, s1, s2];
}

function testXMLAttributesToAny() returns (any) {
    var x1 = xml `<root xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" foo2="bar2"/>`;
    
    return foo(x1@);
}

function foo(any a) returns (any) {
    return a;
}

function testRuntimeNamespaceLookup() returns (xml) {
    xmlns "http://sample.com/wso2/a1" as ns401;
    xmlns "http://sample.com/wso2/d2" as ns402;
    
    var x = xml `<root/>`;
    
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

function testRuntimeNamespaceLookupPriority() returns (xml) {
    xmlns "http://sample.com/wso2/a1" as ns401;
    
    var x = xml `<root xmlns:p1="http://wso2.com" xmlns:p2="http://sample.com/wso2/a1"/>`;
    
    x@["{http://sample.com/wso2/a1}foo1"] = "bar1";
    x@["{http://wso2.com}foo2"] = "bar2";
    
    xmlns "http://wso2.com" as ns402;
    
    return x;
}

function testSetAttributes() returns (xml) {
    map<any> attributesMap = {"foo1":"bar1", "{http://wso2.com}foo2":"bar2"};
    attributesMap[ns0:foo3] = "bar3";
    var x = xml `<root xmlns:p1="http://wso2.com" xmlns:p2="http://sample.com/wso2/a1"/>`;
    
    x.setAttributes(attributesMap);
    
    return x;
}

function testGetAttributeFromSingletonSeq() returns (string?) {
    var x1 = xml `<root><child xmlns:p1="http://wso2.com/" xmlns:p2="http://sample.com/wso2/a1/" p1:foo="bar"/></root>`;
    xml x2 = x1.*;
    return x2@["{http://wso2.com/}foo"];
}

function testGetAttributeFromLiteral() returns (string?) {
    xmlns "http://sample.com/wso2/a1" as ns0;
    
    xml x = xml `<root ns0:id="5"/>`;
    
    return x@[ns0:id];
}

function testGetAttributeMap() returns (map<string>?) {
    var x1 = xml `<child xmlns:p1="http://wso2.com/" xmlns:p2="http://sample.com/wso2/a1/" p1:foo="bar"/>`;
    map<string>? s = x1@;
    return s;
}

function takeInAMap(map<string>? input) returns map<string>? {
    if (input is map<string>) {
        input["tracer"] = "1";
    }
    return input;
}

function passXmlAttrToFunction() returns map<string>? {
    var x1 = xml `<child foo="bar"/>`;
    return takeInAMap(x1@);
}

function mapOperationsOnXmlAttribute() returns [int?, string[]?, boolean] {
    var x1 = xml `<child foo="bar"/>`;
    boolean isMap = false;
    map<string>? attrMap = x1@;
    if (attrMap is map<string>) {
        return [attrMap.length(), attrMap.keys(), true];
    } else {
        return [0, (), false];
    }
}

function mapUpdateOnXmlAttribute() returns (xml) {
    xmlns "the{}url" as nsLocal;
    var x1 = xml `<child foo="bar" itemCode="3344"/>`;
    map<string> attrMap = <map<string>>x1@;

    attrMap["abc"] = "xyz";
    attrMap["{http://example.com/ns}baz"] = "value";
    attrMap["{abc}}bak}bar"] = "theNewVal";
    attrMap[nsLocal:foo] = "foo2";
    return x1;
}

function nonSingletonXmlAttributeAccess() returns boolean {
    xml x = xml `<someEle>cont</someEle>`;
    xml y = xml `<elem>More-Stuff</elem>`;
    xml nonSingleton = x + y;
    if (nonSingleton@ is ()) {
        return true;
    }
    return false;
}

function testAttributeAccess() returns string? {
    // Creates an XML element, which has attributes that are bound to a namespace as well as ones that are not.
    xml x1 = xml `<ns0:book ns0:status="available" count="5"/>`;

    // An attribute can also be accessed using the string representation of the qualified name.
    var s = <string?> x1@["{http://sample.com/wso2/a1}status"];
    string y = "";
    if (s is string) {
        justFunc(s);
        y = s;
    }
    return y;
}

function justFunc(string s) {

}

function testAttribMapUpdate() returns [xml, map<string>, xml, map<string>] {
    xml x1 = xml `<Person name="Foo" />`;
    map<string> attr = <map<string>> x1@;

    xml originalXml = x1.clone();
    map<string> originalAttr = attr.clone();

    x1@["name"] = "Bar";
    return [originalXml, originalAttr, x1, attr];
}

function testPrintAttribMap() {
    xml x1 = xml `<Person name="Foo" />`;
    map<string>? attr = x1@;
    io:print(attr);
}
