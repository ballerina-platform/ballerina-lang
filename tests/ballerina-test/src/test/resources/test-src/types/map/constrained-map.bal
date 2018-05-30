function testConstrainedMapValueTypePositive () returns (string) {
    map<string> testMap = {name:"kevin"};
    string val = testMap.name;
    return val;
}

function testConstrainedMapValueTypeNegative () returns (string) {
    map<string> testMap = {name:"kevin"};
    string val = testMap.names;
    return val;
}

function testConstrainedMapValueTypeIndexBasedPositive () returns (string) {
    map<string> testMap;
    testMap["name"] = "kevin";
    string val = testMap["name"];
    return val;
}

function testConstrainedMapValueTypeIndexBasedNegative () returns (string|()) {
    map<string> testMap;
    testMap["name"] = "kevin";
    string|() val = testMap["names"];
    return val;
}

function testConstrainedMapStructTypePositive () returns ((string, int)) {
    map<Person> testMap;
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    Person val = testMap["item"];
    return (val.name, val.age);
}

function testConstrainedMapStructTypeNegative () returns (Person) {
    map<Person> testMap;
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    Person val = testMap["item-not"];
    return val;
}

function testConstrainedMapValueTypeAssignWithFieldAccessPositive () returns ((string, string)) {
    map<string> testMap;
    testMap.name = "kevin";
    testMap.sname = "ratnasekera";
    return (testMap.name, testMap.sname);
}

function testConstrainedMapConstrainedWithConstrainedMap () returns ((string, string)) {
    map<map<string>> testMap;
    map<string> sMap = {name:"kevin"};
    sMap["sname"] = "ratnasekera";
    testMap.item = sMap;
    map<string> rSMap = testMap["item"];
    string name_r = rSMap.name;
    string sname_r = rSMap.sname;
    return (name_r, sname_r);
}

function testConstrainedMapConstrainedWithConstrainedJson () returns ((string, int)) {
    map<json<Person>> testMap;
    json<Person> jP = {};
    jP.name = "Jack";
    jP.age = 25;
    testMap.item = jP;
    json<Person> rJP = testMap["item"];
    string j_name = check < string > rJP.name;
    int j_age = check < int > rJP.age;
    return (j_name, j_age);
}

function testConstrainedMapConstrainedWithValueTypeArray () returns ((int, int)) {
    map<int[]> testMap;
    int[] intArr = [25, 30];
    testMap["item"] = intArr;
    int[] rIntArr = testMap.item;
    return (rIntArr[0], rIntArr[1]);
}

function testConstrainedMapIntTypePositive () returns ((int, int)) {
    map<int> testMap;
    testMap["item_1"] = 36;
    testMap["item_2"] = 63;
    int item_1 = testMap["item_1"];
    int item_2 = testMap["item_2"];
    return (item_1, item_2);
}

function testConstrainedMapIntTypeNegative () returns ((int, int)) {
    map<int> testMap;
    testMap["item_1"] = 36;
    testMap["item_2"] = 63;
    int item_1 = testMap["item_1"];
    int item_2 = testMap["item_2"];
    return (item_1, item_2);
}

function testConstrainedMapFloatTypePositive () returns ((float, float)) {
    float dummy = 6.9;
    map<float> testMap;
    testMap["item_1"] = 3.6;
    testMap["item_2"] = 6.3;
    float item_1 = testMap["item_1"];
    float item_2 = testMap["item_2"];
    dummy = 9.6;
    return (item_1, item_2);
}

function testConstrainedMapFloatTypeNegative () returns ((float, float)) {
    float dummy = 6.9;
    map<float> testMap;
    testMap["item_1"] = 3.6;
    testMap["item_2"] = 6.3;
    float item_1 = testMap["item_1"];
    float item_2 = testMap["item_2"];
    dummy = 9.6;
    return (item_1, item_2);
}

