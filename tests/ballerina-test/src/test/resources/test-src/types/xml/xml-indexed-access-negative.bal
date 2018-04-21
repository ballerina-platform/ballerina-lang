function testInvalidXMLAccessWithIndex() returns (xml) {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0].*;

    x2[1] = xml `<address/>`;

    return x2; 
}

function testUpdatingGetAllChildren() {
    xml x1 = xml `<name>supun</name>`;
    xml x2 = xml `<fruit>apple</fruit>`;
    x1.* = x2;
}
