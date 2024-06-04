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
    assert(x5, xml ``);

    xml x6 = x1/**/<person>/<name|home\-address>;
    assert(x6, xml ``);

    xml x7 = x1/**/<person>/<name|home\-address>.<home\-address>;
    assert(x7, xml ``);
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

const i = 0;

function testXmlIndexedStepExtend() {
    int k = 1;
    xml x1 = xml
            `<item><!--comment--><name>T-shirt</name><price>19.99</price></item>
            <item><?data?><name>Backpack</name><price>34.99</price></item>
            <item>text<name>Watch</name><price>49.99</price></item>`;

    assert(x1/*[0], xml `<!--comment--><?data?>text`);
    assert(x1/*[k], xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x1/*[3], xml ``);
    assertError(trap x1/*[-1], "xml sequence index out of range. Length: '3' requested: '-1'");

    assert(x1/*[0][0], xml `<!--comment--><?data?>text`);
    assert(x1/*[k][i], xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x1/*[1][1], xml ``);
    assert(x1/*[3][3], xml ``);
    assertError(trap x1/*[0][-1], "xml sequence index out of range. Length: '1' requested: '-1'");
    assertError(trap x1/*[-1], "xml sequence index out of range. Length: '3' requested: '-1'");

    assert(x1/<price>[i], xml `<price>19.99</price><price>34.99</price><price>49.99</price>`);
    assert(x1/<price>[5], xml ``);
    assertError(trap x1/<price>[-1], "xml sequence index out of range. Length: '1' requested: '-1'");

    assert(x1/<price>[i][0], xml `<price>19.99</price><price>34.99</price><price>49.99</price>`);
    assert(x1/<price>[0][1], xml ``);
    assert(x1/<price>[5][4], xml ``);
    assertError(trap x1/<price>[0][-1], "xml sequence index out of range. Length: '1' requested: '-1'");

    assert(x1/**/<name>[0], xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x1/**/<name>[5], xml ``);
    assertError(trap x1/**/<name>[-1], "xml sequence index out of range. Length: '1' requested: '-1'");

    assert(x1/**/<name>[0][0], xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x1/**/<name>[0][1], xml ``);
    assert(x1/**/<name>[5][4], xml ``);
    assertError(trap x1/**/<name>[0][-1], "xml sequence index out of range. Length: '1' requested: '-1'");

    xml x2 = xml
            `<item><!--comment--><name>T-shirt</name><price>19.99</price><name>T-shirt2</name></item>
            <item><?data?><name>Backpack</name><price>34.99</price><name>Backpack2</name></item>
            <item>text<name>Watch</name><price>49.99</price><name>Watch2</name></item>`;

    assert(x2/**/<name>[0], xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x2/**/<name>[1], xml `<name>T-shirt2</name><name>Backpack2</name><name>Watch2</name>`);
    assert(x2/**/<name>[2], xml ``);
    assertError(trap x1/**/<name>[-1], "xml sequence index out of range. Length: '1' requested: '-1'");

    assert(x2/<name>[0], xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x2/<name>[1], xml `<name>T-shirt2</name><name>Backpack2</name><name>Watch2</name>`);
    assert(x2/<name>[2], xml ``);
    assertError(trap x1/<name>[-1], "xml sequence index out of range. Length: '1' requested: '-1'");

    xml:Element x3 = xml `<a>xml text<b><c>c</c><d>d</d></b><b><c>d</c></b><!--comment--></a>`;
    assert(x3/*[0], xml `xml text`);
    assert(x3/*[1], xml `<b><c>c</c><d>d</d></b>`);
    assert(x3/*[2], xml `<b><c>d</c></b>`);
    assert(x3/*[3], xml `<!--comment-->`);
    assert(x3/*[4], xml ``);
    assertError(trap x3/*[-1], "xml sequence index out of range. Length: '4' requested: '-1'");

    assert(x3/<b>[0], xml `<b><c>c</c><d>d</d></b>`);
    assert(x3/<b>[1], xml `<b><c>d</c></b>`);
    assert(x3/<b>[2], xml ``);
    assertError(trap x3/<b>[-1], "xml sequence index out of range. Length: '2' requested: '-1'");

    assert(x3/**/<c>[0], xml `<c>c</c>`);
    assert(x3/**/<c>[1], xml `<c>d</c>`);
    assert(x3/**/<c>[2], xml ``);
    assertError(trap x3/**/<c>[-1], "xml sequence index out of range. Length: '2' requested: '-1'");

    xml:Comment x4 = xml `<!--comment-->`;
    assert(x4/*[0], xml ``);
    assert(x4/*[-1], xml ``);
    assert(x4/<c>[0], xml ``);
    assert(x4/<c>[-1], xml ``);
    assert(x4/**/<c>[0], xml ``);
    assert(x4/**/<c>[-1], xml ``);

    xml:ProcessingInstruction x5 = xml `<?pi?>`;
    assert(x5/*[0], xml ``);
    assert(x5/*[-1], xml ``);
    assert(x5/<b>[i], xml ``);
    assert(x5/<b>[-1], xml ``);
    assert(x5/**/<c>[0], xml ``);
    assert(x5/**/<c>[-1], xml ``);

    xml:Text x6 = xml `xml text`;
    assert(x6/*[0], xml ``);
    assert(x6/*[-1], xml ``);
    assert(x6/<b>[0], xml ``);
    assert(x6/<b>[-1], xml ``);
    assert(x6/**/<c>[0], xml ``);
    assert(x6/**/<c>[-1], xml ``);
}

