function testXMLAccessWithIndex() returns [xml, xml, xml] {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0]/*;

    return [x1[0], x2[0], x2[1]];
}

function testXMLAccessWithOutOfIndex() returns (xml) {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    return x1[1];
}

function testXMLSequenceAccessWithOutOfIndex() returns (xml) {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0]/*;

    return x2[5];
}

function testLengthOfXMLSequence() returns [int, int, int, int] {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0]/*;

    xml[] x3 = [x1, x2];
    return [x1.length(), x2.length(), x2[2].length(), x3.length()];
}
