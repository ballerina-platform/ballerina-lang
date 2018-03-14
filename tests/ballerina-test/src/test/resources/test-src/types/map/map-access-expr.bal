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

function testAccessThroughVar() (string) {
    map m = {};
    m["x"] = "a";
    m["y"] = "b";
    m["z"] = "c";
    return constructString(m);
}

function constructString(map m) (string) {
    string [] keys = m.keys();
    int len = lengthof keys;
    int i = 0;
    string returnStr = "";
    while (i < len) {
        string key = keys[i];
        var val, e = (string) m[key];
        returnStr = returnStr + key + ":" + val + ", ";
        i = i + 1;
    }
    return returnStr;
}

function testMapClear() (int) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    namesMap.clear();
    return lengthof namesMap;
}

function testHasKeyPositive() (boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname");
}

function testHasKeyNegative() (boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname2");
}

function testGetMapValues () (string, string) {
    json j = {"city":"Colombo", "country":"SriLanka"};
    int[] arr = [7,8,9];
    map m = { name:"Supun",
                  age:25,
                  gpa:2.81,
                  status:true,
                  info:null,
                  address:{city:"CA", "country":"USA"},
                  intArray:arr,
                  addressArray:[
                        {address:{city:"Colombo", "country":"SriLanka"}},
                        {address:{city:"Kandy", "country":"SriLanka"}},
                        {address:{city:"Galle", "country":"SriLanka"}}
                  ],
                  finfo:j
                };
    any[] values = m.values();
    var nam, _ = (string) values[0];
    var jsn, _ = (json) values[8];
    var city, _ = (string) jsn.city;
    return nam, city;
}

function testMapRemovePositive() (boolean, boolean, boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname"), namesMap.remove("fname"), namesMap.hasKey("fname");
}

function testMapRemoveNegative() (boolean, boolean, boolean) {
    map namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname2"), namesMap.remove("fname2"), namesMap.hasKey("fname2");
}
