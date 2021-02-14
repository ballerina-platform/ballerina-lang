function testUpdatingGetAllChildren() {
    xml x1 = xml `<name>supun</name>`;
    xml x2 = xml `<fruit>apple</fruit>`;
    x1/* = x2;
}

function testNonStringIndexAccess() {
    xml x = xml `<elem></elem>`;
    var c = x["child"];
    var k = x[true];
    var b = x[1.2];
}

function testInvalidXMLAccessWithIndex() {
    xml x1 = xml `<root><!-- comment node--><name>supun</name><city>colombo</city></root>`;
    xml x2 = x1[0]/*;

    x2[1] = xml `<address/>`;

    string|'xml:Text x3 = xml `sample test`;
    string sampleString1 = x3[0];
    xml x4 = x3[0];

    'xml:Element x5 = xml `<root><!-- comment node--><name>anne</name><city>colombo</city></root>`;
    xml x6 = x5[0]/*;
    'xml:Comment x7 = xml `<!-- comment node-->`;
    xml x8 = x7[0]/*;
    'xml:ProcessingInstruction x9 = xml `<?xml-stylesheet href="mystyle.css" type="text/css"?>`;
    xml x10 = x9[0]/*;

    x6 = x5[0][0];
    x8 = x7[0][0];
    x10 = x9[0][0];

    int i = 'xml:length(x5[0]);
}
