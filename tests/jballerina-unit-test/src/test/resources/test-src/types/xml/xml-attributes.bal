import ballerina/jballerina.java;
import ballerina/lang.'xml;

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
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    // without namespaceUri
    xAttr["foo1"] = "bar1";

    // with a new namespaceUri
    xAttr["{http://sample.com/wso2/e}foo2"] = "bar2";

    // with an existing namespaceUri
    xAttr["{http://sample.com/wso2/f}foo3"] = "bar3";

    return x1;
}

function testAddAttributeWithQName() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();

    xAttr[ns0:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_1() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    //same uri, non existant prefix
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    xmlns "http://sample.com/wso2/f" as pre;
    xAttr[pre:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_2() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    //same uri, existing, non used prefix
    xmlns "http://sample.com/wso2/f/" as ns5;
    xAttr[ns5:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_3() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    //same uri, existing, used prefix
    xmlns "http://sample.com/wso2/f/" as ns4;
    xAttr[ns4:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_4() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    //different uri, existing, non used prefix
    xmlns "http://sample.com/wso2/f/t" as ns5;
    xAttr[ns5:foo1] = "bar1";
    return x1;
}

function testAddAttributeWithDiffQName_5() returns (xml) {
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns4="http://sample.com/wso2/f/" xmlns:ns5="http://sample.com/wso2/f/" ns4:diff="yes"></root>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    //adding attribute with default namespace
    xAttr["{http://sample.com/wso2/c1}foo1"] = "bar1";
    //adding attribute with empty namepsace
    xAttr["{}foo2"] = "bar2";
    //adding attribute without a namespace
    xAttr["foo3"] = "bar3";
    return x1;
}

function testAddAttributeWithoutLocalname() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    xAttr["{http://sample.com/wso2/e}"] = "bar";
    return x1;
}

function testAddAttributeWithEmptyNamespace() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    xAttr["{}foo1"] = "bar";
    return x1;
}

function testAddNamespaceAsAttribute() returns [xml, xml] {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    var xAttr = let var x12 = <'xml:Element> x1 in x12.getAttributes();
    xAttr["{http://www.w3.org/2000/xmlns/}ns4"] = "http://wso2.com";

    var x2 = xml `<root xmlns="http://ballerinalang.org/" xmlns:ns3="http://sample.com/wso2/f"></root>`;
    var xAttr2 = let var x22 = <'xml:Element> x2 in x22.getAttributes();
    xAttr2["{http://ballerinalang.org/}att"] = "http://wso2.com";

    return [x1, x2];
}

function testUpdateNamespaceAsAttribute() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    xAttr["{http://www.w3.org/2000/xmlns/}ns3"] = "http://wso2.com";

    return (x1);
}

function testUpdateAttributeWithString() returns (xml) {
    xmlns "http://defaultNs/";
    
    var x1 = xml `<root xmlns:ns0="http://sample.com/wso2/e" foo1="bar1" ns0:foo2="bar2" foo3="bar3"/>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();

    // with empty namespaceUri
    xAttr["foo1"] = "newbar1";
    
    // with a new namespaceUri
    xAttr["{http://sample.com/wso2/e}foo2"] = "newbar2";
    
    // without namespaceUri
    xAttr["foo3"] = "newbar3";
    
    return x1;
}

function testUpdateAttributeWithString_1() returns (xml) {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns:ns0Kf5j="http://sample.com/wso2/e" foo1="bar1" ns0Kf5j:foo2="bar2" ns4:foo3="bar3"/>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();

    //with a new uri than the assigned uri for the attribute
    xAttr["{http://sample.com/wso2/f/t}foo3"] = "newbar3";

    return x1;
}

function testUpdateAttributeWithQName() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" ns3:foo2="bar2"/>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();

    // with a matching namespaceUri and prefix
    xAttr[ns0:foo1] = "newbar1";
    
    // with a matching namespaceUri but different prefix
    xmlns "http://sample.com/wso2/f" as ns4;
    xAttr[ns4:foo2] = "newbar2";

    return x1;
}

function testUpdateAttributeWithQName_1() returns (xml) {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns0="http://sample.com/wso2/a1" xmlns:ns5="http://sample.com/wso2/a1" ns0:foo1="bar1" ns3:foo2="bar2"/>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    // with a matching namespaceUri but different prefix
    xmlns "http://sample.com/wso2/a1" as pre;
    xAttr[pre:foo1] = "newaddedbar1";

    return x1;
}

function testGetAttributeWithString() returns [string|error, string?, string?] {
    xmlns "http://sample.com/wso2/f";
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns:ns0="http://sample.com/wso2/e" foo1="bar1" ns0:foo2="bar2" ns4:foo3="bar3"/>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    return [x1.foo1, xAttr["{http://sample.com/wso2/e}foo2"], xAttr["{http://sample.com/wso2/eee}foo2"]];
}

function testGetAttributeWithoutLocalname() returns (string?) {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns="http://sample.com/wso2/e" foo1="bar1" ns4:foo3="bar3"/>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    return xAttr["{http://sample.com/wso2/e}"];
}

function testGetNamespaceAsAttribute() returns (string?) {
    var x1 = xml `<root xmlns:ns4="http://sample.com/wso2/f" xmlns="http://sample.com/wso2/e" foo1="bar1" ns4:foo3="bar3"/>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    return xAttr["{http://www.w3.org/2000/xmlns/}ns4"];
}

