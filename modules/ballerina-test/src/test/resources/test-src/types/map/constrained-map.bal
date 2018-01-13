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

function testConstrainedMapIntTypePositive() (int, int) {
    map<int> testMap = {};
    testMap["item_1"] = 36;
    testMap["item_2"] = 63;
    int item_1 = testMap["item_1"];
    int item_2 = testMap["item_2"];
    return item_1, item_2;
}

function testConstrainedMapIntTypeNegative() (int, int) {
    map<int> testMap = {};
    testMap["item_1"] = 36;
    testMap["item_2"] = 63;
    int item_1 = testMap["item_1_n"];
    int item_2 = testMap["item_2_n"];
    return item_1, item_2;
}

function testConstrainedMapFloatTypePositive() (float, float) {
    float dummy = 6.9;
    map<float> testMap = {};
    testMap["item_1"] = 3.6;
    testMap["item_2"] = 6.3;
    float item_1 = testMap["item_1"];
    float item_2 = testMap["item_2"];
    dummy = 9.6;
    return item_1, item_2;
}

function testConstrainedMapFloatTypeNegative() (float, float) {
    float dummy = 6.9;
    map<float> testMap = {};
    testMap["item_1"] = 3.6;
    testMap["item_2"] = 6.3;
    float item_1 = testMap["item_1_n"];
    float item_2 = testMap["item_2_n"];
    dummy = 9.6;
    return item_1, item_2;
}

function testConstrainedMapBooleanTypePositive() (boolean, boolean) {
    map<boolean> testMap = {};
    testMap["item_1"] = true;
    testMap["item_2"] = false;
    boolean item_1 = testMap["item_1"];
    boolean item_2 = testMap["item_2"];
    return item_1, item_2;
}

function testConstrainedMapBooleanTypeNegative() (boolean, boolean) {
    map<boolean> testMap = {};
    testMap["item_1"] = true;
    testMap["item_2"] = false;
    boolean item_1 = testMap["item_1_n"];
    boolean item_2 = testMap["item_2_n"];
    return item_1, item_2;
}

function testConstrainedMapBlobTypePositive() (string, string) {
    map<blob> testMap = {};
    string sitem_1 = "hi";
    blob bitem_1 = sitem_1.toBlob("UTF-8");
    string sitem_2 = "ballerina";
    blob bitem_2 = sitem_2.toBlob("UTF-8");
    testMap["item_1"] = bitem_1;
    testMap["item_2"] = bitem_2;
    blob item_1 = testMap["item_1"];
    blob item_2 = testMap["item_2"];
    string rsitem_1 = item_1.toString("UTF-8");
    string rsitem_2 = item_2.toString("UTF-8");
    return rsitem_1, rsitem_2;
}

function testConstrainedMapBlobTypeNegative() (string, string) {
    map<blob> testMap = {};
    string sitem_1 = "hi";
    blob bitem_1 = sitem_1.toBlob("UTF-8");
    string sitem_2 = "ballerina";
    blob bitem_2 = sitem_2.toBlob("UTF-8");
    testMap["item_1"] = bitem_1;
    testMap["item_2"] = bitem_2;
    blob item_1 = testMap["item_1_n"];
    blob item_2 = testMap["item_2_n"];
    string rsitem_1 = item_1.toString("UTF-8");
    string rsitem_2 = item_2.toString("UTF-8");
    return rsitem_1, rsitem_2;
}