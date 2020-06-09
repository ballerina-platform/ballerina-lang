function testConstrainedMapValueTypePositive () returns (string) {
    map<string> testMap = {name:"kevin"};
    string val = <string> testMap["name"];
    return val;
}

function testConstrainedMapValueTypeNegative () returns (string) {
    map<string> testMap = {name:"kevin"};
    string val = testMap.get("names");
    return val;
}

function testConstrainedMapValueTypeIndexBasedPositive () returns (string) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    var item = testMap["name"];
    string val =  item is string ? item : "";
    return val;
}

function testConstrainedMapValueTypeIndexBasedNegative () returns (string|()) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    string|() val = testMap["names"];
    return val;
}

function testConstrainedMapStructTypePositive () returns ([string, int]) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    Person val = <Person> testMap["item"];
    return [val.name, val.age];
}

function testConstrainedMapStructTypeNegative () returns (Person) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    Person val = testMap.get("item-not");
    return val;
}

function testConstrainedMapValueTypeAssignWithMemberAccessPositive () returns ([string?, string?]) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    testMap["sname"] = "ratnasekera";
    return [testMap["name"], testMap["sname"]];
}

function testConstrainedMapConstrainedWithConstrainedMap () returns ([string, string]) {
    map<map<string>> testMap = {};
    map<string> sMap = {name:"kevin"};
    sMap["sname"] = "ratnasekera";
    testMap["item"] = sMap;
    map<string> rSMap = <map<string>> testMap["item"];
    string name_r = <string> rSMap["name"];
    string sname_r = <string> rSMap["sname"];
    return [name_r, sname_r];
}

function testConstrainedMapConstrainedWithValueTypeArray () returns ([int, int]) {
    map<int[]> testMap = {};
    int[] intArr = [25, 30];
    testMap["item"] = intArr;
    int[] rIntArr = <int[]> testMap["item"];
    return [rIntArr[0], rIntArr[1]];
}

function testConstrainedMapIntTypePositive () returns ([int, int]) {
    map<int> testMap = {};
    testMap["item_1"] = 36;
    testMap["item_2"] = 63;
    int item_1 = <int> testMap["item_1"];
    int item_2 = <int> testMap["item_2"];
    return [item_1, item_2];
}

function testConstrainedMapIntTypeNegative () returns ([int, int]) {
    map<int> testMap = {};
    testMap["item_1"] = 36;
    testMap["item_2"] = 63;
    var item = testMap["item_1_n"];
    int item_1 = item is int ? item : 0;
    item = testMap["item_2_n"];
    int item_2 = item is int ? item : 0;
    return [item_1, item_2];
}

function testConstrainedMapFloatTypePositive () returns ([float, float]) {
    float dummy = 6.9;
    map<float> testMap = {};
    testMap["item_1"] = 3.6;
    testMap["item_2"] = 6.3;
    float item_1 = <float> testMap["item_1"];
    float item_2 = <float> testMap["item_2"];
    dummy = 9.6;
    return [item_1, item_2];
}

function testConstrainedMapFloatTypeNegative () returns ([float, float]) {
    float dummy = 6.9;
    map<float> testMap = {};
    testMap["item_1"] = 3.6;
    testMap["item_2"] = 6.3;
    var item = testMap["item_1_n"];
    float item_1 =  item is float ? item : 0.0;
    item = testMap["item_2_n"];
    float item_2 = item is float ? item : 0.0;
    dummy = 9.6;
    return [item_1, item_2];
}

function testConstrainedMapBooleanTypePositive () returns ([boolean, boolean]) {
    map<boolean> testMap = {};
    testMap["item_1"] = true;
    testMap["item_2"] = false;
    boolean item_1 = <boolean> testMap["item_1"];
    boolean item_2 = <boolean> testMap["item_2"];
    return [item_1, item_2];
}

function testConstrainedMapBooleanTypeNegative () returns ([boolean, boolean]) {
    map<boolean> testMap = {};
    testMap["item_1"] = true;
    testMap["item_2"] = false;
    var item = testMap["item_1_n"];
    boolean item_1 = item is boolean ? item : false;
    item = testMap["item_2_n"];
    boolean item_2 = item is boolean ? item : false;
    return [item_1, item_2];
}

