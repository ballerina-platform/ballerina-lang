function testExpressionAsElementName() returns (xml) {
    int v1 = 11;
    xml x1 = xml `<ns0:{{v1}}>hello</ns0:{{v1}}>`;
    return x1;
}
