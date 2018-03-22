function testConstrainedMapValueTypePositive() returns (string) {
    map<string> testMap = {name:"kevin"};
    string val = testMap.name;
    return val;
}

function testConstrainedMapValueTypeNegative() returns (string) {
    map<string> testMap = {name:"kevin"};
    string val = testMap.names;
    return val;
}

function testConstrainedMapValueTypeIndexBasedPositive() returns (string) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    string val = testMap["name"];
    return val;
}

function testConstrainedMapValueTypeIndexBasedNegative() returns (string) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    string val = testMap["names"];
    return val;
}

function testConstrainedMapStructTypePositive() returns ((string, int)) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    Person val = testMap["item"];
    return (val.name, val.age);
}

function testConstrainedMapStructTypeNegative() returns (Person) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    Person val = testMap["item-not"];
    return val;
}

function testConstrainedMapValueTypeAssignWithFieldAccessPositive() returns ((string, string)) {
    map<string> testMap = {};
    testMap.name = "kevin";
    testMap.sname = "ratnasekera";
    return (testMap.name, testMap.sname);
}

function testConstrainedMapConstrainedWithConstrainedMap() returns ((string, string)) {
    map<map<string>> testMap = {};
    map<string> sMap = {name:"kevin"};
    sMap["sname"] = "ratnasekera";
    testMap.item = sMap;
    map<string> rSMap = testMap["item"];
    string name_r = rSMap.name;
    string sname_r = rSMap.sname;
    return (name_r, sname_r);
}

function testConstrainedMapConstrainedWithConstrainedJson() returns ((string, int)) {
    map<json<Person>> testMap = {};
    json<Person> jP = {};
    jP.name = "Jack";
    jP.age = 25;
    testMap.item = jP;
    json<Person> rJP = testMap["item"];
    var j_name,_ =  (string) rJP.name;
    var j_age,_ =  (int) rJP.age;
    return (j_name, j_age);
}

function testConstrainedMapConstrainedWithValueTypeArray() returns ((int, int)) {
    map<int[]> testMap = {};
    int[] intArr = [25, 30];
    testMap["item"] = intArr;
    int[] rIntArr = testMap.item;
    return (rIntArr[0], rIntArr[1]);
}

function testConstrainedMapIntTypePositive() returns ((int, int)) {
    map<int> testMap = {};
    testMap["item_1"] = 36;
    testMap["item_2"] = 63;
    int item_1 = testMap["item_1"];
    int item_2 = testMap["item_2"];
    return (item_1, item_2);
}

function testConstrainedMapIntTypeNegative() returns ((int, int)) {
    map<int> testMap = {};
    testMap["item_1"] = 36;
    testMap["item_2"] = 63;
    int item_1 = testMap["item_1_n"];
    int item_2 = testMap["item_2_n"];
    return (item_1, item_2);
}

function testConstrainedMapFloatTypePositive() returns ((float, float)) {
    float dummy = 6.9;
    map<float> testMap = {};
    testMap["item_1"] = 3.6;
    testMap["item_2"] = 6.3;
    float item_1 = testMap["item_1"];
    float item_2 = testMap["item_2"];
    dummy = 9.6;
    return (item_1, item_2);
}

function testConstrainedMapFloatTypeNegative() returns ((float, float)) {
    float dummy = 6.9;
    map<float> testMap = {};
    testMap["item_1"] = 3.6;
    testMap["item_2"] = 6.3;
    float item_1 = testMap["item_1_n"];
    float item_2 = testMap["item_2_n"];
    dummy = 9.6;
    return (item_1, item_2);
}

function testConstrainedMapBooleanTypePositive() returns ((boolean, boolean)) {
    map<boolean> testMap = {};
    testMap["item_1"] = true;
    testMap["item_2"] = false;
    boolean item_1 = testMap["item_1"];
    boolean item_2 = testMap["item_2"];
    return (item_1, item_2);
}

function testConstrainedMapBooleanTypeNegative() returns ((boolean, boolean)) {
    map<boolean> testMap = {};
    testMap["item_1"] = true;
    testMap["item_2"] = false;
    boolean item_1 = testMap["item_1_n"];
    boolean item_2 = testMap["item_2_n"];
    return (item_1, item_2);
}

function testConstrainedMapBlobTypePositive() returns ((string, string)) {
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
    return (rsitem_1, rsitem_2);
}

function testConstrainedMapBlobTypeNegative() returns ((string, string)) {
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
    return (rsitem_1, rsitem_2);
}

function testConstrainedMapValueTypeCast() returns (string) {
    map<string> testMap = {name:"kevin"};
    map m = getGenericMap(testMap);
    map<string> castMap;
    castMap, _ = (map<string>) m;
    string val = castMap.name;
    return val;
}

