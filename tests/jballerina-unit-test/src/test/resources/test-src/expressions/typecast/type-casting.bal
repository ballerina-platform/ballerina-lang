import ballerina/lang.'float as floats;
import ballerina/lang.'int as ints;

function floattoint(float value) returns (int) {
    int result;
    //float to int should be a conversion
    result = <int>value;
    return result;
}

function floattointWithError(float value) returns int|error {
    int | error result = <int>value;
    return result;
}

function inttofloat(int value) returns (float) {
    float result;
    //int to float should be a conversion
    result = <float> value;
    return result;
}

function stringtoint(string value) returns int|error {
    int result;
    //string to int should be a unsafe conversion
    result = check ints:fromString(value);
    return result;
}

function testJsonIntToString() returns string|error {
    json j = 5;
    int value;
    value = check trap <int>j;
    return value.toString();
}

function stringtofloat(string value) returns float|error {
    float result;
    //string to float should be a conversion
    result = check floats:fromString(value);
    return result;
}

function inttostring(int value) returns (string) {
    string result;
    //int to string should be a conversion
    result = value.toString();
    return result;
}

function floattostring(float value) returns (string) {
    string result;
    //float to string should be a conversion
    result = value.toString();
    return result;
}

function booleantostring(boolean value) returns (string) {
    string result;
    //boolean to string should be a conversion
    result = value.toString();
    return result;
}

function booleanappendtostring(boolean value) returns (string) {
    string result;
    result = value.toString() + "-append-" + value.toString();
    return result;
}

function anyjsontostring() returns (string) {
    json j = {"a":"b"};
    // TODO: calling toJsonString on any is not allowed
    //any value = j;
    string result;
    result = j.toJsonString();
    return result;
}

function testJsonToStringCast() returns string|error {
    json j = "hello";
    string value;
    value = check trap <string> j;
    return value;
}

function testJSONObjectToStringCast() returns string|error {
    json j = {"foo":"bar"};
    var value = j.toJsonString();
    //TODO : Handle error

    return value;
}

function testJsonToInt() returns int|error {
    json j = 5;
    int value;
    value = check trap <int>j;
    return value;
}

function testJsonToFloat() returns float|error {
    json j = 7.65;
    float value;
    value = check trap <float>j;
    return value;
}

function testJsonToBoolean() returns boolean|error {
    json j = true;
    boolean value;
    value = check trap <boolean>j;
    return value;
}

function testStringToJson(string s) returns (json) {
    return s;
}

type Person record {
    string name;
    int age;
    map<anydata> address = {};
    int[] marks = [];
    Person | () parent = ();
    json info = {};
    anydata a = 0;
    float score = 0.0;
    boolean alive = true;
};

type Student record {
    string name;
    int age;
    map<any> address = {};
    int[] marks = [];
};

function testStructToStruct() returns (Student) {
    Person p = { name:"Supun",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Kandy", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[24, 81]
               };
    var p2 = p;
    return p2;
}

//function testNullStructToStruct() returns (Student) {
//    Person | () p;
//    return <Student> p;
//}

function testStructAsAnyToStruct() returns Person|error {
    Person p1 = { name:"Supun",
                    age:25,
                    parent:{name:"Parent", age:50},
                    address:{"city":"Kandy", "country":"SriLanka"},
                    info:{status:"single"},
                    marks:[24, 81]
                };
    any a = p1;
    var p2 = check trap <Person> a;
    return p2;
}

function testAnyToStruct() returns Person {
    json address = {"city":"Kandy", "country":"SriLanka"};
    map<any> parent = {name:"Parent", age:50};
    map<any> info = {status:"single"};
    int[] marks = [24, 81];
    map<any> a = { name:"Supun",
                age:25,
                parent:parent,
                address:address,
                info:info,
                marks:marks
            };
    any b = a;
    var p2 = <Person> b;
    return p2;
}

function testAnyNullToStruct() returns Person {
    any a = ();
    var p = <Person> a;
    return p;
}

function testRecordToAny() returns (any) {
    Person p = { name:"Supun",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Kandy", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[24, 81]
               };
    return p;
}

function testMapToAny() returns (any) {
    map<any> m = {name:"supun"};
    return m;
}

function testIncompatibleJsonToInt() returns int|error {
    json j = "hello";
    int value;
    value = check ints:fromString(j.toJsonString());
    return value;
}

function testIntInJsonToFloat() returns float|error {
    json j = 7;
    float value;
    value = check floats:fromString(j.toJsonString());
    return value;
}

function testIncompatibleJsonToFloat() returns float|error {
    json j = "hello";
    float value;
    value = check floats:fromString(j.toJsonString());
    return value;
}

type Address record {
    string city;
    string country = "";
};

function testNullJsonToString() returns string {
    json j = {};
    string value;
    value = <string>j;
    return value;
}

function testNullJsonToInt() returns int {
    json j = {};
    int value;
    value = <int>j;
    return value;
}

function testNullJsonToFloat() returns float {
    json j = {};
    float value;
    value = <float>j;
    return value;
}