function testConstrainedMapBlobTypePositive () returns [byte[], byte[]] {
    map<byte[]> testMap = {};
    string sitem_1 = "hi";
    byte[] bitem_1 = sitem_1.toBytes();
    string sitem_2 = "ballerina";
    byte[] bitem_2 = sitem_2.toBytes();
    testMap["item_1"] = bitem_1;
    testMap["item_2"] = bitem_2;
    byte[] item_1 = <byte[]> testMap["item_1"];
    byte[] item_2 = <byte[]> testMap["item_2"];
    return [item_1, item_2];
}

function testConstrainedMapBlobTypeNegative () returns [byte[], byte[]] {
    map<byte[]> testMap = {};
    string sitem_1 = "hi";
    byte[] bitem_1 = sitem_1.toBytes();
    string sitem_2 = "ballerina";
    byte[] bitem_2 = sitem_2.toBytes();
    testMap["item_1"] = bitem_1;
    testMap["item_2"] = bitem_2;
    
    byte[] item_1 = [];
    var b = testMap["item_1"];
    if (b is byte[]) {
        item_1 = b;
    }
    
    byte[] item_2 = [];
    b = testMap["item_2"];
    if (b is byte[]) {
       item_2 = b;
    }
    
    return [item_1, item_2];
}

function testConstrainedMapValueTypeCast () returns (string) {
    map<string> testMap = {name:"kevin"};
    map<any> m = getGenericMap(testMap);
    map<string> castMap;
    castMap = < map<string> > m;
    string val = <string> castMap["name"];
    return val;
}

function testConstrainedMapValueTypeCastNegative () returns (map<int>|error) {
    map<string> testMap = {name:"kevin"};
    map<any> m = getGenericMap(testMap);
    return check trap <map<int>>m;
}

function getGenericMap (map<any> m) returns (map<any>) {
    return m;
}

function testConstrainedMapRefTypeCast () returns ([string, int]) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map<any> m = getGenericMap(testMap);
    map<Person> castMap;
    castMap = < map<Person> > m;
    Person p = <Person> castMap["item"];
    return [p.name, p.age];
}

function testConstrainedMapRefTypeCastNegative () returns (map<int>|error) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map<any> m = getGenericMap(testMap);

    return check trap <map<int>>m;
}

function testUpdateStringMap () returns (string) {
    map<string> testMap = {};
    map<any> m = updateGenericMap(testMap);
    map<string> castMap = < map<string> > m;
    string val = <string> castMap["item"];
    return val;
}

function updateGenericMap (map<any> m) returns (map<any>) {
    m["item"] = "update";
    return m;
}

function testStringMapUpdateWithInvalidTypeNegativeCase () returns (string) {
    map<string> testMap = {};
    map<any> m = updateGenericMapDifferentType(testMap);
    map<string> castMap = < map<string> > m;
    string val = <string> castMap["item"];
    return val;
}

function updateGenericMapDifferentType (map<any> m) returns (map<any>) {
    m["item"] = 1;
    return m;
}

function testStringMapUpdateWithInvalidNullTypeNegativeCase () returns (string) {
    map<string> testMap = {};
    map<any> m = updateGenericMapWithNullValue(testMap);
    map<string> castMap = < map<string> > m;
    string val = <string> castMap["item"];
    return val;
}

function updateGenericMapWithNullValue (map<any> m) returns (map<any>) {
    m["item"] = ();
    return m;
}

type Person record {|
    string name;
    int age;
    string address = "";
|};

type Employee record {
    string name;
    int age;
};

function testStructConstrainedMapRuntimeCast () returns ([string, int]) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map<any> m = getGenericMap(testMap);
    map<Employee> castMap = m is map<Employee> ? m : {};
    Employee p = <Employee> castMap["item"];
    return [p.name, p.age];
}