function testGetAttributeWithQName() returns [string?, string?, string?] {
    var x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f" xmlns:ns0="http://sample.com/wso2/a1" ns0:foo1="bar1" ns3:foo2="bar2"/>`;
    var xAttr = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    xmlns "http://sample.com/wso2/f" as ns4;
    return [xAttr[ns0:foo1], xAttr[ns4:foo2], xAttr[ns1:foo2]];
}

function testUsingQNameAsString () returns [string, string] {
    string s1 = ns0:wso2;
    string temp = "  " + ns0:ballerina + "  ";
    string s2 = temp.trim();
    return [s1, s2];
}

function testGetAttributeFromSingletonSeq() returns (string?) {
    var x1 = xml `<root><child xmlns:p1="http://wso2.com/" xmlns:p2="http://sample.com/wso2/a1/" p1:foo="bar"/></root>`;
    xml x2 = x1/*;
    var xAttr = let var x3 = <'xml:Element> x2 in x3.getAttributes();
    return xAttr["{http://wso2.com/}foo"];
}

function mapOperationsOnXmlAttribute() returns [int?, string[]?, boolean] {
    var x1 = xml `<child foo="bar"/>`;
    var attrMap = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    return [attrMap.length(), attrMap.keys(), true];
}

function testPrintAttribMap() {
    xml x1 = xml `<Person name="Foo" />`;
    var attrMap = let var x2 = <'xml:Element> x1 in x2.getAttributes();
    print(attrMap);
}

function testCharacterReferencesInXmlAttributeValue() {
    string u9 = "\u{9}";
    string uA = "\u{A}";
    string uD = "\u{D}";

    var x = xml`<p att="x&amp;y|${u9}|${uA}|${uD}|&lt;&gt;"/>`; // last segment: space followed by a tab character
    string att = checkpanic x.att;
    string expected = string `x&y|${u9}|${uA}|${uD}|<>`;
    if (att == expected) {
        return;
    }
    panic error("Assertion error, expected `" + expected + "`, found `" + att + "`");
}

function testAttributesInEmptyXMLSequence() {
    xml testXml = xml `
            <ClinicalDocument>
                <realmCode code="US"/>
                <typeId root="2.16.840.1.113883.1.3" extension="POCD_HD000040"/>
                <templateId root="2.16.840.1.113883"/>
            </ClinicalDocument>
        `;

    xml idElement = testXml/<id>;
    string|error idVal = idElement.root;
    string? optionalIdVal = checkpanic idElement?.root;
    assertEquality((), optionalIdVal);
    if idVal is error {
        error error(_, ...details) = idVal;
        assertEquality("empty xml sequence cannot have attributes", details["message"]);
        return;
    }
    panic error("expected 'error', found 'string'");
}

public function print(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
