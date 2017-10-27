import ballerina.lang.xmls;

function testInvalidXMLAccessWithIndex() (xml) {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = xmls:children(x1[0]);

    x2[1] = xml `<address/>`;

    return x2; 
}
