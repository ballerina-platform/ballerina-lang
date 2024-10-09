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
    assertNonErrorValue(x1.<item>[0]?.id, "0");
    assertNonErrorValue(x1.<item>[1]?.id, "1");
    assertNonErrorValue(x1.<*>[0]?.id, "0");
    assertNonErrorValue(x1.<*>[1]?.id, "1");
    assertNonErrorValue(x1.<items>[0]?.id, ());
    assertNonErrorValue(x1.<item>[0]?.i, ());
    assertNonErrorValue(x1.<item>[1]?.j, ());
    assertNonErrorValue(x1.<item>[0][0]?.id, "0");
    assertNonErrorValue(x1.<item>[0][2]?.id, ());

    xml x2 = xml `<a i="2"><b n="3">book</b><c p="4">10</c><c p="5">10</c></a>`;
    assertNonErrorValue((x2/*)[0]?.n, "3");
    assertNonErrorValue(x2/*.<c>[0]?.p, "4");
    assertNonErrorValue(x2/*.<c>[1]?.p, "5");
    assertNonErrorValue(x2/*.<d>[0]?.p, ());
    assertNonErrorValue((x2/<c>)[0]?.p, "4");
    assertNonErrorValue((x2/<*>)[0]?.n, "3");
    assertNonErrorValue((x2/<*>)[2]?.p, "5");
    assertNonErrorValue((x2/<*>)[0]?.e, ());
    assertNonErrorValue(x2/*.<c>[2]?.p, ());
    assertNonErrorValue((x2/*)[0][0]?.n, "3");
    assertNonErrorValue((x2/*)[1][0]?.p, "4");

    xml x3 = xml `<a i="2"><b n="3">book</b><c p="4"><d g="5">10</d><d g="6">11</d></c></a>`;
    assertNonErrorValue((x3/**/<d>)[0]?.g, "5");
    assertNonErrorValue((x3/**/<d>)[1]?.g, "6");
    assertNonErrorValue((x3/**/<b>)[0]?.n, "3");
    assertNonErrorValue((x3/**/<b>)[0]?.n, "3");
    assertNonErrorValue((x3/**/<e>)[0]?.n, ());
    assertNonErrorValue((x3/**/<d>)[0]?.f, ());
    assertNonErrorValue((x3/**/<d>)[3]?.g, ());
    assertNonErrorValue((x3/**/<d>)[0][0]?.g, "5");
    assertNonErrorValue((x3/**/<c>)[0][1]?.p, ());
}

function testXMLIndexedOptionalAttributeAccessWithNS() {
    xmlns "ns" as ns2;
    xmlns "ps" as ns3;
    xml x1 = xml `<ns2:a id="1">ns2a</ns2:a><ns2:a id="2">ns2a2</ns2:a><ns3:a id="3">ns3a</ns3:a>`;
    assertNonErrorValue(x1.<ns2:a>[0]?.id, "1");
    assertNonErrorValue(x1.<ns2:a>[1]?.id, "2");
    assertNonErrorValue(x1.<ns3:*>[0]?.id, "3");
    assertNonErrorValue(x1.<ns2:*>[0]?.id, "1");
    assertNonErrorValue(x1.<ns2:*>[1]?.id, "2");
    assertNonErrorValue(x1.<ns2:a|ns3:a>[2]?.id, "3");
    assertNonErrorValue(x1.<ns2:c>[0]?.id, ());
    assertNonErrorValue(x1.<ns2:a>[0]?.j, ());
    assertNonErrorValue(x1.<ns2:*>[0]?.k, ());
    assertNonErrorValue(x1.<ns2:a>[3]?.id, ());
    assertNonErrorValue(x1.<ns2:a>[0][0]?.id, "1");
    assertNonErrorValue(x1.<ns2:c>[0][0]?.id, ());
    assertNonErrorValue(x1.<ns2:*>[1][0]?.id, "2");

    xml x2 = xml `<a i="2"><ns2:b n="3">book</ns2:b><ns3:c p="4">10</ns3:c></a>`;
    assertNonErrorValue(x2/*.<ns2:*>[0]?.n, "3");
    assertNonErrorValue(x2/*.<ns3:c>[0]?.p, "4");
    assertNonErrorValue((x2/<ns2:b>)[0]?.n, "3");
    assertNonErrorValue((x2/<ns3:*>)[0]?.p, "4");
    assertNonErrorValue((x2/<ns2:*|ns3:*>)[1]?.p, "4");
    assertNonErrorValue((x2/<ns3:*>)[0]?.k, ());
    assertNonErrorValue((x2/<ns2:b>)[0]?.l, ());
    assertNonErrorValue((x2/<ns2:d>)[0]?.l, ());
    assertNonErrorValue((x2/<ns2:*|ns3:*>)[4]?.p, ());
    assertNonErrorValue(x2/*.<ns2:*>[0][0]?.n, "3");
    assertNonErrorValue(x2/*.<ns2:*|ns3:*>[2][0]?.p, ());

    xml x3 = xml `<a i="2"><ns2:b n="3">book</ns2:b><c p="4"><ns2:d g="5">10</ns2:d><ns2:d g="6">1</ns2:d></c></a>`;
    assertNonErrorValue((x3/**/<ns2:d>)[0]?.g, "5");
    assertNonErrorValue((x3/**/<ns2:d>)[1]?.g, "6");
    assertNonErrorValue((x3/**/<ns2:e>)[0]?.g, ());
    assertNonErrorValue((x3/**/<ns2:d>)[0]?.j, ());
    assertNonErrorValue((x3/**/<ns2:d>)[5]?.g, ());
    assertNonErrorValue((x3/**/<ns2:d>)[1][0]?.g, "6");
    assertNonErrorValue((x3/**/<ns2:c>)[1][0]?.g, ());
}

