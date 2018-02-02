function testCompareNullOfDifferentTypes()(boolean, xml, json) {
    xml x = null;
    json j = null;
    int a = 0;
    if (x == j) {
        a = 11;
    }
    
    return x, j, a;
}