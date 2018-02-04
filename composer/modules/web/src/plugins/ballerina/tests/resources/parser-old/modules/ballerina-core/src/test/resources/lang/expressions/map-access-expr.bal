function mapAccessTest(int x, int y)(int) {
    map testMap = {};
    int xx;
    int yy;
    testMap["first"] = x;
    testMap["second"] = y;
    testMap["third"] = x + y;
    testMap["forth"] = x - y;
    xx, _ = (int) testMap["first"];
    yy, _ = (int) testMap["second"];

    return xx + yy;
}

function mapReturnTest(string firstName, string lastName) (map) {
    map testMap = {};
    testMap["fname"] = firstName;
    testMap["lname"] = lastName;
    testMap[ firstName + lastName ] = firstName + lastName;
    return testMap;
}

function testArrayAccessAsIndexOfMapt() (string) {
    map namesMap = {fname:"Supun",lname:"Setunga"};
    string[] keys = ["fname","lname"];
    string key;
    key, _ = (string)namesMap[keys[0]];
    return key;
}
