function testConstrainedMapAssignNegative() (map<int>) {
    map testMap = {};
    return testMap;
}

function testConstrainedMapRecordLiteralNegative() (map<int>) {
    map<int> testMap = {index_1:1, index_2:"two"};
    return testMap;
}

function testConstrainedMapIndexBasedAssignNegative() (map<string>) {
    map<string> testMap = {};
    testMap["name"] = 24;
    return testMap;
}
