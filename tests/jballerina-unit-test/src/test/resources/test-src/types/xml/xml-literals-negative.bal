import ballerina/lang.'int as x;

function testRestrictedElementPrefix() returns (xml) {
    xml x = xml `<xmlns:foo>hello</xmlns:foo>`;
    return x;
}

function xmlUndeclaredElementPrefix() returns (xml) {
    xml x = xml `<ns1:foo>hello</ns1:foo>`;
    return x;
}

function xmlTemplateWithNonXML() {
    map<any> m = {};
    xml x = xml `<root xmlns="http://default/namespace">${m}</root>`;
}

function defineEmptyNamespaceInline() {
    xml x = xml `<root xmlns:ns0=""/>`;
}

function testRedeclareNamespaces() {
    xmlns "http://sample.com/wso2/a2" as ns0;
    xmlns "http://sample.com/wso2/c2";
    xmlns "http://sample.com/wso2/d2" as ns3;
    
    if (true) {
        xmlns "http://sample.com/wso2/a3" as ns0;
    }
}

function foo() {
    xmlns "http:wso2.com/" as x;
}

function updateQname() {
    xmlns "http://wso2.com/" as ns0;
    ns0:foo = "{uri}localname";
}

function undefinedNamespace() {
    xml x = xml `<root/>`;
    if (true) {
        xmlns "http://wso2.com/" as ns0;
    }
    string|error s = x.ns0:foo;
}

function defineEmptyNamespace() {
    xmlns "" as ns0;
}

function testMismatchingElementTags() {
    var x = xml `<book></book12>`;
}

function dummyFunctionToUseMath() {
    _ = x:abs(-123);
}

function testXmlNsInterpolation() returns xml {
    string ns = "http://wso2.com/";
    xml x = xml `<foo xmlns="${ns}" xmlns:foo="${ns}">hello</foo>`;
    return x;
}

function testXMLSequence() {
    xml<'xml:Text> x4 = xml `<!--comment-->text1`;
    'xml:Text x10 = xml `<!--comment-->text1`;
    xml<'xml:Text|'xml:Comment> x7 = xml `<root> text1<foo>100</foo><foo>200</foo></root>`;
    xml<'xml:Text|'xml:Comment> x8 = xml `<root> text1<foo>100</foo><foo>200</foo></root><?foo?>`;
    xml<'xml:Text>|xml<'xml:Comment> x11 = xml `<root> text1<foo>100</foo><foo>200</foo></root>`;
    xml<'xml:Text>|xml<'xml:Comment> x12 = xml `<root> text1<foo>100</foo><foo>200</foo></root><?foo?>`;
    'xml:Text|'xml:Comment x15 = xml `<root> text1<foo>100</foo><foo>200</foo></root>`;
    'xml:Text|'xml:Comment x16 = xml `<root> text1<foo>100</foo><foo>200</foo></root><?foo?>`;
    'xml:Text|'xml:Comment x17 = xml `<!--comment-->text1`;
    xml<'xml:Text>|'xml:Comment x18 = xml `<!--comment-->text1`;
    'xml:Element x20 = xml `<foo>100</foo><foo>200</foo>`;
    'xml:Comment|'xml:Element x21 = xml `<foo>foo</foo><bar/><!--comment-->`;
    'xml:Comment|'xml:Element x22 = xml `<foo>foo</foo><bar/>`;

    'xml:ProcessingInstruction x23 = xml `<?foo?><?faa?>`;
    'xml:Element x24 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    'xml:Comment x25 = xml `<!--comment1--><!--comment2-->`;

    string val1 = "text1";
    xml<'xml:ProcessingInstruction> x26 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    xml<'xml:ProcessingInstruction> x27 = xml `text1 ${val1}`;
    xml<'xml:ProcessingInstruction> x28 = xml `<!--comment1--><!--comment2-->`;

    xml<'xml:Element> x29 = xml `text1 ${val1}`;
    xml<'xml:Element> x30 = xml `<?foo?><?faa?>`;
    xml<'xml:Element> x31 = xml `<!--comment1--><!--comment2-->`;

    xml<'xml:Comment> x32 = xml `<?foo?><?faa?>`;
    xml<'xml:Comment> x33 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    xml<'xml:Comment> x34 = xml `text1 ${val1}`;

    xml<'xml:Text> x35 = xml `<!--comment1--><!--comment2-->`;
    xml<'xml:Text> x36 = xml `<foo>Anne</foo><fuu>Peter</fuu>`;
    xml<'xml:Text> x37 = xml `<?foo?><?faa?>`;
}

