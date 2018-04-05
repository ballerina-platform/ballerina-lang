function mapAccessTest(int x, int y) returns (int) {
    map testMap;
    int xx;
    int yy;
    testMap["first"] = x;
    testMap["second"] = y;
    testMap["third"] = x + y;
    testMap["forth"] = x - y;
    xx = check <int> testMap["first"];
    yy = check <int> testMap["second"];

    return xx + yy;
}

function mapReturnTest(string firstName, string lastName) returns (map) {
    map testMap;
    testMap["fname"] = firstName;
    testMap["lname"] = lastName;
    testMap[ firstName + lastName ] = firstName + lastName;
    return testMap;
}

function testArrayAccessAsIndexOfMapt() returns (string) {
    map namesMap = {fname:"Supun",lname:"Setunga"};
    string[] keys = ["fname","lname"];
    string key;
    key = <string> namesMap[keys[0]];
    return key;
}

function testAccessThroughVar() returns (string) {
    map m;
    m["x"] = "a";
    m["y"] = "b";
    m["z"] = "c";
    return constructString(m);
}

function constructString(map m) returns (string) {
    string [] keys = m.keys();
    int len = lengthof keys;
    int i = 0;
    string returnStr = "";
    while (i < len) {
        string key = keys[i];
        string val = <string> m[key];
        returnStr = returnStr + key + ":" + val + ", ";
        i = i + 1;
    }
    return returnStr;
}

function testMapClear() returns (int) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    namesMap.clear();
    return lengthof namesMap;
}

function testHasKeyPositive() returns (boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname");
}

function testHasKeyNegative() returns (boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname2");
}

function testGetMapValues () returns (string, string) {
    json j = {"city":"Colombo", "country":"SriLanka"};
    int[] arr = [7,8,9];
    map address = {city:"CA", "country":"USA"};
    map<map>[] addressArray=[{address:{city:"Colombo", "country":"SriLanka"}},
                               {address:{city:"Kandy", "country":"SriLanka"}},
                               {address:{city:"Galle", "country":"SriLanka"}}];
    map m = { name:"Supun",
                  age:25,
                  gpa:2.81,
                  status:true,
                  info:null,
                  address:address,
                  intArray:arr,
                  addressArray:addressArray,
                  finfo:j
                };
    any[] values = m.values();
    var nam = <string> values[0];
    var jsn = check <json> values[8];
    var city = check <string> jsn.city;
    return (nam, city);
}

function testMapRemovePositive() returns (boolean, boolean, boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return (namesMap.hasKey("fname"), namesMap.remove("fname"), namesMap.hasKey("fname"));
}

function testMapRemoveNegative() returns (boolean, boolean, boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return (namesMap.hasKey("fname2"), namesMap.remove("fname2"), namesMap.hasKey("fname2"));
}
