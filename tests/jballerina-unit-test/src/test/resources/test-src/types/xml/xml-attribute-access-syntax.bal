import ballerina/lang.'xml;

type XMLElement xml:Element;
type XMLElement2 xml<xml:Element>;
type XMLElement3 xml<xml<xml:Element>>;
type XMLElement4 xml;

function getElementAttrBasic() returns error? {
    xml x = xml `<root attr="attr-val"><a></a><b></b></root>`;
    xml<xml:Element> x2 = xml `<root attr="attr-val"><a></a><b></b></root>`;
    xml<xml<xml:Element>> x3 = xml `<root attr="attr-val"><a></a><b></b></root>`;
    assert(check x.attr, "attr-val");
    assert(check x2.attr, "attr-val");
    assert(check x3.attr, "attr-val");
    assertError(x.name, "{ballerina/lang.xml}XMLOperationError", "attribute 'name' not found");
    assertError(x2.name, "{ballerina/lang.xml}XMLOperationError", "attribute 'name' not found");
    assertError(x3.name, "{ballerina/lang.xml}XMLOperationError", "attribute 'name' not found");
}

function getOptionalElementAttrBasic() returns error? {
    xml:Element x = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    xml<xml:Element> x2 = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    xml<xml<xml:Element>> x3 = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    xml x4 = xml `<root1 attr="attr-val"><a></a><b></b></root1><root2 attr="attr-val"><a></a><b></b></root2>`;
    assert(check x?.attr, "attr-val");
    assert(check x2?.attr, "attr-val");
    assert(check x3?.attr, "attr-val");
    assert(check x?.name, ());
    assert(check x2?.name, ());
    assert(check x3?.name, ());
    assertError(x4?.attr, "{ballerina/lang.xml}XMLOperationError", "invalid xml attribute access on xml sequence");
}

function getUserDefinedTypeElementAttrBasic() returns error? {
    XMLElement x = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    XMLElement2 x2 = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    XMLElement3 x3 = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    assert(check x.attr, "attr-val");
    assert(check x2.attr, "attr-val");
    assert(check x3.attr, "attr-val");
    assertError(x.name, "{ballerina/lang.xml}XMLOperationError", "attribute 'name' not found");
    assertError(x2.name, "{ballerina/lang.xml}XMLOperationError", "attribute 'name' not found");
    assertError(x3.name, "{ballerina/lang.xml}XMLOperationError", "attribute 'name' not found");
}

function getUserDefinedTypeOptionalElementAttrBasic() returns error? {
    XMLElement x = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    XMLElement2 x2 = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    XMLElement3 x3 = xml `<elem xmlns="ns-uri" attr="attr-val" xml:space="default"></elem>`;
    XMLElement4 x4 = xml `<root1 attr="attr-val"><a></a><b></b></root1><root2 attr="attr-val"><a></a><b></b></root2>`;
    assert(check x?.attr, "attr-val");
    assert(check x2?.attr, "attr-val");
    assert(check x3?.attr, "attr-val");
    assert(check x?.name, ());
    assert(check x2?.name, ());
    assert(check x3?.name, ());
    assertError(x4?.attr, "{ballerina/lang.xml}XMLOperationError", "invalid xml attribute access on xml sequence");
}