function testStructConstrainedMapStaticCast () returns ([string, int]) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map<Employee> castMap = testMap;
    Employee p = <Employee> castMap["item"];
    return [p.name, p.age];
}


function testStructEquivalentMapUpdate () returns ([string, int]) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map<Employee> m =  testMap;
    map<Employee> castMap = m;
    Employee p = <Employee> castMap["item"];
    return [p.name, p.age];
}

function updateEquivalentMap (map<Employee> m) returns (map<Employee>) {
    Employee b = {name:"Kevin", age:75};
    m["update"] = b;
    return m;
}

function testStructEquivalentMapAccess () returns ([string, int]) {
    map<Person> testMap = {};
    Person jack = {name:"Mervin", age:25, address:"Usa"};
    testMap["item"] = jack;
    return equivalentMapAccess(testMap);
}

function equivalentMapAccess (map<Employee> m) returns ([string, int]) {
    Employee b = <Employee> m["item"];
    return [b.name, b.age];
}

function testStructMapUpdate () returns ([string, int]) {
    map<Person> testMap = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    testMap["item"] = jack;
    map<any> m = updateStructMap(testMap);
    map<Employee> castMap = m is map<Employee> ? m : {};
    Employee p = <Employee> castMap["update"];
    return [p.name, p.age];
}

function updateStructMap (map<any> m) returns (map<any>) {
    Person k = {name:"Arnold", age:45, address:"UK"};
    m["update"] = k;
    return m;
}

function testStructNotEquivalentRuntimeCast () returns (map<Person>|error) {
    map<Employee> testMap = {};
    Employee jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    map<any> m = getGenericMap(testMap);

    return check trap <map<Person>>m;
}

function testAnyMapToValueTypeRuntimeCast () returns (map<int>|error) {
    map<any> testMap = {};
    testMap["item"] = 5;
    return check trap <map<int>>testMap;
}

function testAnyMapToRefTypeRuntimeCast () returns (map<Employee>|error) {
    map<any> testMap = {};
    Employee jack = {name:"Jack", age:25};
    testMap["item"] = jack;
    return check trap <map<Employee>>testMap;
}

type Student record {|
    int index;
    int age;
|};

function testMapToStructConversion () returns ([int, int]|error) {
    map<int> testMap = {};
    testMap["index"] = 100;
    testMap["age"] = 63;
    Student k = check testMap.cloneWithType(Student);
    return [k.index, k.age];
}

function testMapFunctionsOnConstrainedMaps () returns (string[]) {
    map<string> testMap = {};
    testMap["index"] = "100";
    testMap["age"] = "63";
    return testMap.keys();
}

function testForEachOnConstrainedMaps () returns ([string, string]) {
    map<string> testMap = {};
    testMap["name"] = "Ronnie";
    testMap["sname"] = "Cale";
    string[] arr = [];
    int index = 0;
    foreach var v in testMap {
        arr[index] = v[1];
        index = index + 1;
    }
    return [arr[0], arr[1]];
}

function testMapOfElementTypeArray () returns ([string, string]) {
    map<string[]> testMap = {};
    string[] s1 = ["name", "sname"];
    string[] s2 = ["Kollupitiya", "Ja-Ela"];
    testMap["a1"] = s1;
    testMap["a2"] = s2;
    string[] r2 = <string[]> testMap["a2"];
    return [r2[0], r2[1]];
}

function testMapOfElementTypeRefArray () returns ([string, int]) {
    map<Employee[]> testMap = {};
    Employee jack = {name:"Jack", age:25};
    Employee[] e1 = [];
    e1[0] = jack;
    testMap["e1"] = e1;
    Employee[] r2 = <Employee[]> testMap["e1"];
    Employee jackR = r2[0];
    return [jackR.name, jackR.age];
}

type PersonComplex record {|
    string name = "";
    int age = 0;
    PersonComplex? parent = ();
    json info = ();
    map<string> address = {};
    int[] marks = [];
    anydata a = ();
    float score = 0.0;
    boolean alive = false;
|};

