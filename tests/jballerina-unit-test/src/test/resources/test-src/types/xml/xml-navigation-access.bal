 import ballerina/lang.'xml as xmllib;

function testXMLNavigationOnSingleElement() returns [xml, xml, xml, xml, xml] {
    xml x1 = xml `<root><child attr="attr-val"></child></root>`;
    xml x2 = x1/<child>;
    xml x3 = x1/*; // all children of each element of x1
    xml x4 = x1/<*>; // all elements children of each element of x1
    xml x5 = x1/**/<child>; // descendants
    xml x6 = x1/<child>[0]; // first child of each element in x1 that match ns:child

    return [x2, x3, x4, x5, x6];
}

function testXMLNavigationOnSingleElementWithNamespaces() returns [xml, xml, xml, xml, xml] {
    xmlns "foo" as ns;
    xmlns "bar" as k;
    xml x1 = xml `<ns:root><ns:child></ns:child></ns:root>`;
    xml x2 = x1/<ns:child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<ns:child>;
    xml x6 = x1/<ns:child>[0];

    return [x2, x3, x4, x5, x6];
}


function testXMLNavigationOnSingleElementReferToDefaultNS()
        returns [xml, xml, xml, xml, xml, xml, int, xml] {
    xmlns "foo";
    xmlns "bar" as k;
    xml x1 = xml `<root><child></child></root>`;
    xml x2 = x1/<child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<child>;
    xml x6 = x1/<child>[0];
    xml x7 = (x1/*)[0];
    xml x8 = (x1/*)[1];
    xml x9 = (x1/**/<child>)[0];

    return [x2, x3, x4, x5, x6, x7, x8.length(), x9];
}


function testXMLNavigationOnSingleElementReferToDefaultNSViaPrefix() returns [xml, xml, xml, xml, xml] {
    xmlns "foo";
    xmlns "bar" as k;
    xmlns "foo" as ns;
    xml x1 = xml `<root><child></child></root>`;
    xml x2 = x1/<ns:child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<ns:child>;
    xml x6 = x1/<ns:child>[0];

    return [x2, x3, x4, x5, x6];
}

function testXMLNavigationOnSequence() returns [xml, xml, xml, xml, xml] {
    xml x1 = xmllib:concat(xml `<root><child>A</child></root>`,
                        xml `<root><child>B</child></root>`,
                        xml `<item><child>C</child><it-child>D</it-child>TEXT</item>`);
    xml x2 = x1/<child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<child>;
    xml x6 = x1/<child>[0];
    return [x2, x3, x4, x5, x6];
}

function testXMLNavigationOnSequenceWithNamespaces() returns [xml, xml, xml, xml, xml] {
    xmlns "foo";
    xmlns "bar" as k;
    xmlns "foo" as ns;
    xml x1 = xmllib:concat(xml `<root><child>A</child></root>`,
                        xml `<root><ns:child>B</ns:child></root>`,
                        xml `<item><k:child>C</k:child><it-child>D</it-child>TEXT</item>`);
    xml x2 = x1/<child>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<ns:child>;
    xml x6 = x1/<child>[0];
    return [x2, x3, x4, x5, x6];
}

function testXMLNavigationOnSequenceWithNamespacesAndMultipleFilters() returns [xml, xml, xml, xml, xml] {
    xmlns "foo";
    xmlns "bar" as k;
    xmlns "foo" as ns;
    xml x1 = xmllib:concat(xml `<root><child>A</child></root>`,
                        xml `<root><ns:child>B</ns:child></root>`,
                        xml `<item><k:child>C</k:child><child2>D</child2>TEXT</item>`);
    xml x2 = x1/<child|child2>;
    xml x3 = x1/*;
    xml x4 = x1/<*>;
    xml x5 = x1/**/<ns:child|k:child|child2>;
    xml x6 = x1/<child|child2>[0];
    return [x2, x3, x4, x5, x6];
}