function getAttrOfASequence() returns string|error? {
    xml x = xml `<root attr="attr-val"><a attr="a-attr"></a><b attr="b-attr"></b></root>`;
    string|error? name = x/*.attr; // get children sequences' attribute `attr`
    return name;
}

function getElementAttrWithNSPrefix() returns string|error? {
    xmlns "www.url.com" as ns;
    xml x = xml `<root attr="attr-val" ns:attr="attr-with-ns-val"><a></a><b></b></root>`;
    string|error? val = x.ns:attr;
    return val;
}

function usePredefinedXMLNamespace() returns string|error? {
    xml x = xml `<root xmlns="the-url" attr="the attr" xml:space="preserve"></root>`;
    return x.'xml:space;
}

function testXMLAfterRemoveAttribute() {
    map<string> attributes = {"{http://www.w3.org/2000/xmlns/}ns0":"http://sample.com/test","status":"online"};
    xml:Element element = xml:createElement("{http://sample.com/test}bookStore", attributes, xml ``);
    assert(element.toString(), "<ns0:bookStore xmlns:ns0=\"http://sample.com/test\" status=\"online\"/>");

    _ = element.getAttributes().remove("{http://www.w3.org/2000/xmlns/}ns0");
    assert(element.toString(), "<bookStore xmlns=\"http://sample.com/test\" status=\"online\"/>");
}

function testXMLIndexedOptionalAttributeAccess() {
    xml x1 = xml `<item id="0">book</item><item id="1">pen</item>`;
    assertNotErrorAndValue(x1.<item>[0]?.id, "0");
    assertNotErrorAndValue(x1.<item>[1]?.id, "1");
    assertNotErrorAndValue(x1.<*>[0]?.id, "0");
    assertNotErrorAndValue(x1.<*>[1]?.id, "1");
    assertNotErrorAndValue(x1.<items>[0]?.id, ());
    assertNotErrorAndValue(x1.<item>[0]?.i, ());
    assertNotErrorAndValue(x1.<item>[1]?.j, ());

    xmlns "ns" as ns2;
    xmlns "ps" as ns3;
    xml x2 = xml `<ns2:a id="1">ns2a</ns2:a><ns2:a id="2">ns2a2</ns2:a><ns3:a id="3">ns3a</ns3:a>`;
    assertNotErrorAndValue(x2.<ns2:a>[0]?.id, "1");
    assertNotErrorAndValue(x2.<ns2:a>[1]?.id, "2");
    assertNotErrorAndValue(x2.<ns3:*>[0]?.id, "3");
    assertNotErrorAndValue(x2.<ns2:*>[0]?.id, "1");
    assertNotErrorAndValue(x2.<ns2:*>[1]?.id, "2");
    assertNotErrorAndValue(x2.<ns2:a|ns3:a>[2]?.id, "3");
    assertNotErrorAndValue(x2.<ns2:c>[0]?.id, ());
    assertNotErrorAndValue(x2.<ns2:a>[0]?.j, ());
    assertNotErrorAndValue(x2.<ns2:*>[0]?.k, ());
    assertNotErrorAndValue(x2.<ns2:a>[3]?.id, ());

    xml x3 = xml `<a i="2"><b n="3">book</b><c p="4">10</c><c p="5">10</c></a>`;
    assertNotErrorAndValue((x3/*)[0]?.n, "3");
    assertNotErrorAndValue(x3/*.<c>[0]?.p, "4");
    assertNotErrorAndValue(x3/*.<c>[1]?.p, "5");
    assertNotErrorAndValue(x3/*.<d>[0]?.p, ());
    assertNotErrorAndValue((x3/<c>)[0]?.p, "4");
    assertNotErrorAndValue((x3/<*>)[0]?.n, "3");
    assertNotErrorAndValue((x3/<*>)[2]?.p, "5");
    assertNotErrorAndValue((x3/<*>)[0]?.e, ());
    assertNotErrorAndValue(x3/*.<c>[2]?.p, ());

    xml x4 = xml `<a i="2"><ns2:b n="3">book</ns2:b><ns3:c p="4">10</ns3:c></a>`;
    assertNotErrorAndValue(x4/*.<ns2:*>[0]?.n, "3");
    assertNotErrorAndValue(x4/*.<ns3:c>[0]?.p, "4");
    assertNotErrorAndValue((x4/<ns2:b>)[0]?.n, "3");
    assertNotErrorAndValue((x4/<ns3:*>)[0]?.p, "4");
    assertNotErrorAndValue((x4/<ns2:*|ns3:*>)[1]?.p, "4");
    assertNotErrorAndValue((x4/<ns3:*>)[0]?.k, ());
    assertNotErrorAndValue((x4/<ns2:b>)[0]?.l, ());
    assertNotErrorAndValue((x4/<ns2:d>)[0]?.l, ());
    assertNotErrorAndValue((x4/<ns2:*|ns3:*>)[4]?.p, ());

    xml x5 = xml `<a i="2"><b n="3">book</b><c p="4"><d g="5">10</d><d g="6">11</d></c></a>`;
    assertNotErrorAndValue((x5/**/<d>)[0]?.g, "5");
    assertNotErrorAndValue((x5/**/<d>)[1]?.g, "6");
    assertNotErrorAndValue((x5/**/<b>)[0]?.n, "3");
    assertNotErrorAndValue((x5/**/<b>)[0]?.n, "3");
    assertNotErrorAndValue((x5/**/<e>)[0]?.n, ());
    assertNotErrorAndValue((x5/**/<d>)[0]?.f, ());
    assertNotErrorAndValue((x5/**/<d>)[3]?.g, ());

    xml x6 = xml `<a i="2"><ns2:b n="3">book</ns2:b><c p="4"><ns2:d g="5">10</ns2:d><ns2:d g="6">1</ns2:d></c></a>`;
    assertNotErrorAndValue((x6/**/<ns2:d>)[0]?.g, "5");
    assertNotErrorAndValue((x6/**/<ns2:d>)[1]?.g, "6");
    assertNotErrorAndValue((x6/**/<ns2:e>)[0]?.g, ());
    assertNotErrorAndValue((x6/**/<ns2:d>)[0]?.j, ());
    assertNotErrorAndValue((x6/**/<ns2:d>)[5]?.g, ());
}

function assertNotErrorAndValue(anydata|error actual, anydata expected) {
    if actual is error {
        string reason = "expected [" + expected.toString() + "] " + ", but got error with message " + actual.message();
        error e = error(reason);
        panic e;
    }
    assert(actual, expected);
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}

type Error error<record { string message; }>;

function assertError(any|error value, string errorMessage, string expDetailMessage) {
    if value is Error {
        if (value.message() != errorMessage) {
            panic error("Expected error message: " + errorMessage + " found: " + value.message());
        }

        if value.detail().message == expDetailMessage {
            return;
        }
        panic error("Expected error detail message: " + expDetailMessage + " found: " + value.detail().message);
    }
    panic error("Expected: Error, found: " + (typeof value).toString());
}
