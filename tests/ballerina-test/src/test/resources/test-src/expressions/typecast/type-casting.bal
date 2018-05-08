
function floattoint(float value) returns (int) {
    int result;
    //float to int should be a conversion
    result = <int>value;
    return result;
}

function floattointWithError(float value) returns (int | error) {
    int | error result = <int>value;
    return result;
}

function inttofloat(int value) returns (float) {
    float result;
    //int to float should be a conversion
    result = <float> value;
    return result;
}

function stringtoint(string value) returns (int) {
    int result;
    //string to int should be a unsafe conversion
    result = check <int>value;
    return result;
}

function testJsonIntToString() returns (string) {
    json j = 5;
    int value;
    value = check <int>j;
    return <string> value;
}

function stringtofloat(string value) returns (float) {
    float result;
    //string to float should be a conversion
    result = check <float>value;
    return result;
}

function inttostring(int value) returns (string) {
    string result;
    //int to string should be a conversion
    result = <string>value;
    return result;
}

function floattostring(float value) returns (string) {
    string result;
    //float to string should be a conversion
    result = <string>value;
    return result;
}

function booleantostring(boolean value) returns (string) {
    string result;
    //boolean to string should be a conversion
    result = <string>value;
    return result;
}

function booleanappendtostring(boolean value) returns (string) {
    string result;
    result = value + "-append-" + value;
    return result;
}

function anyfloattostring() returns (string) {
    any value = 5.5;
    string result;
    //any to string should be a conversion
    result = <string>value;
    return result;
}

function anyjsontostring() returns (string) {
    json j = {"a":"b"};
    any value = j;
    string result;
    //any to string should be a conversion
    result = <string>value;
    return result;
}

function intarrtofloatarr() returns (float[]) {
    float[] numbers;
    numbers = [999,95,889];
    return numbers;
}

function testJsonToStringCast() returns (string) {
    json j = "hello";
    string value;
    value = check <string>j;
    return value;
}

function testJSONObjectToStringCast() returns (string | error) {
    json j = {"foo":"bar"};
    var value = <string>j;
    //TODO : Handle error

    return value;
}

function testJsonToInt() returns (int){
    json j = 5;
    int value;
    value = check <int>j;
    return value;
}

function testJsonToFloat() returns (float){
    json j = 7.65;
    float value;
    value = check <float>j;
    return value;
}

function testJsonToBoolean() returns (boolean){
    json j = true;
    boolean value;
    value = check <boolean>j;
    return value;
}

function testStringToJson(string s) returns (json) {
    return s;
}

type Person {
    string name;
    int age;
    map address;
    int[] marks;
    Person | () parent;
    json info;
    any a;
    float score;
    boolean alive;
};

