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