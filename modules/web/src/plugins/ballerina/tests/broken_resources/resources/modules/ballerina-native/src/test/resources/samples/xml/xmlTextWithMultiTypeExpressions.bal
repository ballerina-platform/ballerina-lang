function testTextWithMultiTypeExpressions() (xml) {
    int v1 = 11;
    string v2 = "world";
    xml v3 = xml `<foo>apple</foo>`;
    
    xml x = xml `hello {{v1}} {{v2}}. How are you {{v3}} ?`;
    return x;
}