function testJsonToStructConversionStructWithConstrainedMap () returns [string, string] {
    json j = {name:"Child",
                 age:25,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent:(),
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
                 a:"anydata value",
                 score:5.67,
                 alive:true
             };

    var result = j.cloneWithType(PersonComplex);
    if (result is PersonComplex) {
        map<string> ms = result.address;
        return [<string> ms["city"], <string> ms["country"]];
    } else {
        panic result;
    }
}

type PersonComplexTwo record {|
    string name = "";
    int age = 0;
    PersonComplexTwo? parent = ();
    json info = ();
    map<int> address = {};
    int[] marks = [];
    anydata a = ();
    float score = 0.0;
    boolean alive = false;
|};

function testJsonToStructConversionStructWithConstrainedMapNegative () returns (PersonComplexTwo|error) {
    json j = {name:"Child",
                 age:25,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent:(),
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
    return check j.cloneWithType(PersonComplexTwo);
}

function testConstrainedUnionRetrieveString () returns (string) {
    map<string> testMap = {};
    testMap["name"] = "kevin";
    return testMap.get("name");
}

function testConstrainedUnionRetrieveInt () returns (int) {
    map<int> testMap = {};
    testMap["id"] = 3;
    return testMap.get("id");
}

function testMapConstrainedEquivalentMapInsert () returns [string?, int?] {
    map<Employee> emp = {};
    Person jack = {name:"Jack", age:25, address:"Usa"};
    emp["jack"] = jack;
    return [emp["jack"]["name"], emp["jack"]["age"]];
}

type Transaction record {
    string transactionId;
    string coordinationType;
    map<Participant> participants?;
    Protocol[] coordinatorProtocols?;
};

type Participant record {|
    string participantId;
    Protocol[] participantProtocols;
|};

type Protocol record {|
    string name;
    string url;
    int transactionBlockId;
    (function (string transactionId,
               int transactionBlockId,
               string protocolAction) returns boolean)|() protocolFn;
|};

type TwoPhaseCommitTransaction record {|
    string transactionId;
    string coordinationType;
    map<Participant> participants?;
    Protocol[] coordinatorProtocols?;
    boolean possibleMixedOutcome?;
|};

function testRuntimeStructEquivalencyWithNestedConstrainedMaps () returns (string?) {
    map<Transaction> initiatedTransactions = {};
    TwoPhaseCommitTransaction tpc = {transactionId:"TR-ID", coordinationType:"2pc"};
    initiatedTransactions["Foo"] = tpc;
    return initiatedTransactions["Foo"]["transactionId"];
}

function testMapConstrainedToUnion () returns (string|int?) {
    map<string|int> testMap = {};
    testMap["test"] = "test-value";
    return testMap["test"];
}

function testMapConstrainedToUnionCaseTwo () returns (string|int?) {
    map<string|int> testMap = {};
    testMap["test"] = 2;
    return testMap["test"];
}

function testMapConstrainedToUnionCaseThree () returns (string|int?) {
    map<string|int> testMap = {};
    return testMap.get("non-existing-key");
}

function testMapConstrainedStringNonExistingKeyRetrieve () returns (string?) {
    map<string> testMap = {};
    return testMap.get("nonexist-key");
}

function testMapConstrainedToNullableUnion () returns (string?) {
    map<string?> testMap = {};
    testMap["test"] = "test-nullable-union";
    return testMap["test"];
}

function testMapConstrainedToNullableUnionNonExistingKey () returns (string?) {
    map<string?> testMap = {};
    return testMap.get("nonexist");
}

function testMapConstrainedToNullableUnionNonExistingKeyWithIndexAccess () returns (string?) {
    map<string?> testMap = {};
    return testMap["nonexist"];
}

function testInherentTypeViolationWithNilType() {
    map<string> m = { one: "one" };
    insertNilToMap(m);
}

function insertNilToMap(map<any> m) {
    m["two"] = (); // panic
}

function testMapAnyDataClosedRecordAssignment() returns (anydata) {
    Person p = {name:"Jack", age:25, address:"Usa"};
    map<anydata> m = p;
    return m["name"];
}
