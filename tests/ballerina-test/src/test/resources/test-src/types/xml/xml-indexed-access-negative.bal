function testInvalidXMLAccessWithIndex() (xml) {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0].children();

    x2[1] = xml `<address/>`;

    return x2; 
}