function testErrorsOnXMLIndexedOptionalAttributeAccess() {
    string errorMessage = "{ballerina/lang.xml}XMLOperationError";

    xml x1 = xml `<!--Comment1--><a id="0"><b><!--Comment2-->></b><!--Comment3--></a>`;
    final string expCommentDetailMessage = "invalid xml attribute access on xml comment";
    assertError(x1[0]?.id, errorMessage, expCommentDetailMessage);
    assertError(x1[0][0]?.id, errorMessage, expCommentDetailMessage);
    assertError((x1[1]/*)[1]?.id, errorMessage, expCommentDetailMessage);
    assertError((x1.<a>/*)[1]?.id, errorMessage, expCommentDetailMessage);
    assertError((x1/*)[1]?.id, errorMessage, expCommentDetailMessage);
    assertError((x1/<b>/*)[0]?.id, errorMessage, expCommentDetailMessage);
    assertError((x1/**/<b>/*)[0]?.id, errorMessage, expCommentDetailMessage);

    xml x2 = xml `<?data?><c id="1"><d><?data2?></d><?target?></c>`;
    final string expPiDetailMessage = "invalid xml attribute access on xml pi";
    assertError(x2[0]?.id, errorMessage, expPiDetailMessage);
    assertError(x2[0][0]?.id, errorMessage, expPiDetailMessage);
    assertError((x2[1]/*)[1]?.id, errorMessage, expPiDetailMessage);
    assertError((x2.<c>/*)[1]?.id, errorMessage, expPiDetailMessage);
    assertError((x2/*)[1]?.id, errorMessage, expPiDetailMessage);
    assertError((x2/<d>/*)[0]?.id, errorMessage, expPiDetailMessage);
    assertError((x2/**/<d>/*)[0]?.id, errorMessage, expPiDetailMessage);

    xml x3 = xml `Text<e id="2"><f>f</f>Another Text</e>`;
    final string expTextDetailMessage = "invalid xml attribute access on xml text";
    assertError(x3[0]?.id, errorMessage, expTextDetailMessage);
    assertError(x3[0][0]?.id, errorMessage, expTextDetailMessage);
    assertError((x3[1]/*)[1]?.id, errorMessage, expTextDetailMessage);
    assertError((x3.<e>/*)[1]?.id, errorMessage, expTextDetailMessage);
    assertError((x3/*)[1]?.id, errorMessage, expTextDetailMessage);
    assertError((x3/<f>/*)[0]?.id, errorMessage, expTextDetailMessage);
    assertError((x3/**/<f>/*)[0]?.id, errorMessage, expTextDetailMessage);
}

type XC xml:Comment;
type XPI xml:ProcessingInstruction;
type XT xml:Text;
type XE xml:Element;

function testXmlAttributeAccessOnXmlUnionTypes() {
    xml<xml:Element|xml:Text> x1 = xml `<a attr="aa">a</a>`;
    string|error result = x1.attr;
    assertNonErrorValue(result, "aa");
    string|error? resultOptional = x1?.attr;
    assertNonErrorValue(resultOptional, "aa");

    xml<xml:Element|xml:Comment> x2 = xml `<b attr="ba">b</b>`;
    assertNonErrorValue(x2.attr, "ba");
    assertNonErrorValue(x2?.attr, "ba");

    xml<xml:Element|xml:ProcessingInstruction> x3 = xml `<c attr="ca">c</c>`;
    assertNonErrorValue(x3.attr, "ca");
    assertNonErrorValue(x3?.attr, "ca");

    xml<xml:Element|xml:Text|xml:ProcessingInstruction> x4 = xml `<d attr="da">d</d>`;
    assertNonErrorValue(x4.attr, "da");
    assertNonErrorValue(x4?.attr, "da");

    xml<xml:Element|xml:Comment|xml:ProcessingInstruction> x5 = xml `<e attr="ea">e</e>`;
    result = x5.attr;
    assertNonErrorValue(result, "ea");
    resultOptional = x5?.attr;
    assertNonErrorValue(resultOptional, "ea");

    xml<xml:Element|xml:Comment|xml:Text> x6 = xml `<f attr="fa">f</f>`;
    assertNonErrorValue(x6.attr, "fa");
    assertNonErrorValue(x6?.attr, "fa");

    xml<xml:Element|xml:ProcessingInstruction|xml:Comment|xml:Text> x7 = xml `<g attr="ga">g</g>`;
    result = x7.attr;
    assertNonErrorValue(result, "ga");
    resultOptional = x7?.attr;
    assertNonErrorValue(resultOptional, "ga");

    xml:Element|xml:ProcessingInstruction x8 = xml `<h attr="ha">h</h>`;
    assertNonErrorValue(x8.attr, "ha");
    assertNonErrorValue(x8?.attr, "ha");

    xml:Element|xml:Text x9 = xml `<j attr="ja">j</j>`;
    result = x9.attr;
    assertNonErrorValue(result, "ja");
    resultOptional = x9?.attr;
    assertNonErrorValue(resultOptional, "ja");

    xml:Element|xml:Comment x10 = xml `<k attr="ka">k</k>`;
    assertNonErrorValue(x10.attr, "ka");
    assertNonErrorValue(x10?.attr, "ka");

    XC|XE x11 = xml `<l attr="la">l</l>`;
    result = x11.attr;
    assertNonErrorValue(result, "la");
    resultOptional = x11?.attr;
    assertNonErrorValue(resultOptional, "la");
}

function testErrorsOnXmlAttributeAccessOnNonXmlElementValue() {
    string errorMessage = "{ballerina/lang.xml}XMLOperationError";

    xml:Comment x1 = xml `<!--comment-->`;
    string|error result = x1.attr;
    assertError(result, errorMessage, "invalid xml attribute access on xml comment");
    string|error? resultOptional = x1?.attr;
    assertError(resultOptional, errorMessage, "invalid xml attribute access on xml comment");

    xml:Text x2 = xml `text`;
    assertError(x2.attr, errorMessage, "invalid xml attribute access on xml text");
    assertError(x2?.attr, errorMessage, "invalid xml attribute access on xml text");

    xml:ProcessingInstruction x3 = xml `<?data?>`;
    assertError(x3.attr, errorMessage, "invalid xml attribute access on xml pi");
    assertError(x3?.attr, errorMessage, "invalid xml attribute access on xml pi");

    xml<xml:Comment|xml:Text> x4 = xml `<!--comment-->text`;
    result = x4.attr;
    assertError(result, errorMessage, "invalid xml attribute access on xml sequence");
    resultOptional = x4?.attr;
    assertError(resultOptional, errorMessage, "invalid xml attribute access on xml sequence");

    xml<xml:Comment|xml:ProcessingInstruction> x5 = xml `<?data?><!--comment-->`;
    assertError(x5.attr, errorMessage, "invalid xml attribute access on xml sequence");
    assertError(x5?.attr, errorMessage, "invalid xml attribute access on xml sequence");

    xml<xml:Text|xml:ProcessingInstruction> x6 = xml `<?data?>text`;
    assertError(x6.attr, errorMessage, "invalid xml attribute access on xml sequence");
    assertError(x6?.attr, errorMessage, "invalid xml attribute access on xml sequence");

    xml<xml:Text|xml:ProcessingInstruction|xml:Comment> x7 = xml `<!--comment--><?data?>text`;
    result = x7.attr;
    assertError(result, errorMessage, "invalid xml attribute access on xml sequence");
    resultOptional = x7?.attr;
    assertError(resultOptional, errorMessage, "invalid xml attribute access on xml sequence");

    xml<xml:Element|xml:Text> x8 = xml `text`;
    assertError(x8.attr, errorMessage, "invalid xml attribute access on xml text");
    assertError(x8?.attr, errorMessage, "invalid xml attribute access on xml text");

    xml<xml:Element|xml:Comment> x9 = xml `<!--comment-->`;
    assertError(x9.attr, errorMessage, "invalid xml attribute access on xml comment");
    assertError(x9?.attr, errorMessage, "invalid xml attribute access on xml comment");

    xml<xml:Element|xml:ProcessingInstruction> x10 = xml `<?target?>`;
    assertError(x10.attr, errorMessage, "invalid xml attribute access on xml pi");
    assertError(x10?.attr, errorMessage, "invalid xml attribute access on xml pi");

    xml<xml:Element|xml:Text> x11 = xml `<a attr="attr">a</a>text`;
    assertError(x11.attr, errorMessage, "invalid xml attribute access on xml sequence");
    assertError(x11?.attr, errorMessage, "invalid xml attribute access on xml sequence");

    xml<xml:Element|xml:Comment> x12 = xml `<a attr="attr">a</a><!--comment-->`;
    assertError(x12.attr, errorMessage, "invalid xml attribute access on xml sequence");
    assertError(x12?.attr, errorMessage, "invalid xml attribute access on xml sequence");

    xml<xml:Element|xml:ProcessingInstruction> x13 = xml `<a attr="attr">a</a><?target?>`;
    assertError(x13.attr, errorMessage, "invalid xml attribute access on xml sequence");
    assertError(x13?.attr, errorMessage, "invalid xml attribute access on xml sequence");

    xml<xml:Element|xml:ProcessingInstruction|xml:Comment|xml:Text> x14 =
            xml `<a attr="attr">a</a><?target?><!--comment-->text`;
    assertError(x14.attr, errorMessage, "invalid xml attribute access on xml sequence");
    assertError(x14?.attr, errorMessage, "invalid xml attribute access on xml sequence");

    XC x15 = xml `<!--comment-->`;
    assertError(x15.attr, errorMessage, "invalid xml attribute access on xml comment");
    assertError(x15?.attr, errorMessage, "invalid xml attribute access on xml comment");

    XPI x16 = xml `<?data?>`;
    result = x16.attr;
    assertError(result, errorMessage, "invalid xml attribute access on xml pi");
    resultOptional = x16?.attr;
    assertError(resultOptional, errorMessage, "invalid xml attribute access on xml pi");

    XT x17 = xml `text`;
    assertError(x17.attr, errorMessage, "invalid xml attribute access on xml text");
    assertError(x17?.attr, errorMessage, "invalid xml attribute access on xml text");

    xml:Comment|xml:Text x18 = xml `<!--comment-->`;
    assertError(x18.attr, errorMessage, "invalid xml attribute access on xml comment");
    assertError(x18?.attr, errorMessage, "invalid xml attribute access on xml comment");

    xml:Text|xml:ProcessingInstruction x19 = xml `text`;
    result = x19.attr;
    assertError(result, errorMessage, "invalid xml attribute access on xml text");
    resultOptional = x19.attr;
    assertError(resultOptional, errorMessage, "invalid xml attribute access on xml text");

    xml:ProcessingInstruction|xml:Comment x20 = xml `<?data?>`;
    assertError(x20.attr, errorMessage, "invalid xml attribute access on xml pi");
    assertError(x20?.attr, errorMessage, "invalid xml attribute access on xml pi");

    xml:ProcessingInstruction|xml:Element x21 = xml `<?target?>`;
    assertError(x21.attr, errorMessage, "invalid xml attribute access on xml pi");
    assertError(x21?.attr, errorMessage, "invalid xml attribute access on xml pi");
}

function assertNonErrorValue(anydata|error actual, anydata expected) {
    if actual is error {
        panic error("expected [" + expected.toString() + "] " + ", but got error with message " + actual.message());
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