function testUndefinedVariable() {
    xml c = xml `text1 text2 ${b}`;
}

function testIncompatibleTypesInTemplates() {
    map<string> b = {};
    error err = error("xml error");
    xml c = xml `text1 text2 ${b} ${err}`;
}

function testXMLIncompatibleValueAssignment() {
    xml<xml<xml:Element>> e1 = xml `<foo>100</foo><bar>200</bar>`;
    xml:Element e2 = e1;
    xml<xml<xml<'xml:Comment>>> c1 = xml `<!--some comment--><!--some comment-->`;
    xml:Comment c2 = c1;
    xml<xml<xml<'xml:ProcessingInstruction>>> p1 = xml `<?foo?><?faa?>`;
    xml:ProcessingInstruction p2 = p1;
    xml x1 = xml `<e/>/`;
    xml:Text t1 = x1;
    xml<xml> x2 = xml `<elem/>`;
    xml:Text t2 = x2;
}

function testSyntaxErrorsInXMLCDATASections() {
    xml x1 = xml `<![CDATA[ some text -->< { } &`;
}

function testXmlNsInterpolationWithinQuery() {
    string ns = "http://wso2.com/";
    xml _ = xml `<empRecords xmlns:inlineNS="${ns}">
             ${from int i in 1 ... 3
            select xml `<empRecord employeeId="${i}"><inlineNS:id>${i}</inlineNS:id></empRecord>`}
          </empRecords>;`;
}

function testQueryInXMLTemplateExprNegative() {
    var _ = xml `<doc>${from string i in ["A", "B"] select i + "C"}</doc>`;
    xml _ = xml `<doc>${from string i in ["A", "B"] select i + "C"}</doc>`;
    xml<xml:Element> _ = xml `<doc>${from var i in [1, 2, 3] select i * 2}</doc>`;
    var _ = xml `<doc>${from int i in 0..<2 select `<foo></foo>`}</doc>`;
    var _ = xml `<doc>${<string>from int i in 0..<2 select `<foo></foo>`}</doc>`;
}

function textInvalidXmlSequence() {
    xml<'xml:Text>|xml<'xml:Comment> x38 = xml `<!--comment-->text1`;
    xml<'xml:Element>|'xml:Text x39 = xml `<root><foo><foo></foo>3</foo></root>3`;
    xml<xml<'xml:Text>>|xml<xml<'xml:Comment>> x40 = xml `<!--comment-->text1`;
}

function testMissingClosingGTToken() {
    xml x1 = xml `<a/a>`;
    xml x2 = xml `<a n="a"/a>`;
}

type X xml;
type XE xml<xml:Element>;
type XP xml<xml:ProcessingInstruction>;
type XC xml<xml:Comment>;
type UX XE|XP|XC|xml:Text;

function testXMLInvalidSubtype() {
    X x1 = xml `<a></a><b></b>`;
    UX _ = x1;

    X x2 = xml `<?target instructions?><?a data?>`;
    UX _ = x2;

    X x3 = xml `<!--comment one--><!--comment two-->`;
    UX _ = x3;

    X x4 = xml `random sequence of text`;
    UX _ = x4;

    X x5 = xml `<a></a><b></b><?target instructions?><?c data?>`;
    UX _ = x5;

    X x6 = xml `<a></a><b></b><!--comment one--><!--comment two-->`;
    UX _ = x6;

    X x7 = xml `<a></a><b></b>random text`;
    UX _ = x7;

    X x8 = xml `<?target instructions?><?a data?>text`;
    UX _ = x8;

    X x9 = xml `<?target instructions?><?a data?><!--comment one--><!--comment two-->`;
    UX _ = x9;

    X x10 = xml `<!--comment one--><!--comment two-->text`;
    UX _ = x10;

    X x11 = xml `<a></a><b></b><?d data?><?c data?>?><!--comment--><!--comment-->text`;
    UX _ = x11;
}
