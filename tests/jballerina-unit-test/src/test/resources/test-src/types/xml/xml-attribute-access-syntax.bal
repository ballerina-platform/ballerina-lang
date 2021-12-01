import ballerina/lang.'xml;

function getElementAttrBasic() returns string|error? {
    xml x = xml `<root attr="attr-val"><a></a><b></b></root>`;
    string|error? val = x.attr;
    return val;
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