function testNullJsonToBoolean() returns boolean {
    json j = {};
    boolean value;
    value = <boolean> j;
    return value;
}

function testAnyIntToJson() returns json|error {
    any a = 8;
    json value;
    value = <json> a;
    return value;
}

function testAnyStringToJson() returns json {
    any a = "Supun";
    json value;
    value = <json> a;
    return value;
}

function testAnyBooleanToJson() returns json {
    any a = true;
    json value;
    value = <json> a;
    return value;
}

function testAnyFloatToJson() returns json {
    any a = 8.73;
    json value;
    value = <json> a;
    return value;
}

function testAnyMapToJson() returns json {
    map<any> m = {name:"supun"};
    any a = m;
    json value;
    value = <json> a;
    return value;
}

function testAnyStructToJson() returns json {
    Address adrs = {city:"CA"};
    any a = adrs;
    json value;
    value = <json> a;
    return value;
}

type JsonTypedesc typedesc<json>;

function testAnyNullToJson() returns json|error {
    anydata a = ();
    json value;
    value = check a.cloneWithType(JsonTypedesc);
    return value;
}

function testAnyJsonToJson() returns json|error {
    json j = {home:"SriLanka"};
    any a = j;
    json value;
    value = check trap <json> a;
    return value;
}

function testAnyNullToMap() returns map<any> {
    any a = ();
    map<any> value;
    value = <map<any>> a;
    return value;
}

function testAnyNullToXml() returns xml {
    any a = ();
    xml value;
    value = <xml> a;
    return value;
}

type A record {
    string x;
    int y;
};

type B record {
    string x;
};

function testCompatibleStructForceCasting() returns A|error {
    A a = {x: "x-valueof-a", y:4};
    B b = {x: "x-valueof-b"};

    b = a;
    A c = check trap <A> b;

    //TODO Handle error

    a.x = "updated-x-valueof-a";
    return c;
}

type ATypedesc typedesc<A>;

function testInCompatibleStructForceCasting() returns A|error {
    B b = {x: "x-valueof-b"};
    A a = check b.cloneWithType(ATypedesc);

    //TODO Handle error

    return a;
}

function testAnyToIntWithoutErrors() returns int|error {
    any a = 6;
    int s;
    s = check trap <int> a;
    //TODO Handle error

    return s;
}

function testAnyToFloatWithoutErrors() returns float|error {
    any a = 6.99;
    float s;
    s = check trap <float> a;
    // TODO Handle error

    return s;
}

function testAnyToBooleanWithoutErrors() returns boolean|error {
    any a = true;
    boolean s;
    s = check trap <boolean> a;
    //TODO Handle error

    return s;
}

function testAnyToBooleanWithErrors() returns boolean|error {
    map<any> m = { one: "one" };
    any a = m;
    boolean b;
    b = check trap <boolean> a;
    // TODO Handle error

    return b;
}

function testAnyNullToBooleanWithErrors() returns boolean|error {
    any a = ();
    boolean b;
    b = check trap <boolean> a;
    //TODO Handle error

    return b;
}

function testAnyToIntWithErrors() returns int|error {
    any a = "foo";
    int b;
    b = check trap <int> a;
    //TODO Handle error

    return b;
}

function testAnyNullToIntWithErrors() returns int|error {
    any a = ();
    int b;
    b = check trap <int> a;
    //TODO Handle error

    return b;
}

function testAnyToFloatWithErrors() returns float|error {
    any a = "foo";
    float b;
    b = check trap <float> a;
    //TODO Handle error

    return b;
}

function testAnyNullToFloatWithErrors() returns float|error {
    any a = ();
    float b;
    b = check trap <float> a;
    //TODO Handle error

    return b;
}

function testAnyToMapWithErrors() returns (map<any> | error) {
    any a = "foo";
    map<any> b;
    b = check trap <map<any>> a;
    //TODO Handle error

    return b;
}

function testAnyNullToString() returns string {
    any a = ();
    string s;
    s = a.toString();
    return s;
}

function testSameTypeCast() returns int {
    int a = 10;

    int b = <int> a;
    return b;
}

function testJSONValueCasting() returns [string|error, int|error, float|error, boolean|error] {

    // json to string
    json j1 = "hello";
    var s = <string> j1;

    // json to int
    json j2 = 4;
    var i = <int> j2;

    // json to float
    json j3 = 4.2;
    var f = <float> j3;

    // json to boolean
    json j4 = true;
    var b = <boolean> j4;

    return [s, i, f, b];
}

function testAnyToTable(){
    table<Employee> tb = table [
                    {id:1, name:"Jane"},
                    {id:2, name:"Anne"}
        ];

    any anyValue = tb;
    var casted = <table<Employee>> anyValue;
    table<Employee>|error  castedValue = casted;
    assertEquality("[{\"id\":1,\"name\":\"Jane\"},{\"id\":2,\"name\":\"Anne\"}]", castedValue.toString());
}

type Employee record {
    int id;
    string name;
};


function testAnonRecordInCast() returns record {| string name; |} {
    return <record {| string name; |}>{ name: "Pubudu" };
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
