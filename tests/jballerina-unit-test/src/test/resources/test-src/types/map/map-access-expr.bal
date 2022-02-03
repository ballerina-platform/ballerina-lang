function mapAccessTest(int x, int y) returns (int) {
    map<any> testMap = {};
    int xx = 0;
    int yy = 0;
    testMap["first"] = x;
    testMap["second"] = y;
    testMap["third"] = x + y;
    testMap["forth"] = x - y;
    xx = <int> testMap["first"];
    yy = <int> testMap["second"];

    return xx + yy;
}

function mapReturnTest(string firstName, string lastName) returns (map<any>) {
    map<any> testMap = {};
    testMap["fname"] = firstName;
    testMap["lname"] = lastName;
    testMap[ firstName + lastName ] = firstName + lastName;
    return testMap;
}

function testArrayAccessAsIndexOfMapt() returns (string) {
    map<any> namesMap = {fname:"Supun",lname:"Setunga"};
    string[] keys = ["fname","lname"];
    string key = "";
    var a = namesMap[keys[0]];
    key =  a is string ? a : "";
    return key;
}

function testAccessThroughVar() returns (string) {
    map<any> m = {};
    m["x"] = "a";
    m["y"] = "b";
    m["z"] = "c";
    return constructString(m);
}

function constructString(map<any> m) returns (string) {
    string [] keys = m.keys();
    int len = keys.length();
    int i = 0;
    string returnStr = "";
    while (i < len) {
        string key = keys[i];
        var a = m[key];
        string val = a is string ? a : "";
        returnStr = returnStr + key + ":" + val + ", ";
        i = i + 1;
    }
    return returnStr;
}

function testMapRemoveAll() returns (int) {
    map<any> namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    namesMap.removeAll();
    return namesMap.length();
}

function testHasKeyPositive() returns (boolean) {
    map<any> namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname");
}

function testHasKeyNegative() returns (boolean) {
    map<any> namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return namesMap.hasKey("fname2");
}

function testGetMapValues () returns [string, string] {
    json j = {"city":"Colombo", "country":"SriLanka"};
    int[] arr = [7,8,9];
    map<any> address = {city:"CA", "country":"USA"};
    map<map<any>>[] addressArray=[{address:{city:"Colombo", "country":"SriLanka"}},
                               {address:{city:"Kandy", "country":"SriLanka"}},
                               {address:{city:"Galle", "country":"SriLanka"}}];
    map<any> m = { name:"Supun",
                  age:25,
                  gpa:2.81,
                  status:true,
                  info:(),
                  address:address,
                  intArray:arr,
                  addressArray:addressArray,
                  finfo:j
                };
    any[] values = [];
    values = m.reduce(function(any[] arr1, any value) returns any[] {
        arr1[arr1.length()] = value;
        return arr1;
    }, values);
    var nam = <string> values[0];
    var jsn = <json> values[8];
    var city = <string> checkpanic jsn.city;
    return [nam, city];
}

function testMapRemovePositive() returns [boolean, boolean, boolean] {
    map<any> namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    return [namesMap.hasKey("fname"), namesMap.remove("fname") === "Supun", namesMap.hasKey("fname")];
}

function testMapRemoveNegative() {
    map<any> namesMap = {fname:"Supun", lname:"Setunga", sname:"Kevin", tname:"Ratnasekera"};
    _ = namesMap.remove("fname2"); // panic
}

function testRemoveIfHasKeyPositive1() returns boolean {
    map<string> student = {id:"1", name:"Andrew", country:"Sri Lanka", city:"Colombo"};
    string? s = student.removeIfHasKey("city");
    if (s is ()) {
        return false;
    }
    return <string> s == "Colombo";
}

function testRemoveIfHasKeyNegative1() returns boolean {
    map<string> student = {id:"1", name:"Andrew", country:"Sri Lanka", city:"Colombo"};
    string? s = student.removeIfHasKey("age");
    if (s is ()) {
        return false;
    }
    return <string> s == "Sri Lanka";
}

function testRemoveIfHasKeyPositive2() returns boolean {
    map<any> student = {id:1, name:"Andrew", country:"Sri Lanka", city:"Colombo"};
    var s = student.removeIfHasKey("city");
    if (s is ()) {
        return false;
    }
    return <string> s == "Colombo";
}

function testRemoveIfHasKeyNegative2() returns boolean {
    map<any> student = {id:1, name:"Andrew", country:"Sri Lanka", city:"Colombo"};
    var s = student.removeIfHasKey("age");
    if (s is ()) {
        return false;
    }
    return <string> s == "Sri Lanka";
}

function testMapToString() returns string {
    map<map<json>> arr = {};
    typedesc<any> t = typeof arr;
    return t.toString();
}