function testConstrainedMapBooleanTypePositive () returns ((boolean, boolean)) {
    map<boolean> testMap;
    testMap["item_1"] = true;
    testMap["item_2"] = false;
    boolean item_1 = testMap["item_1"];
    boolean item_2 = testMap["item_2"];
    return (item_1, item_2);
}

function testConstrainedMapBooleanTypeNegative () returns ((boolean, boolean)) {
    map<boolean> testMap;
    testMap["item_1"] = true;
    testMap["item_2"] = false;
    boolean item_1 = testMap["item_1"];
    boolean item_2 = testMap["item_2"];
    return (item_1, item_2);
}

function testConstrainedMapBlobTypePositive () returns ((string, string)) {
    map<blob> testMap;
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

function testConstrainedMapBlobTypeNegative () returns ((string, string)) {
    map<blob> testMap;
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

function testConstrainedMapValueTypeCast () returns (string) {
    map<string> testMap = {name:"kevin"};
    map m = getGenericMap(testMap);
    map<string> castMap;
    castMap = check < map<string> > m;
    string val = castMap.name;
    return val;
}

function testConstrainedMapValueTypeCastNegative () returns (map<int>|error) {
    map<string> testMap = {name:"kevin"};
    map m = getGenericMap(testMap);
    var value = <map<int>>m;
    match value {
        error er => {
            return er;
        }
        map<int> castMap => {
            return castMap;
        }

    }
}

function getGenericMap (map m) returns (map) {
    return m;
}

function testConstrainedMapRefTypeCast () returns ((string, int)) {
    map<Person> testMap;
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map m = getGenericMap(testMap);
    map<Person> castMap;
    castMap = check < map<Person> > m;
    Person p = castMap["item"];
    return (p.name, p.age);
}

function testConstrainedMapRefTypeCastNegative () returns (map<int>|error) {
    map<Person> testMap;
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map m = getGenericMap(testMap);

    var value = <map<int>>m;
    match value {
        map<int> castMap => {
            return castMap;
        }
        error err => {
            return err;
        }
    }
}

function testUpdateStringMap () returns (string) {
    map<string> testMap;
    map m = updateGenericMap(testMap);
    map<string> castMap = check < map<string> > m;
    string val = castMap.item;
    return val;
}

function updateGenericMap (map m) returns (map) {
    m["item"] = "update";
    return m;
}

function testStringMapUpdateWithInvalidTypeNegativeCase () returns (string) {
    map<string> testMap;
    map m = updateGenericMapDifferentType(testMap);
    map<string> castMap = check < map<string> > m;
    string val = castMap.item;
    return val;
}

function updateGenericMapDifferentType (map m) returns (map) {
    m["item"] = 1;
    return m;
}

function testStringMapUpdateWithInvalidNullTypeNegativeCase () returns (string) {
    map<string> testMap;
    map m = updateGenericMapWithNullValue(testMap);
    map<string> castMap = check < map<string> > m;
    string val = castMap.item;
    return val;
}

function updateGenericMapWithNullValue (map m) returns (map) {
    m["item"] = null;
    return m;
}

type Person {
     string name,
    int age,
    string address;
};

type Employee {
     string name,
    int age;
};

function testStructConstrainedMapRuntimeCast () returns ((string, int)) {
    map<Person> testMap;
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map m = getGenericMap(testMap);
    map<Employee> castMap = check < map<Employee> > m;
    Employee p = castMap["item"];
    return (p.name, p.age);
}

function testStructConstrainedMapStaticCast () returns ((string, int)) {
    map<Person> testMap;
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map<Employee> castMap = <map<Employee>>testMap;
    Employee p = castMap["item"];
    return (p.name, p.age);
}


function testStructEquivalentMapUpdate () returns ((string, int)) {
    map<Person> testMap;
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map<Employee> m = updateEquivalentMap(testMap);
    map<Employee> castMap = <map<Employee>>m;
    Employee p = castMap["item"];
    return (p.name, p.age);
}

function updateEquivalentMap (map<Employee> m) returns (map<Employee>) {
    Employee b = {name:"Kevin", age:75};
    m["update"] = b;
    return m;
}

function testStructEquivalentMapAccess () returns ((string, int)) {
    map<Person> testMap;
    Person jack = {name:"Mervin", age:25, address:"Usa"};
    testMap["item"] = jack;
    return equivalentMapAccess(testMap);
}

function equivalentMapAccess (map<Employee> m) returns ((string, int)) {
    Employee b = m["item"];
    return (b.name, b.age);
}

function testStructMapUpdate () returns ((string, int)) {
    map<Person> testMap;
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map m = updateStructMap(testMap);
    map<Employee> castMap = check < map<Employee> > m;
    Employee p = castMap["update"];
    return (p.name, p.age);
}

function updateStructMap (map m) returns (map) {
    Person k = {name:"Arnold", age:45, address:"UK"};
    m["update"] = k;
    return m;
}

function testStructNotEquivalentRuntimeCast () returns (map<Person>|error) {
    map<Employee> testMap;
    Employee jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map m = getGenericMap(testMap);

    var val = <map<Person>>m;
    match val {
        map<Person> personMap => {
            return personMap;
        }
        error err => {
            return err;
        }
    }
}

function testAnyMapToValueTypeRuntimeCast () returns (map<int>|error) {
    map testMap;
    testMap["item"] = 5;
    var value = <map<int>>testMap;
    match value {
        map<int> intMap => {
            return intMap;
        }
        error err => {
            return err;
        }
    }
}

function testAnyMapToRefTypeRuntimeCast () returns (map<Employee>|error) {
    map testMap;
    Employee jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    var value = <map<Employee>>testMap;
    match value {
        map<Employee> employeeMap => {
            return employeeMap;
        }
        error err =>
        return err;
    }
}

type Student {
     int index,
    int age;
};

function testMapToStructConversion () returns ((int, int)) {
    map<int> testMap;
    testMap["index"] = 100;
    testMap["age"] = 63;
    Student k = check < Student > testMap;
    return (k.index, k.age);
}

function testMapToStructConversionNegative () returns (Student|error) {
    map<string> testMap;
    testMap["index"] = "100";
    testMap["age"] = "63";
    var value = <Student>testMap;
    match value {
        Student student => {
            return student;
        }
        error err => {
            return err;
        }
    }
}

function testMapFunctionsOnConstrainedMaps () returns (string[]) {
    map<string> testMap;
    testMap["index"] = "100";
    testMap["age"] = "63";
    return testMap.keys();
}

function testForEachOnConstrainedMaps () returns ((string, string)) {
    map<string> testMap;
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

function testMapOfElementTypeArray () returns ((string, string)) {
    map<string[]> testMap;
    string[] s1 = ["name", "sname"];
    string[] s2 = ["Kollupitiya", "Ja-Ela"];
    testMap["a1"] = s1;
    testMap["a2"] = s2;
    string[] r2 = testMap["a2"];
    return (r2[0], r2[1]);
}

function testMapOfElementTypeRefArray () returns ((string, int)) {
    map<Employee[]> testMap;
    Employee jack = {name:"Jack", age:25};
    Employee[] e1 = [];
    e1[0] = jack;
    testMap["e1"] = e1;
    Employee[] r2 = testMap["e1"];
    Employee jackR = r2[0];
    return (jackR.name, jackR.age);
}

type PersonComplex {
     string name,
    int age,
    PersonComplex? parent,
    json info,
    map<string> address,
    int[] marks,
    any a,
    float score,
    boolean alive;
};

function testJsonToStructConversionStructWithConstrainedMap () returns (string, string) {
    json j = {name:"Child",
                 age:25,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent:{},
                            address:{},
                            info:{},
                            marks:[],
                            a:{},
                            score:4.57,
                            alive:false
                        },
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[56, 79],
                 a:"any value",
                 score:5.67,
                 alive:true
             };
    PersonComplex p = check < PersonComplex > j;
    map<string> ms = p.address;
    return (ms["city"], ms["country"]);
}

type PersonComplexTwo {
    string name,
    int age,
    PersonComplexTwo? parent,
    json info,
    map<int> address,
    int[] marks,
    any a,
    float score,
    boolean alive;
};

function testJsonToStructConversionStructWithConstrainedMapNegative () returns (PersonComplexTwo|error) {
    json j = {name:"Child",
                 age:25,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent:{},
                            address:{},
                            info:{},
                            marks:[],
                            a:{},
                            score:4.57,
                            alive:false
                        },
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[56, 79],
                 a:"any value",
                 score:5.67,
                 alive:true
             };
    var value = <PersonComplexTwo>j;
    match value {
        PersonComplexTwo personComplexTwo => {
            return personComplexTwo;
        }
        error err => {
            return err;
        }

    }
}

function testConstrainedUnionRetrieveString () returns (string) {
    map<string> testMap;
    testMap["name"] = "kevin";
    string|int s = testMap["name"];
    match s {
        string k => return k;
        int l => return "default";
    }
}

function testConstrainedUnionRetrieveInt () returns (int) {
    map<int> testMap;
    testMap["id"] = 3;
    string|int id = testMap["id"];
    match id {
        string k => return 0;
        int l => return l;
    }
}

function testConstrainedMapWithIncrementOperator () returns (int) {
    map<int> testMap;
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

function testConstrainedMapWithCompoundAssignment () returns (int) {
    map<int> testMap;
    testMap["count"] = 1000;
    testMap["count"] += 25000;
    testMap["count"] /= 1000;
    int j = testMap["count"];
    testMap["count"] *= 2;
    return testMap["count"];
}

function testMapConstrainedEquivalentMapInsert () returns (string, int) {
    map<Employee> emp;
    Person jack = {name:"Jack", age:25, address:"Usa"};
    emp["jack"] = jack;
    return (emp["jack"].name, emp["jack"].age);
}

type Transaction {
    string transactionId,
    string coordinationType,
    map<Participant> participants,
    Protocol[] coordinatorProtocols;
};

type Participant {
    string participantId,
    Protocol[] participantProtocols;
};

type Protocol {
    string name,
    string url,
    int transactionBlockId,
    (function (string transactionId,
               int transactionBlockId,
               string protocolAction) returns boolean)|() protocolFn;
};

type TwoPhaseCommitTransaction {
    string transactionId,
    string coordinationType,
    map<Participant> participants,
    Protocol[] coordinatorProtocols,
    boolean possibleMixedOutcome;
};

function testRuntimeStructEquivalencyWithNestedConstrainedMaps () returns (string) {
    map<Transaction> initiatedTransactions;
    TwoPhaseCommitTransaction tpc = {transactionId:"TR-ID", coordinationType:"2pc"};
    initiatedTransactions["Foo"] = tpc;
    return initiatedTransactions["Foo"].transactionId;
}

function testMapConstrainedToUnion () returns (string|int) {
    map<string|int> testMap;
    testMap["test"] = "test-value";
    return testMap["test"];
}

function testMapConstrainedToUnionCaseTwo () returns (string|int) {
    map<string|int> testMap;
    testMap["test"] = 2;
    return testMap["test"];
}

function testMapConstrainedToUnionCaseThree () returns (string|int) {
    map<string|int> testMap;
    return testMap["non-existing-key"];
}

function testMapConstrainedStringNonExistingKeyRetrieve () returns (string?) {
    map<string> testMap;
    return testMap["nonexist-key"];
}

function testMapConstrainedToNullableUnion () returns (string?) {
    map<string?> testMap;
    testMap["test"] = "test-nullable-union";
    return testMap["test"];
}

function testMapConstrainedToNullableUnionNonExistingKey () returns (string?) {
    map<string?> testMap;
    return testMap.nonexist;
}

function testMapConstrainedToNullableUnionNonExistingKeyWithIndexAccess () returns (string?) {
    map<string?> testMap;
    return testMap["nonexist"];
}


