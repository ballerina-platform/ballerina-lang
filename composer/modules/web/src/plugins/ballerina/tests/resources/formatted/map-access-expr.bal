import ballerina/lang.maps;

function mapAccessTest (int x, int y) (int) {
    map testMap = {};
    int xx;
    int yy;
    testMap["first"] = x;
    testMap["second"] = y;
    testMap["third"] = x + y;
    testMap["forth"] = x - y;
    xx, _ = (int)testMap["first"];
    yy, _ = (int)testMap["second"];

    return xx + yy;
}

function mapReturnTest (string firstName, string lastName) (map) {
    map testMap = {};
    testMap["fname"] = firstName;
    testMap["lname"] = lastName;
    testMap[firstName + lastName] = firstName + lastName;
    return testMap;
}

function testArrayAccessAsIndexOfMapt () (string) {
    map namesMap = {fname:"Supun", lname:"Setunga"};
    string[] keys = ["fname", "lname"];
    string key;
    key, _ = (string)namesMap[keys[0]];
    return key;
}

function testAccessThroughVar () (string) {
    map m = {};
    m["x"] = "a";
    m["y"] = "b";
    m["z"] = "c";
    return constructString(m);
}

function constructString (map m) (string) {
    string[] keys = maps:keys(m);
    int len = lengthof keys;
    int i = 0;
    string returnStr = "";
    while (i < len) {
        string key = keys[i];
        var val, e = (string)m[key];
        returnStr = returnStr + key + ":" + val + ", ";
        i = i + 1;
    }
    return returnStr;
}