function testXMLElementAccessNavigationAccessComposition() returns [xml, xml, xml, xml, xml, xml, xml] {
    xml x = xml `<root>
        <person><name><fname>Kamal</fname><lname>Gunae</lname></name></person>
        <person><name><fname>Nimal</fname><lname>Jayee</lname></name></person>
        <person><name><fname>Sunil</fname><lname>Kumarae</lname></name></person>
        </root>`;

    xml c = x/**/<name>/<lname>;
    xml d = x/**/<name>/<lname|fname>;
    xml e = x/**/<name>/<lname|fname>.<lname>;
    xml f = x/**/<name>/<lname|fname>.<fname>;
    xml g = x/**/<name>/<lname|fname>/*;
    xml h = x/**/<fname>;
    xml i = x/<person>/**/<fname>;
    return [c, d, e, f, g, h, i];
}

function testXMLNavigationExpressionWithQuotedIdentifiers() returns [xml, xml] {
    xml x = xml `<jobInfo xmlns="http://www.force.com/2009/06/asyncapi/dataload">
        <id>750k2v101000N25bjbAc</id>
        <operation>insert</operation>
        <object>Account</object>
    </jobInfo>`;

    xmlns "http://www.force.com/2009/06/asyncapi/dataload";
    xmlns "http://www.force.com/2009/06/asyncapi/dataload" as ns0;

    return [x/<'object>, x/<ns0:'object>];
}

function testXMLNavigationExpressionWithXMLSubtypeOnLHS() {
        xml val = xml `<foo><bar>0</bar><baz>1</baz><bar>2</bar></foo>`;
        xml<xml:Element> xmlResult = val/<bar>;
        string s = xmlResult.toString();
        if (s == "<bar>0</bar><bar>2</bar>") {
            return;
        }
        panic error("Assertion error, expected: `<bar>0</bar><bar>2</bar>`, found: " + s);
}

function testXMLNavigationDescendantsStepWithXMLSubtypeOnLHS() {
        xml val = xml `<foo><bar>0</bar><baz>1</baz><bar>2</bar></foo>`;
        xml<xml:Element> xmlResult = val/**/<baz>;
        string s = xmlResult.toString();
        if (s == "<baz>1</baz>") {
            return;
        }
        panic error("Assertion error, expected: `<baz>1</baz>`, found: " + s);
}

function testXMLNavigationWithEscapeCharacter() {
    xmlns "foo" as ns;
    xml x1 = xml `<person><name>John</name><home-address>some address</home-address></person>`;
    xml x2 = xml `<ns:root><ns:child-node></ns:child-node></ns:root>`;

    xml x3 = x1/<home\-address>;
    assert(x3, xml `<home-address>some address</home-address>`);

    xml x4 = x2/<ns:child\-node>;
    assert(x4, xml `<ns:child-node xmlns:ns="foo"/>`);

    xml x5 = x1/**/<person>/<home\-address>;
    assert(x5, xml `<home-address>some address</home-address>`);

    xml x6 = x1/**/<person>/<name|home\-address>;
    assert(x6, xml `<name>John</name><home-address>some address</home-address>`);

    xml x7 = x1/**/<person>/<name|home\-address>.<home\-address>;
    assert(x7, xml `<home-address>some address</home-address>`);
}

type XC xml:Comment;
type XE xml:Element;
type XCE XC|XE;

function testXmlNavigationWithUnionType() {
    xml<xml:Element>|xml<xml:Comment> x1 = xml `<a><b><c><e>foo</e></c></b><d><g>bar</g></d></a>`;
    assert(x1.<a>, xml `<a><b><c><e>foo</e></c></b><d><g>bar</g></d></a>`);
    assert(x1/*, xml `<b><c><e>foo</e></c></b><d><g>bar</g></d>`);
    assert(x1/*.<b>, xml `<b><c><e>foo</e></c></b>`);
    assert(x1/**/<c>, xml `<c><e>foo</e></c>`);
    assert((x1/**/<c>)/*.<e>, xml `<e>foo</e>`);
    assert(x1/<d>, xml `<d><g>bar</g></d>`);
    assert((x1/<d>)/*.<g>, xml `<g>bar</g>`);

    xml:Element|xml:Comment x2 = xml `<!-- comment node-->`;
    assert(x2/*, xml ``);
    assert(x2/*.<a>, xml ``);
    assert(x2/**/<a>, xml ``);
    assert(x2/<b>, xml ``);

    xml:Element|xml:ProcessingInstruction x3 = xml `<?target data?>`;
    assert(x3/*, xml ``);
    assert(x3/*.<b>, xml ``);
    assert(x3/**/<c>, xml ``);
    assert(x3/<d>, xml ``);

    xml:Text|xml:ProcessingInstruction x4 = xml `test xml text`;
    assert(x4/*, xml ``);
    assert(x4/*.<d>, xml ``);
    assert(x4/**/<e>, xml ``);
    assert(x4/<f>, xml ``);

    XCE x5 = xml `<foo><bar>b</bar></foo>`;
    assert(x5.<foo>, xml `<foo><bar>b</bar></foo>`);
    assert(x5/*, xml `<bar>b</bar>`);
    assert(x5/*.<bar>, xml `<bar>b</bar>`);
    assert(x5/**/<bar>, xml `<bar>b</bar>`);

    xml<xml:Element>|xml<xml:Comment>|xml<xml:ProcessingInstruction>|xml:Text x6 = xml `<l><m><n></n></m></l>`;
    assert(x6/*, xml `<m><n></n></m>`);
    assert(x6/*.<m>, xml `<m><n></n></m>`);
    assert(x6/**/<n>, xml `<n></n>`);
    assert(x6/<m>, xml `<m><n></n></m>`);
}