type Student {
    string name;
    int age;
    map address;
    int[] marks;
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
//    Person | null p;
//    return <Student> p;
//}

function testStructAsAnyToStruct() returns (Person) {
    Person p1 = { name:"Supun",
                    age:25,
                    parent:{name:"Parent", age:50},
                    address:{"city":"Kandy", "country":"SriLanka"},
                    info:{status:"single"},
                    marks:[24, 81]
                };
    any a = p1;
    var p2 = check <Person> a;
    return p2;
}

function testAnyToStruct() returns (Person) {
    json address = {"city":"Kandy", "country":"SriLanka"};
    map parent = {name:"Parent", age:50};
    map info = {status:"single"};
    int[] marks = [24, 81];
    map a = { name:"Supun",
                age:25,
                parent:parent,
                address:address,
                info:info,
                marks:marks
            };
    any b = a;
    var p2 = check <Person> b;
    return p2;
}

function testAnyNullToStruct() returns (Person) {
    any a;
    var p = check <Person> a;
    return p;
}

function testStructToAnyExplicit() returns (any) {
    Person p = { name:"Supun",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Kandy", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[24, 81]
               };
    return <any> p;
}

function testMapToAnyExplicit() returns (any) {
    map m = {name:"supun"};
    return <any> m;
}

function testBooleanInJsonToInt() returns (int) {
    json j = true;
    int value;
    value = check <int>j;
    return value;
}

function testIncompatibleJsonToInt() returns (int) {
    json j = "hello";
    int value;
    value = check <int>j;
    return value;
}

function testIntInJsonToFloat() returns (float) {
    json j = 7;
    float value;
    value = check <float>j;
    return value;
}

function testIncompatibleJsonToFloat() returns (float) {
    json j = "hello";
    float value;
    value = check <float>j;
    return value;
}

function testIncompatibleJsonToBoolean() returns (boolean) {
    json j = "hello";
    boolean value;
    value = check <boolean>j;
    return value;
}

type Address {
    string city;
    string country;
};

function testNullJsonToString() returns (string) {
    json j;
    string value;
    value = check <string>j;
    return value;
}

function testNullJsonToInt() returns (int) {
    json j;
    int value;
    value = check <int>j;
    return value;
}




function testNullJsonToFloat() returns (float) {
    json j;
    float value;
    value = check <float>j;
    return value;
}

function testNullJsonToBoolean() returns (boolean) {
    json j;
    boolean value;
    value = check <boolean>j;
    return value;
}

function testAnyIntToJson() returns (json) {
    any a = 8;
    json value;
    value = check <json> a;
    return value;
}

function testAnyStringToJson() returns (json) {
    any a = "Supun";
    json value;
    value = check <json> a;
    return value;
}

function testAnyBooleanToJson() returns (json) {
    any a = true;
    json value;
    value = check <json> a;
    return value;
}

function testAnyFloatToJson() returns (json) {
    any a = 8.73;
    json value;
    value = check <json> a;
    return value;
}

function testAnyMapToJson() returns (json) {
    map m = {name:"supun"};
    any a = m;
    json value;
    value = check <json> a;
    return value;
}

function testAnyStructToJson() returns (json) {
    Address adrs = {city:"CA"};
    any a = adrs;
    json value;
    value = check <json> a;
    return value;
}

function testAnyNullToJson() returns (json) {
    any a = null;
    json value;
    value = check <json> a;
    return value;
}

function testAnyJsonToJson() returns (json) {
    json j = {home:"SriLanka"};
    any a = j;
    json value;
    value = check <json> a;
    return value;
}

function testAnyArrayToJson() returns (json) {
    any[] a = [8,4,6];
    json value;
    value = check <json> a;
    return value;
}

function testAnyNullToMap() returns (map) {
    any a;
    map value;
    value = check <map> a;
    return value;
}

function testAnyNullToXml() returns (xml) {
    any a;
    xml value;
    value = check <xml> a;
    return value;
}

type A {
    string x;
    int y;
};

type B {
    string x;
};

function testCompatibleStructForceCasting() returns (A | error) {
    A a = {x: "x-valueof-a", y:4};
    B b = {x: "x-valueof-b"};

    b = <B> a;
    A c = check <A> b;

    //TODO Handle error

    a.x = "updated-x-valueof-a";
    return c;
}

function testInCompatibleStructForceCasting() returns (A | error) {
    B b = {x: "x-valueof-b"};
    A a = check <A> b;

    //TODO Handle error

    return a;
}

function testAnyToIntWithoutErrors() returns (int | error) {
    any a = 6;
    int s;
    s = check <int> a;
    //TODO Handle error

    return s;
}

function testAnyToFloatWithoutErrors() returns (float | error) {
    any a = 6.99;
    float s;
    s = check <float> a;
    // TODO Handle error

    return s;
}

function testAnyToBooleanWithoutErrors() returns (boolean | error) {
    any a = true;
    boolean s;
    s = check <boolean> a;
    //TODO Handle error

    return s;
}

function testAnyToBooleanWithErrors() returns (boolean | error) {
    any a = 5;
    boolean b;
    b = check <boolean> a;
    // TODO Handle error

    return b;
}

function testAnyNullToBooleanWithErrors() returns (boolean | error) {
    any a = null;
    boolean b;
    b = check <boolean> a;
    //TODO Handle error

    return b;
}

function testAnyToIntWithErrors() returns (int | error) {
    any a = "foo";
    int b;
    b = check <int> a;
    //TODO Handle error

    return b;
}

function testAnyNullToIntWithErrors() returns (int | error) {
    any a = null;
    int b;
    b = check <int> a;
    //TODO Handle error

    return b;
}

function testAnyToFloatWithErrors() returns (float | error) {
    any a = "foo";
    float b;
    b = check <float> a;
    //TODO Handle error

    return b;
}

function testAnyNullToFloatWithErrors() returns (float | error) {
    any a = null;
    float b;
    b = check <float> a;
    //TODO Handle error

    return b;
}

function testAnyToMapWithErrors() returns (map | error) {
    any a = "foo";
    map b;
    b = check <map> a;
    //TODO Handle error

    return b;
}

function testAnyNullToString() returns (string) {
    any a = null;
    string s;
    s = <string> a;
    return s;
}

function testSameTypeCast() returns (int) {
    int a = 10;

    int b = <int> a;
    return b;
}

function testJSONValueCasting() returns (string | error, int | error, float | error, boolean | error) {

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

    return (s, i, f, b);
}

function testAnyToTable() returns (table | error) {
    table < Employee> tb = table{};

    Employee e1 = {id:1, name:"Jane"};
    Employee e2 = {id:2, name:"Anne"};
    _ = tb.add(e1);
    _ = tb.add(e2);

    any anyValue = tb;
    var casted = <table> anyValue;
    return casted;
}

function testAnyToTableWithErrors() returns (table | error) {
    any stringValue = "SomeString";
    table casted;
    casted = check <table> stringValue;
   //TODO Handle error
    return casted;
}

type Employee {
    int id;
    string name;
};






 