function testXmlFilterStepExtend() {
    xml x1 = xml
            `<item><name>T-shirt</name><price>19.99</price><count>1</count><brand><name>nike</name><local>no</local></brand></item>
            <item><?data?><name>Backpack</name><price>34.99</price><count>3</count><brand><name>adidas</name><local>yes</local></brand></item>
            <item>text<name>Watch</name><price>49.99</price><count>2</count><brand><name>samsung</name><local>no</local></brand></item>`;

    assert(x1/*.<name>, xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x1/*.<price>, xml `<price>19.99</price><price>34.99</price><price>49.99</price>`);
    assert(x1/*.<amount>, xml ``);
    assert(x1/*.<name>.<name>, xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x1/*.<name>.<price>, xml ``);
    assert(x1/*.<name|price>,
            xml `<name>T-shirt</name><price>19.99</price><name>Backpack</name><price>34.99</price><name>Watch</name><price>49.99</price>`);
    assert(x1/*.<name|price>.<price>, xml `<price>19.99</price><price>34.99</price><price>49.99</price>`);
    assert(x1/*.<*>.<price>, xml `<price>19.99</price><price>34.99</price><price>49.99</price>`);

    assert(x1/<name>.<name>, xml `<name>T-shirt</name><name>Backpack</name><name>Watch</name>`);
    assert(x1/<price>.<price>, xml `<price>19.99</price><price>34.99</price><price>49.99</price>`);
    assert(x1/<name|price>.<price>, xml `<price>19.99</price><price>34.99</price><price>49.99</price>`);
    assert(x1/<name|price|count>.<name|count>,
            xml `<name>T-shirt</name><count>1</count><name>Backpack</name><count>3</count><name>Watch</name><count>2</count>`);
    assert(x1/<name|price|count>.<name|count>.<count>, xml `<count>1</count><count>3</count><count>2</count>`);
    assert(x1/<count>.<amount>, xml ``);
    assert(x1/<amount>.<name>, xml ``);
    assert(x1/<amount>.<name|price>, xml ``);

    assert(x1/**/<name>.<name>, xml `<name>T-shirt</name><name>nike</name><name>Backpack</name><name>adidas</name><name>Watch</name><name>samsung</name>`);
    assert(x1/**/<price>.<price>, xml `<price>19.99</price><price>34.99</price><price>49.99</price>`);
    assert(x1/**/<name|local>.<local>, xml `<local>no</local><local>yes</local><local>no</local>`);
    assert(x1/**/<brand|local>.<brand>, xml `<brand><name>nike</name><local>no</local></brand><brand><name>adidas</name><local>yes</local></brand><brand><name>samsung</name><local>no</local></brand>`);
    assert(x1/**/<price|local|name>.<name|price>.<name>, xml `<name>T-shirt</name><name>nike</name><name>Backpack</name><name>adidas</name><name>Watch</name><name>samsung</name>`);
    assert(x1/**/<price|local|name>.<name|price>.<*>, xml `<name>T-shirt</name><price>19.99</price><name>nike</name><name>Backpack</name><price>34.99</price><name>adidas</name><name>Watch</name><price>49.99</price><name>samsung</name>`);
    assert(x1/**/<brand|amount>.<amount>, xml ``);
    assert(x1/**/<total|amount>.<amount>, xml ``);

    xml:Element x2 = xml `<items>
                            xml text
                            <pen><price>10</price></pen>
                            <pen><price>15</price></pen>
                            <book><price>30</price></book>
                            <paper><price>4</price></paper>
                            <price>40</price>
                            <!--comment-->
                          </items>`;
    assert(x2/*.<pen>, xml `<pen><price>10</price></pen><pen><price>15</price></pen>`);
    assert(x2/*.<book>, xml `<book><price>30</price></book>`);
    assert(x2/*.<book|pen>, xml `<pen><price>10</price></pen><pen><price>15</price></pen><book><price>30</price></book>`);
    assert(x2/*.<book|pen>.<book>, xml `<book><price>30</price></book>`);
    assert(x2/*.<pencil>, xml ``);
    assert(x2/*.<pencil>.<pen|book>, xml ``);

    assert(x2/<pen>.<pen>, xml `<pen><price>10</price></pen><pen><price>15</price></pen>`);
    assert(x2/<pen|book>.<book>, xml `<book><price>30</price></book>`);
    assert(x2/<pen|book|paper>.<paper|pen>, xml `<pen><price>10</price></pen><pen><price>15</price></pen><paper><price>4</price></paper>`);
    assert(x2/<pen|book|paper>.<paper|pen>.<paper>, xml `<paper><price>4</price></paper>`);
    assert(x2/<stationary>.<paper>, xml ``);
    assert(x2/<pencil>.<name>, xml ``);
    assert(x1/*.<pencil>.<name|price>, xml ``);

    assert(x2/**/<price>.<price>, xml `<price>10</price><price>15</price><price>30</price><price>4</price><price>40</price>`);
    assert(x2/**/<pen>.<pen>, xml `<pen><price>10</price></pen><pen><price>15</price></pen>`);assert(x2/**/<book|price>.<book>, xml `<book><price>30</price></book>`);
    assert(x2/**/<book|price>.<book>, xml `<book><price>30</price></book>`);
    assert(x2/**/<price|pen|book>.<pen|price>, xml `<pen><price>10</price></pen><price>10</price><pen><price>15</price></pen><price>15</price><price>30</price><price>4</price><price>40</price>`);
    assert(x2/**/<price|pen|book>.<pen|price>.<price>, xml `<price>10</price><price>15</price><price>30</price><price>4</price><price>40</price>`);
    assert(x2/**/<pen|pencil>.<pencil>, xml ``);
    assert(x2/**/<total|pencil>.<total>, xml ``);

    xml:Comment x3 = xml `<!--comment-->`;
    assert(x3/*.<name>, xml ``);
    assert(x3/*.<name|price>, xml ``);
    assert(x3/*.<name>.<price>, xml ``);
    assert(x3/<name>.<price>, xml ``);
    assert(x3/<name>.<price|pen>, xml ``);
    assert(x3/**/<name>.<name>, xml ``);
    assert(x3/**/<price|name>.<name>, xml ``);

    xml:ProcessingInstruction x4 = xml `<?pi?>`;
    assert(x4/*.<name>, xml ``);
    assert(x4/*.<name|price>, xml ``);
    assert(x4/*.<name>.<price>, xml ``);
    assert(x4/<name>.<price>, xml ``);
    assert(x4/<name>.<price|pen>, xml ``);
    assert(x4/**/<name>.<name>, xml ``);
    assert(x4/**/<price|name>.<name>, xml ``);

    xml:Text x5 = xml `xml text`;
    assert(x5/*.<name>, xml ``);
    assert(x5/*.<name|price>, xml ``);
    assert(x5/*.<name>.<price>, xml ``);
    assert(x5/<name>.<price>, xml ``);
    assert(x5/<name>.<price|pen>, xml ``);
    assert(x5/**/<name>.<name>, xml ``);
    assert(x5/**/<price|name>.<name>, xml ``);
}

