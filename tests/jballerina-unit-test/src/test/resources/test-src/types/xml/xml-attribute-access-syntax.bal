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
    assert(element.toString(), "<ns0:bookStore xmlns:ns0=\"http://sample.com/test\" status=\"online\"></ns0:bookStore>");

    _ = element.getAttributes().remove("{http://www.w3.org/2000/xmlns/}ns0");
    assert(element.toString(), "<bookStore xmlns=\"http://sample.com/test\" status=\"online\"></bookStore>");
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
