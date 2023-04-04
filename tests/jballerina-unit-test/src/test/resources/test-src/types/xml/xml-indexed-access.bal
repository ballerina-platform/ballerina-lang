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
    assert(seq[0].toString(), "<root>interpolation1 text1 text2<foo>123</foo><bar></bar></root>");
    assert(seq[1].toString(), "text2interpolation1");
    assert(seq[2].toString(), "<!--commentinterpolation1-->");
    assert(seq[3].toString(), "text3");
    assert(seq[4].toString(), "<?foo ?>");

    xml elements = seq[0]/*;
    assert(elements[0].toString(), "interpolation1 text1 text2");
    assert(elements[1].toString(), "<foo>123</foo>");
    assert(elements[2].toString(), "<bar></bar>");
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
    xml|error e = trap x[-1];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x[a];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x[i];
    check assertErrorMessage(e, "xml index out of range: index -1");

    xml:Text x2 = xml `This is an xml`;
    e = trap x2[-1];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x2[a];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x2[i];
    check assertErrorMessage(e, "xml index out of range: index -1");

    xml x3 = xml `<name>Sherlock Holmes</name>`;
    e = trap x3[-1];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x3[a];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x3[i];
    check assertErrorMessage(e, "xml index out of range: index -1");

    xml:Element x4 = xml `<name>Sherlock Holmes</name>`;
    e = trap x4[-1];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x4[a];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x4[i];
    check assertErrorMessage(e, "xml index out of range: index -1");

    xml x5 = xml `<!--This is a comment 1-->`;
    e = trap x5[-1];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x5[a];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x5[i];
    check assertErrorMessage(e, "xml index out of range: index -1");

    xml:Comment x6 = xml `<!--This is a comment 1-->`;
    e = trap x6[-1];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x6[a];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x6[i];
    check assertErrorMessage(e, "xml index out of range: index -1");

    xml x7 = xml `<?target data?>`;
    e = trap x7[-1];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x7[a];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x7[i];
    check assertErrorMessage(e, "xml index out of range: index -1");

    xml:ProcessingInstruction x8 = xml `<?target data?>`;
    e = trap x8[-1];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x8[a];
    check assertErrorMessage(e, "xml index out of range: index -1");
    e = trap x8[i];
    check assertErrorMessage(e, "xml index out of range: index -1");

    xml x9 = xml `<names><name>Sherlock Holmes</name><name>Sherlock Holmes</name></names>`;
    e = trap x9[1][-1];
    check assertErrorMessage(e, "Index -1 out of bounds for length 0");
    e = trap x9[1][a];
    check assertErrorMessage(e, "Index -1 out of bounds for length 0");
    e = trap x9[0][i];
    check assertErrorMessage(e, "xml index out of range: index -1");
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

function assertErrorMessage(anydata|error actual, string expectedErrorMessage) returns error? {
    anydata actualValue = (actual is error)? check (<error> actual).detail().get("message").ensureType() : actual;
    if expectedErrorMessage == actualValue {
        return;
    }
    panic error(string `expected '${expectedErrorMessage.toBalString()}', found '${actualValue.toBalString()}'`);
}
