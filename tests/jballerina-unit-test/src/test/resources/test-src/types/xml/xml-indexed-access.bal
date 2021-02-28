function testXMLAccessWithIndex() {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0]/*;

    assert(x1[0].toString(), "<root><!-- comment node--><name>supun</name><city>colombo</city></root>");
    assert(x2[0].toString(), "<!-- comment node-->");
    assert(x2[1].toString(), "<name>supun</name>");

    xml<'xml:Text> x3 = xml `sample test`;
    string testString1 = x3[0];
    assert(x3[0].toString(), "sample test");
    assert(testString1, "sample test");

    'xml:Text x4 = xml `sample test`;
    string testString2 = x4[0];
    assert(x4[0].toString(), "sample test");
    assert(testString2, "sample test");

    xml <xml<'xml:Text>> x5 = xml `sample test`;
    string testString3 = x5[0];
    assert(x5[0].toString(), "sample test");
    assert(testString3, "sample test");

    string testString4 = x3[1];
    assert(testString4, "");
    string testString5 = x4[1];
    assert(testString5, "");
    string testString6 = x5[1];
    assert(testString6, "");

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

function testLengthOfXMLSequence() returns [int, int, int, int] {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0]/*;

    xml[] x3 = [x1, x2];
    return [x1.length(), x2.length(), x2[2].length(), x3.length()];
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