function testConstrainedMapValueTypeCastNegative() returns (error) {
    map<string> testMap = {name:"kevin"};
    map m = getGenericMap(testMap);
    map<int> castMap;
    error er;
    _, er = (map<int>) m;
    return er;
}

function getGenericMap(map m) returns (map){
    return m;
}

function testConstrainedMapRefTypeCast() returns ((string, int)) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map m = getGenericMap(testMap);
    map<Person> castMap;
    castMap, _ = (map<Person>) m;
    Person p = castMap["item"];
    return (p.name, p.age);
}

function testConstrainedMapRefTypeCastNegative() returns (error) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map m = getGenericMap(testMap);
    map<int> castMap;
    error er;
    _, er = (map<int>) m;
    return er;
}

function testUpdateStringMap() returns (string) {
    map<string> testMap = {};
    map m = updateGenericMap(testMap);
    map<string> castMap;
    castMap, _ = (map<string>) m;
    string val = castMap.item;
    return val;
}

function updateGenericMap(map m) returns (map) {
    m["item"] = "update";
    return m;
}

function testStringMapUpdateWithInvalidTypeNegativeCase() returns (string) {
    map<string> testMap = {};
    map m = updateGenericMapDifferentType(testMap);
    map<string> castMap;
    castMap, _ = (map<string>) m;
    string val = castMap.item;
    return val;
}

function updateGenericMapDifferentType(map m) returns (map) {
    m["item"] = 1;
    return m;
}

function testStringMapUpdateWithInvalidNullTypeNegativeCase() returns (string) {
    map<string> testMap = {};
    map m = updateGenericMapWithNullValue(testMap);
    map<string> castMap;
    castMap, _ = (map<string>) m;
    string val = castMap.item;
    return val;
}

function updateGenericMapWithNullValue(map m) returns (map) {
    m["item"] = null;
    return m;
}

struct Person {
    string name;
    int age;
    string address;
}

struct Employee {
    string name;
    int age;
}

function testStructConstrainedMapRuntimeCast() returns ((string, int)) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map m = getGenericMap(testMap);
    map<Employee> castMap;
    castMap, _ = (map<Employee>) m;
    Employee p = castMap["item"];
    return (p.name, p.age);
}

function testStructConstrainedMapStaticCast() returns ((string, int)) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map<Employee> castMap;
    castMap, _ = (map<Employee>) testMap;
    Employee p = castMap["item"];
    return (p.name, p.age);
}


function testStructEquivalentMapUpdate() returns ((string, int)) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map<Employee> m = updateEquivalentMap(testMap);
    map<Employee> castMap;
    castMap, _ = (map<Employee>) m;
    Employee p = castMap["item"];
    return (p.name, p.age);
}

function updateEquivalentMap(map<Employee> m) returns (map<Employee>) {
    Employee b = {name:"Kevin", age:75};
    m["update"] = b;
    return m;
}

function testStructEquivalentMapAccess() returns ((string, int)) {
    map<Person> testMap = {};
    Person jack = {name:"Mervin", age:25, address:"Usa"};
    testMap["item"] = jack;
    return equivalentMapAccess(testMap);
}

function equivalentMapAccess(map<Employee> m) returns ((string, int)) {
    Employee b = m["item"];
    return (b.name, b.age);
}

function testStructMapUpdate() returns ((string, int)) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map m = updateStructMap(testMap);
    map<Employee> castMap;
    castMap,_ = (map<Employee>) m;
    Employee p = castMap["update"];
    return (p.name, p.age);
}

function updateStructMap(map m) returns (map) {
    Person k = {name:"Arnold", age:45, address:"UK"};
    m["update"] = k;
    return m;
}

function testStructNotEquivalentRuntimeCast() returns (error) {
    map<Employee> testMap = {};
    Employee jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map m = getGenericMap(testMap);
    error err;
    _, err = (map<Person>) m;
    return err;
}

function testAnyMapToValueTypeRuntimeCast() returns (error) {
    map testMap = {};
    testMap["item"] = 5;
    error err;
    _, err = (map<int>) testMap;
    return err;
}

function testAnyMapToRefTypeRuntimeCast() returns (error) {
    map testMap = {};
    Employee jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    error err;
    _, err = (map<Employee>) testMap;
    return err;
}

struct Student {
    int index;
    int age;
}

function testMapToStructConversion() returns ((int, int)) {
    map<int> testMap = {};
    testMap["index"] = 100;
    testMap["age"] = 63;
    Student k;
    k,_ = <Student>testMap;
    return (k.index, k.age);
}

