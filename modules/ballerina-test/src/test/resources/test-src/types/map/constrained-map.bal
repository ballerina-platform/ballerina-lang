function testConstrainedMapValueTypePositive() (string) {
    map<string> testMap = {name:"kevin"};
    string val = testMap.name;
    return val;
}

function testConstrainedMapValueTypeNegative() (string) {
    map<string> testMap = {name:"kevin"};
    string val = testMap.names;
    return val;
}

function testConstrainedMapValueTypeIndexBasedPositive() (string) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    string val = testMap["name"];
    return val;
}

function testConstrainedMapValueTypeIndexBasedNegative() (string) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    string val = testMap["names"];
    return val;
}

struct Person {
    string name;
    int age;
}

function testConstrainedMapStructTypePositive() (string, int) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    Person val = testMap["item"];
    return val.name, val.age;
}

function testConstrainedMapStructTypeNegative() (Person) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    Person val = testMap["item-not"];
    return val;
}

function testAnyAssignment() (string) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    map anyMap = testMap;
    string val;
    val,_ = (string)anyMap["name"];
    return val;
}

function testConstrainedMapValueTypeAssignWithFieldAccessPositive() (string, string) {
    map<string> testMap = {};
    testMap.name = "kevin";
    testMap.sname = "ratnasekera";
    return testMap.name, testMap.sname;
}

function testConstrainedMapConstrainedWithConstrainedMap() (string, string) {
    map<map<string>> testMap = {};
    map<string> sMap = {name:"kevin"};
    sMap["sname"] = "ratnasekera";
    testMap.item = sMap;
    map<string> rSMap = testMap["item"];
    string name_r = rSMap.name;
    string sname_r = rSMap.sname;
    return name_r, sname_r;
}

function testConstrainedMapConstrainedWithConstrainedJson() (string, int) {
    map<json<Person>> testMap = {};
    json<Person> jP = {};
    jP.name = "Jack";
    jP.age = 25;
    testMap.item = jP;
    json<Person> rJP = testMap["item"];
    var j_name,_ =  (string) rJP.name;
    var j_age,_ =  (int) rJP.age;
    return j_name, j_age;
}

function testConstrainedMapConstrainedWithValueTypeArray() (int, int) {
    map<int[]> testMap = {};
    int[] intArr = [25, 30];
    testMap["item"] = intArr;
    int[] rIntArr = testMap.item;
    return rIntArr[0], rIntArr[1];
}