function testXmlIndexedAndFilterStepExtend() {
    int k = 1;
    xml x1 = xml
            `<item><!--comment--><name>T-shirt</name><price>19.99</price><brand><name>nike</name><local>no</local></brand></item>
            <item><?data?><iname>Backpack</iname><iname>IBackpack</iname><price>34.99</price><brand><name>adidas</name><local>yes</local></brand></item>
            <item>text<name>Watch</name><price>49.99</price><brand><name>samsung</name><local>no</local></brand></item>`;

    assert(x1/*[1].<name>, xml `<name>T-shirt</name><name>Watch</name>`);
    assert(x1/*[k].<name|iname>, xml `<name>T-shirt</name><iname>Backpack</iname><name>Watch</name>`);
    assert(x1/*[0].<count>, xml ``);
    assert(x1/*.<name>[0][0], xml `<name>T-shirt</name><name>Watch</name>`);
    assert(x1/*.<name>[1][0], xml ``);
    assert(x1/*.<name|iname>[0].<iname>, xml `<iname>Backpack</iname>`);
    assert(x1/*.<name|iname>[k].<iname>, xml `<iname>IBackpack</iname>`);
    assertError(trap x1/*.<name>[-1], "xml sequence index out of range. Length: '1' requested: '-1'");
    assertError(trap x1/*.<name|iname>[k].<iname>[-1], "xml sequence index out of range. Length: '0' requested: '-1'");

    assert(x1/<brand|name>[0].<brand>, xml `<brand><name>adidas</name><local>yes</local></brand>`);
    assert(x1/<brand|name>.<name>[0], xml `<name>T-shirt</name><name>Watch</name>`);
    assert(x1/<price>.<brand>[5], xml ``);
    assert(x1/<brand|iname>[0].<brand>.<brand>[0], xml `<brand><name>nike</name><local>no</local></brand><brand><name>samsung</name><local>no</local></brand>`);

    assert(x1/**/<name|iname>[i].<name>, xml `<name>T-shirt</name><name>Watch</name>`);
    assert(x1/**/<name|iname>[k].<name>, xml `<name>nike</name><name>samsung</name>`);
    assert(x1/**/<name|iname>[0].<name>[0], xml `<name>T-shirt</name><name>Watch</name>`);
    assertError(trap x1/<price>[-1].<count>, "xml sequence index out of range. Length: '1' requested: '-1'");

    xml:Element x2 = xml `<a>xml text<b><c>c</c><d>d</d></b><b><c>d</c></b><!--comment--></a>`;
    assert(x2/*.<b>[i], xml `<b><c>c</c><d>d</d></b>`);
    assert(x2/*[2].<b>, xml `<b><c>d</c></b>`);
    assert(x2/*[2].<b>[0].<b>, xml `<b><c>d</c></b>`);
    assertError(trap x2/*.<b>[-1].<c>, "xml sequence index out of range. Length: '2' requested: '-1'");

    assert(x2/<b>[0].<b>, xml `<b><c>c</c><d>d</d></b>`);
    assert(x2/<b>[1].<b>, xml `<b><c>d</c></b>`);
    assert(x2/<b>[1].<b>[k], xml ``);
    assertError(trap x2/<b>.<c>[-1], "xml sequence index out of range. Length: '0' requested: '-1'");

    assert(x2/**/<b|c>.<c>[0], xml `<c>c</c>`);
    assert(x2/**/<b|c>.<c>[k], xml `<c>d</c>`);
    assert(x2/**/<*>.<c>[0], xml `<c>c</c>`);
    assertError(trap x2/**/<c>[-1].<b>, "xml sequence index out of range. Length: '2' requested: '-1'");

    xml:Comment x3 = xml `<!--comment-->`;
    assert(x3/*.<a>[0], xml ``);
    assert(x3/*[-1].<a>, xml ``);
    assert(x3/<c>[i].<b>, xml ``);
    assert(x3/**/<c>.<d>[0].<e>, xml ``);
    assert(x3/**/<c>[-1].<a>, xml ``);

    xml:ProcessingInstruction x4 = xml `<?pi?>`;
    assert(x4/*.<a>[0], xml ``);
    assert(x4/*[-1].<c>, xml ``);
    assert(x4/<b>[0].<a>.<b>[i], xml ``);
    assert(x4/<b>.<a>[-1], xml ``);
    assert(x4/**/<c>.<c>[0], xml ``);
    assert(x4/**/<c>[-1].<a>.<b>, xml ``);

    xml:Text x5 = xml `xml text`;
    assert(x5/*.<a>[0], xml ``);
    assert(x5/*.<b>[-1], xml ``);
    assert(x5/<b>[0].<c>, xml ``);
    assert(x5/<b>[-1].<b>, xml ``);
    assert(x5/**/<c>.<d>[0], xml ``);
    assert(x5/**/<c>[-1].<e>, xml ``);
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        string reason = "expected `" + expected.toString() + "`, but found `" + actual.toString() + "`";
        error e = error(reason);
        panic e;
    }
}

function assertError(any|error value, string errorMessage) {
    if value is error {
        if (value.message() != errorMessage) {
            panic error("Expected error message: " + errorMessage + " found: " + value.message());
        }
        return;
    }
    panic error("Expected: Error, found: " + (typeof value).toString());
}
