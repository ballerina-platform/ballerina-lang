function testXMLAccessWithIndex() {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0]/*;
    assert(x1[0].toString(), "<root><!-- comment node--><name>supun</name><city>colombo</city></root>");
    assert(x2[0].toString(), "<!-- comment node-->");
    assert(x2[1].toString(), "<name>supun</name>");

    'xml:Element x15 = xml `<root><!-- comment node--><name>anne</name><city>colombo</city></root>`;
    xml x16 = x15[0]/*;
    assert(x16.toString(), "<!-- comment node--><name>anne</name><city>colombo</city>");
    'xml:Comment x17 = xml `<!-- comment node-->`;
    xml x18 = x17[0]/*;
    assert(x18.toString(), "");
    'xml:ProcessingInstruction x19 = xml `<?xml-stylesheet href="mystyle.css" type="text/css"?>`;
    xml x20 = x19[0]/*;
    assert(x20.toString(), "");

    x16 = x15[0][0];
    assert(x16.toString(), "<root><!-- comment node--><name>anne</name><city>colombo</city></root>");
    x18 = x17[0][0];
    assert(x18.toString(), "<!-- comment node-->");
    x20 = x19[0][0];
    assert(x20.toString(), "<?xml-stylesheet href=\"mystyle.css\" type=\"text/css\"?>");

    int i = 'xml:length(x15[0]);
    assert(i.toString(), "1");

    xml<'xml:Text> x3 = xml `sample test`;
    assert(x3[0].toString(), "sample test");

    'xml:Text x4 = xml `sample test`;
    assert(x4[0].toString(), "sample test");

    xml <xml<'xml:Text>> x5 = xml `sample test`;
    assert(x5[0].toString(), "sample test");

    xml<'xml:Comment> x6 = xml `<!-- comment node-->`;
    assert(x6[0].toString(), "<!-- comment node-->");
    assert(x6[1].toString(), "");
    'xml:Comment x7 = xml `<!-- comment node-->`;
    assert(x7[0].toString(), "<!-- comment node-->");
    assert(x7[1].toString(), "");
    xml<xml<'xml:Comment>> x8 = xml `<!-- comment node-->`;
    assert(x8[0].toString(), "<!-- comment node-->");
    assert(x8[1].toString(), "");

    xml<'xml:ProcessingInstruction> x9 = xml `<?target data?>`;
    assert(x9[0].toString(), "<?target data?>");
    assert(x9[1].toString(), "");
    'xml:ProcessingInstruction x10 = xml `<?target data?>`;
    assert(x10[0].toString(), "<?target data?>");
    assert(x10[1].toString(), "");
    xml<xml<'xml:ProcessingInstruction>> x11 = xml `<?target data?>`;
    assert(x11[0].toString(), "<?target data?>");
    assert(x11[1].toString(), "");

    xml<'xml:Element> x12 = xml `<root><name>supun</name><city>colombo</city></root>`;
    assert(x12[0].toString(), "<root><name>supun</name><city>colombo</city></root>");
    assert(x12[1].toString(), "");
    'xml:Element x13 = xml `<root><name>supun</name><city>colombo</city></root>`;
    assert(x13[0].toString(), "<root><name>supun</name><city>colombo</city></root>");
    assert(x13[1].toString(), "");
    xml<xml<'xml:Element>> x14 = xml `<root><name>supun</name><city>colombo</city></root>`;
    assert(x14[0].toString(), "<root><name>supun</name><city>colombo</city></root>");
    assert(x14[1].toString(), "");
}

function testXMLSequenceAccessWithIndex() {
    string v1 = "interpolation1";
    xml seq = xml `<root>${v1} text1 text2<foo>123</foo><bar></bar></root>text2${v1}<!--comment${v1}-->text3<?foo?>`;
    assert(seq[0].toString(), "<root>interpolation1 text1 text2<foo>123</foo><bar/></root>");
    assert(seq[1].toString(), "text2interpolation1");
    assert(seq[2].toString(), "<!--commentinterpolation1-->");
    assert(seq[3].toString(), "text3");
    assert(seq[4].toString(), "<?foo ?>");

    xml elements = seq[0]/*;
    assert(elements[0].toString(), "interpolation1 text1 text2");
    assert(elements[1].toString(), "<foo>123</foo>");
    assert(elements[2].toString(), "<bar/>");
}

function testLengthOfXMLSequence() returns [int, int, int, int] {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0]/*;

    xml[] x3 = [x1, x2];
    return [x1.length(), x2.length(), x2[2].length(), x3.length()];
}

const i = -1;

function testInvalidXMLAccessWithNegativeIndex() returns error? {
    int a = -1;
    xml x = xml `This is an xml`;
    string errorMessage = "xml sequence index out of range. Length: '1' requested: '-1'";
    string errorMessage2 = "xml sequence index out of range. Length: '0' requested: '-1'";
    string errorMessage3 = "xml sequence index out of range. Length: '2' requested: '-1'";

    xml|error e = trap x[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml:Text x2 = xml `This is an xml`;
    e = trap x2[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x2[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x2[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml x3 = xml `<name>Sherlock Holmes</name>`;
    e = trap x3[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x3[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x3[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml:Element x4 = xml `<name>Sherlock Holmes</name>`;
    e = trap x4[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x4[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x4[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml x5 = xml `<!--This is a comment 1-->`;
    e = trap x5[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x5[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x5[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml:Comment x6 = xml `<!--This is a comment 1-->`;
    e = trap x6[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x6[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x6[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml x7 = xml `<?target data?>`;
    e = trap x7[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x7[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x7[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml:ProcessingInstruction x8 = xml `<?target data?>`;
    e = trap x8[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x8[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x8[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml x9 = xml `<names><name>Sherlock Holmes</name><name>Sherlock Holmes</name></names>`;
    e = trap x9[1][-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage2);
    e = trap x9[1][a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage2);
    e = trap x9[1][i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage2);

    e = trap x9[0][-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x9[0][a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x9[0][i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);


    xml x10 = xml `<!--This is a comment 1--><!--This is a comment 1-->`;
    e = trap x10[1][-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x10[1][a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x10[1][i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml x11 = xml `<?target data?><?target data?>`;
    e = trap x11[1][-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x11[1][a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x11[1][i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml x12 = xml `<names><name>Sherlock Holmes</name><name>Sherlock Holmes</name></names>`;
    e = trap x12[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x12[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x12[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml x13 = xml `<!--This is a comment 1--><!--This is a comment 1-->`;
    e = trap x13[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);
    e = trap x13[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);
    e = trap x13[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);

    xml x14 = xml `<?target data?><?target data?>`;
    e = trap x14[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);
    e = trap x14[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);
    e = trap x14[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);

    xml x15 = xml `<name>Sherlock Holmes</name><name>Sherlock Holmes</name>`;
    e = trap x15[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);
    e = trap x15[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);
    e = trap x15[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage3);
}

const j = 100;

function testXmlAccessWithLargerIndex() {
    int a = 100;
    xml x = xml `This is an xml`;

    xml xmlValue = x[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml:Text x2 = xml `This is an xml`;
    xmlValue = x2[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x2[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x2[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x3 = xml `<name>Sherlock Holmes</name>`;
    xmlValue = x3[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x3[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x3[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml:Element x4 = xml `<name>Sherlock Holmes</name>`;
    xmlValue = x4[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x4[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x4[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x5 = xml `<!--This is a comment 1-->`;
    xmlValue = x5[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x5[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x5[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml:Comment x6 = xml `<!--This is a comment 1-->`;
    xmlValue = x6[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x6[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x6[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x7 = xml `<?target data?>`;
    xmlValue = x7[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x7[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x7[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml:ProcessingInstruction x8 = xml `<?target data?>`;
    xmlValue = x8[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x8[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x8[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x9 = xml `<names><name>Sherlock Holmes</name><name>Sherlock Holmes</name></names>`;
    xmlValue = x9[1][100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x9[1][a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x9[0][j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x10 = xml `<!--This is a comment 1--><!--This is a comment 1-->`;
    xmlValue = x10[1][100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x10[1][a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x10[0][j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x11 = xml `<?target data?><?target data?>`;
    xmlValue = x11[1][100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x11[1][a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x11[0][j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x12 = xml `<names><name>Sherlock Holmes</name><name>Sherlock Holmes</name></names>`;
    xmlValue = x12[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x12[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x12[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x13 = xml `<!--This is a comment 1--><!--This is a comment 1-->`;
    xmlValue = x13[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x13[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x13[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x14 = xml `<?target data?><?target data?>`;
    xmlValue = x14[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x14[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x14[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);

    xml x15 = xml `<names><name>Sherlock Holmes</name><name>Sherlock Holmes</name></names>`;
    xmlValue = x15[100];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x15[a];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
    xmlValue = x15[j];
    assertTrue(xmlValue is xml);
    assert(xmlValue, xml ``);
}

type XmlType1 xml:Text|xml:Comment;

type XmlType2 xml<xml:Element|xml:ProcessingInstruction>;

type XmlType3 XmlType1|XmlType2;

function testXmlIndexedAccessWithUnionType() {
    xml value;
    xml:Element|xml:Comment x1 = xml `<a>abc</a>`;
    value = x1[0];
    assert(value, xml `<a>abc</a>`);
    assertTrue(typeof value is typedesc<xml:Element>);
    value = x1[1];
    assert(value, xml ``);
    assertTrue(typeof value is typedesc<xml:Text>);

    xml:Element|xml:Text x2 = xml `Hello World`;
    value = x2[0];
    assert(value, xml `Hello World`);
    assertTrue(typeof value is typedesc<xml:Text>);
    value = x2[0][1];
    assert(value, xml ``);
    assertTrue(typeof value is typedesc<xml:Text>);

    xml:Element|xml:ProcessingInstruction x3 = xml `<?target data?>`;
    value = x3[0];
    assert(value, xml `<?target data?>`);
    assertTrue(typeof value is typedesc<xml:ProcessingInstruction>);
    value = x3[1][100];
    assert(value, xml ``);
    assertTrue(typeof value is typedesc<xml:Text>);

    xml:Element|xml:Comment x4 = xml `<!-- comment node-->`;
    value = x4[0];
    assert(value, xml `<!-- comment node-->`);
    assertTrue(typeof value is typedesc<xml:Comment>);
    value = x4[10];
    assert(value, xml ``);
    assertTrue(typeof value is typedesc<xml:Text>);

    xml:Text|xml:Comment x5 = xml `<!-- comment node 2 -->`;
    value = x5[0];
    assert(value, xml `<!-- comment node 2 -->`);
    assertTrue(typeof value is typedesc<xml:Comment>);

    xml:ProcessingInstruction|xml:Comment x6 = xml `<?target test?>`;
    value = x6[0];
    assert(value, xml `<?target test?>`);
    assertTrue(typeof value is typedesc<xml:ProcessingInstruction>);

    xml:ProcessingInstruction|xml:Text x7 = xml `Test Text`;
    value = x7[0];
    assert(value, xml `Test Text`);
    assertTrue(typeof value is typedesc<xml:Text>);

    xml|xml:Comment x8 = xml `<a>abc</a><b>def</b>`;
    value = x8[0];
    assert(value, xml `<a>abc</a>`);
    assertTrue(typeof value is typedesc<xml:Element>);

    xml|xml x9 = xml `<c>ghi</c><d>jkl</d>`;
    value = x9[0];
    assert(value, xml `<c>ghi</c>`);
    assertTrue(typeof value is typedesc<xml:Element>);

    xml<xml:Text|xml:Comment>|xml<xml:ProcessingInstruction|xml:Element> x10 = xml `<name>Dan Brown</name>`;
    value = x10[0];
    assert(value, xml `<name>Dan Brown</name>`);
    assertTrue(typeof value is typedesc<xml:Element>);
    value = x10[1];
    assert(value, xml ``);
    assertTrue(typeof value is typedesc<xml:Text>);

    XmlType1 x11 = xml `Hello!`;
    value = x11[0];
    assert(value, xml `Hello!`);
    assertTrue(typeof value is typedesc<xml:Text>);

    XmlType2 x12 = xml `<name>Sherlock Holmes</name><work>Detective</work>`;
    value = x12[0];
    assert(value, xml `<name>Sherlock Holmes</name>`);
    assertTrue(typeof value is typedesc<xml:Element>);
    value = x12[1];
    assert(value, xml `<work>Detective</work>`);
    assertTrue(typeof value is typedesc<xml:Element>);

    XmlType3 x13 = xml `<name>Sherlock Holmes</name>`;
    value = x13[0];
    assert(value, xml `<name>Sherlock Holmes</name>`);
    assertTrue(typeof value is typedesc<xml:Element>);
    value = x13[50][1];
    assert(value, xml ``);
    assertTrue(typeof value is typedesc<xml:Text>);
}

function testInvalidXMLUnionAccessWithNegativeIndex() {
    int a = -1;
    xml:Element|xml:Comment x1 = xml `<a>abc</a>`;
    string errorMessage = "xml sequence index out of range. Length: '1' requested: '-1'";

    xml|error e = trap x1[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x1[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x1[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    xml<xml:Text|xml:Comment>|xml<xml:ProcessingInstruction|xml:Element> x2 = xml `<name>Dan Brown</name>`;
    e = trap x2[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x2[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x2[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    XmlType1 x3 = xml `Hello!`;
    e = trap x3[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x3[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x3[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);

    XmlType3 x4 = xml `<name>Sherlock Holmes</name>`;
    e = trap x4[-1];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x4[a];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
    e = trap x4[i];
    assertTrue(e is error);
    assert((<error> e).message(), errorMessage);
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

function assertTrue(anydata actual) {
    assert(actual, true);
}