function testMapToStructConversionNegative() returns (error) {
    map<string> testMap = {};
    testMap["index"] = "100";
    testMap["age"] = "63";
    error err;
    _,err = <Student>testMap;
    return err;
}

function testMapFunctionsOnConstrainedMaps() returns (string[]) {
    map<string> testMap = {};
    testMap["index"] = "100";
    testMap["age"] = "63";
    return testMap.keys();
}

function testForEachOnConstrainedMaps() returns ((string, string)) {
    map<string> testMap = {};
    testMap["name"] = "Ronnie";
    testMap["sname"] = "Coleman";
    string[] arr = [];
    int index = 0;
    foreach v in testMap {
            arr[index] = v;
            index = index + 1;
    }
    return (arr[0], arr[1]);
}

function testMapOfElementTypeArray() returns ((string, string)) {
    map<string[]> testMap = {};
    string[] s1 = ["name", "sname"];
    string[] s2 = ["Kollupitiya", "Ja-Ela"];
    testMap["a1"] = s1;
    testMap["a2"] = s2;
    string[] r2 = testMap["a2"];
    return (r2[0], r2[1]);
}

function testMapOfElementTypeRefArray() returns ((string, int)) {
    map<Employee[]> testMap = {};
    Employee jack = {name:"Jack", age:25};
    Employee[] e1 = [];
    e1[0] = jack;
    testMap["e1"] = e1;
    Employee[] r2 = testMap["e1"];
    Employee jackR = r2[0];
    return (jackR.name, jackR.age);
}

//struct PersonComplex {
//    string name;
//    int age;
//    PersonComplex parent;
//    json info;
//    map<string> address;
//    int[] marks;
//    any a;
//    float score;
//    boolean alive;
//}

//function testJsonToStructConversionStructWithConstrainedMap() returns (string, string) {
//    json j = { name:"Child",
//               age:25,
//               parent:{
//                    name:"Parent",
//                    age:50,
//                    parent: null,
//                    address:null,
//                    info:null,
//                    marks:null,
//                    a:null,
//                    score: 4.57,
//                    alive:false
//               },
//               address:{"city":"Colombo", "country":"SriLanka"},
//               info:{status:"single"},
//               marks:[56,79],
//               a:"any value",
//               score: 5.67,
//               alive:true
//             };
//    PersonComplex p;
//    p,_ = <PersonComplex> j;
//    map<string> ms = p.address;
//    return (ms["city"], ms["country"]);
//}

//struct PersonComplexTwo {
//    string name;
//    int age;
//    PersonComplexTwo parent;
//    json info;
//    map<int> address;
//    int[] marks;
//    any a;
//    float score;
//    boolean alive;
//}

//function testJsonToStructConversionStructWithConstrainedMapNegative() returns (error) {
//    json j = { name:"Child",
//               age:25,
//               parent:{
//                    name:"Parent",
//                    age:50,
//                    parent: null,
//                    address:null,
//                    info:null,
//                    marks:null,
//                    a:null,
//                    score: 4.57,
//                    alive:false
//               },
//               address:{"city":"Colombo", "country":"SriLanka"},
//               info:{status:"single"},
//               marks:[56,79],
//               a:"any value",
//               score: 5.67,
//               alive:true
//             };
//    error err;
//    _,err = <PersonComplexTwo> j;
//    return err;
//}

function testConstrainedUnionRetrieveString() returns (string) {
    map<string|int> testMap = {};
    testMap["name"] = "kevin";
    string|int s = testMap["name"];
    match s {
        string k => return k;
        int l    => return "default";
    }
}

function testConstrainedUnionRetrieveInt() returns (int) {
    map<string|int> testMap = {};
    testMap["id"] = 3;
    string|int id = testMap["id"];
    match id {
        string k => return 0;
        int l    => return l;
    }
}

function testConstrainedMapWithIncrementOperator() returns (int) {
    map<int> testMap = {};
    testMap["count"] = 1000;
    testMap["count"]++;
    testMap["count"]++;
    testMap["count"]++;
    testMap["count"]--;
    int i = 5;
    int j = testMap["count"];
    testMap["count"]++;
    return testMap["count"];
}

function testConstrainedMapWithCompoundAssignment() returns (int) {
    map<int> testMap = {};
    testMap["count"] = 1000;
    testMap["count"] += 25000;
    testMap["count"] /= 1000;
    int j = testMap["count"];
    testMap["count"] *= 2;
    return testMap["count"];
}

function testMapConstrainedEquivalentMapInsert() returns (string, int) {
    map<Employee> emp = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    emp["jack"] = jack;
    return (emp["jack"].name, emp["jack"].age);
}