function testXmlNavigationWithDefaultNamespaceDefinedAfter() {
    xml[] results = [];
    xml x1 = xml `<a>a<b>b<c>c</c></b><d>d</d></a><f/>`;
    results[0] = x1.<a>;
    results[1] = x1/*;
    results[2] = x1/<b>;
    results[3] = x1/<*>;
    results[4] = x1/**/<c>;

    xmlns "http://example2.com/" as p1;
    xml x2 = xml `<l>l<m>m<n>n</n></m><p1:q>q1</p1:q><q>q2</q></l>`;
    {
        xmlns "http://example2.com/";
    }
    results[5] = x2.<l>;
    results[6] = x2/<m>;
    results[7] = x2/<q>;
    results[8] = x2/**/<n>;

    {
        xmlns "http://example2.com/";
        {
            results[9] = x2.<l>;
            results[10] = x2/<q>;
        }
    }
    xmlns "http://example.com/" as p2;
    xml x3 = xml `<e>no-ns</e><f/><p2:e>with-ns</p2:e>`;
    results[11] = x3.<e>;

    xml x4 = xml `<e>e<f><g>f</g></f><h>h</h></e><j/>`;
    xmlns "http://example.com/";
    results[12] = x3.<e>;
    results[13] = x4.<e>;
    results[14] = x4/*;
    results[15] = x4/<f>;
    results[16] = x4/<*>;
    results[17] = x4/**/<g>;

    assertXmlNavigationWithDefaultNamespaceDefinedAfter(results);
}

function assertXmlNavigationWithDefaultNamespaceDefinedAfter(xml[] results) {
    assert(results[0], xml `<a>a<b>b<c>c</c></b><d>d</d></a>`);
    assert(results[1], xml `a<b>b<c>c</c></b><d>d</d>`);
    assert(results[2], xml `<b>b<c>c</c></b>`);
    assert(results[3], xml `<b>b<c>c</c></b><d>d</d>`);
    assert(results[4], xml `<c>c</c>`);
    assert(results[5], xml `<l>l<m>m<n>n</n></m><p1:q xmlns:p1="http://example2.com/">q1</p1:q><q>q2</q></l>`);
    assert(results[6], xml `<m>m<n>n</n></m>`);
    assert(results[7], xml `<q>q2</q>`);
    assert(results[8], xml `<n>n</n>`);
    assert(results[9], xml ``);
    assert(results[10], xml `<p1:q xmlns:p1="http://example2.com/">q1</p1:q>`);
    assert(results[11], xml `<e>no-ns</e>`);
    assert(results[12], xml `<p2:e xmlns:p2="http://example.com/">with-ns</p2:e>`);
    assert(results[13], xml ``);
    assert(results[14], xml `e<f><g>f</g></f><h>h</h>`);
    assert(results[15], xml ``);
    assert(results[16], xml `<f><g>f</g></f><h>h</h>`);
    assert(results[17], xml ``);
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        string reason = "expected `" + expected.toString() + "`, but found `" + actual.toString() + "`";
        error e = error(reason);
        panic e;
    }
}
