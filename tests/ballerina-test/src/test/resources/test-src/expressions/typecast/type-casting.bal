function floattoint(float value) returns (int) {
    int result;
    //float to int should be a conversion
    result = <int>value;
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
    result, _ = <int>value;
    return result;
}

function testJsonIntToString() returns (string) {
    json j = 5;
    int value;
    value, _ = (int)j;
    return <string> value;
}

function stringtofloat(string value) returns (float) {
    float result;
    //string to float should be a conversion
    result, _ = <float>value;
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
    any value = {"a":"b"};
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
    value, _ = (string)j;
    return value;
}

function testJSONObjectToStringCast() returns (string | error) {
    json j = {"foo":"bar"}; 
    var value, e = (string)j;
    //TODO : Handle error
    
    return value;
}

function testJsonToInt() returns (int){
    json j = 5;
    int value;
    value, _ = (int)j;
    return value;
}

function testJsonToFloat() returns (float){
    json j = 7.65;
    float value;
    value, _ = (float)j;
    return value;
}

function testJsonToBoolean() returns (boolean){
    json j = true;
    boolean value;
    value, _ = (boolean)j;
    return value;
}

function testStringToJson(string s) returns (json) {
    return s;
}

struct Person {
    string name;
    int age;
    map address;
    int[] marks;
    Person parent;
    json info;
    any a;
    float score;
    boolean alive;
}

struct Student {
    string name;
    int age;
    map address;
    int[] marks;
}

function testStructToStruct() returns (Student) {
    Person p = { name:"Supun",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Kandy", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[24, 81]
               };
    return (Student) p;
}

function testNullStructToStruct() returns (Student) {
    Person p;
    return (Student) p;
}

function testStructAsAnyToStruct() returns (Person) {
    Person p1 = { name:"Supun",
                    age:25,
                    parent:{name:"Parent", age:50},
                    address:{"city":"Kandy", "country":"SriLanka"},
                    info:{status:"single"},
                    marks:[24, 81]
                };
    any a = p1;
    Person p2;
    p2, _ = (Person) a;
    return p2;
}

function testAnyToStruct() returns (Person) {
    any a = { name:"Supun",
                age:25,
                parent:{name:"Parent", age:50},
                address:{"city":"Kandy", "country":"SriLanka"},
                info:{status:"single"},
                marks:[24, 81]
            };
    Person p2;
    error e;
    p2, _ = (Person) a;
    return p2;
}

function testAnyNullToStruct() returns (Person) {
    any a;
    Person p;
    p, _ = (Person) a;
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
    return (any) p;
}

function testMapToAnyExplicit() returns (any) {
    map m = {name:"supun"};
    return (any) m;
}

function testBooleanInJsonToInt() returns (int) {
    json j = true;
    int value;
    value, _ = (int)j;
    return value;
}

function testIncompatibleJsonToInt() returns (int) {
    json j = "hello";
    int value;
    value, _ = (int)j;
    return value;
}

function testIntInJsonToFloat() returns (float) {
    json j = 7;
    float value;
    value, _ = (float)j;
    return value;
}

function testIncompatibleJsonToFloat() returns (float) {
    json j = "hello";
    float value;
    value, _ = (float)j;
    return value;
}

function testIncompatibleJsonToBoolean() returns (boolean) {
    json j = "hello";
    boolean value;
    value, _ = (boolean)j;
    return value;
}

struct Address {
    string city;
    string country;
}

function testNullJsonToString() returns (string) {
    json j;
    string value;
    value, _ = (string)j;
    return value;
}

function testNullJsonToInt() returns (int) {
    json j;
    int value;
    value, _ = (int)j;
    return value;
}

function testNullJsonToFloat() returns (float) {
    json j;
    float value;
    value, _ = (float)j;
    return value;
}

function testNullJsonToBoolean() returns (boolean) {
    json j;
    boolean value;
    value, _ = (boolean)j;
    return value;
}

function testAnyIntToJson() returns (json) {
    any a = 8;
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyStringToJson() returns (json) {
    any a = "Supun";
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyBooleanToJson() returns (json) {
    any a = true;
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyFloatToJson() returns (json) {
    any a = 8.73;
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyMapToJson() returns (json) {
    map m = {name:"supun"};
    any a = m;
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyStructToJson() returns (json) {
    Address adrs = {city:"CA"};
    any a = adrs;
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyNullToJson() returns (json) {
    any a = null;
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyJsonToJson() returns (json) {
    json j = {home:"SriLanka"};
    any a = j;
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyArrayToJson() returns (json) {
    any a = [8,4,6];
    json value;
    value, _ = (json) a;
    return value;
}

function testAnyNullToMap() returns (map) {
    any a;
    map value;
    value, _ = (map) a;
    return value;
}

function testAnyNullToXml() returns (xml) {
    any a;
    xml value;
    value, _ = (xml) a;
    return value;
}

struct A {
    string x;
    int y;
}

struct B {
    string x;
}

function testCompatibleStructForceCasting() returns (A | error) {
    A a = {x: "x-valueof-a", y:4};
    B b = {x: "x-valueof-b"};
    A c;

    b = (B) a;
    c, _ = (A) b;
    
    //TODO Handle error

    a.x = "updated-x-valueof-a";
    return c;
}

function testInCompatibleStructForceCasting() returns (A | error) {
    B b = {x: "x-valueof-b"};
    A a;
    a, _ = (A) b;
    
    //TODO Handle error

    return a;
}

function testAnyToStringWithErrors() returns (string | error) {
    any a = 5; 
    string s;
    s, _ = (string) a;
    //TODO Handle error
    
    return s;
}

function testAnyToStringWithoutErrors() returns (string | error) {
    any a = "value";
    string s;
    s, _ = (string) a;
    //TODO Handle error

    return s;
}

function testAnyToIntWithoutErrors() returns (int | error) {
    any a = 6;
    int s;
    s, _ = (int) a;
    //TODO Handle error

    return s;
}

function testAnyToFloatWithoutErrors() returns (float | error) {
    any a = 6.99;
    float s;
    s, _ = (float) a;
    // TODO Handle error

    return s;
}

function testAnyToBooleanWithoutErrors() returns (boolean | error) {
    any a = true;
    boolean s;
    s, _ = (boolean) a;
    //TODO Handle error

    return s;
}

function testAnyToBooleanWithErrors() returns (boolean | error) {
    any a = 5; 
    boolean b;
    b, _ = (boolean) a;
    // TODO Handle error
    
    return b;
}

function testAnyNullToBooleanWithErrors() returns (boolean | error) {
    any a = null; 
    boolean b;
    b , _ = (boolean) a;
    //TODO Handle error 
    
    return b;
}

function testAnyToIntWithErrors() returns (int | error) {
    any a = "foo"; 
    int b;
    b, _ = (int) a;
    //TODO Handle error
    
    return b;
}

function testAnyNullToIntWithErrors() returns (int | error) {
    any a = null; 
    int b;
    b, _ = (int) a;
    //TODO Handle error
    
    return b;
}

function testAnyToFloatWithErrors() returns (float | error) {
    any a = "foo"; 
    float b;
    b, _ = (float) a;
    //TODO Handle error
    
    return b;
}

function testAnyNullToFloatWithErrors() returns (float | error) {
    any a = null; 
    float b;
    b, _ = (float) a;
    //TODO Handle error
    
    return b;
}

function testAnyToMapWithErrors() returns (map | error) {
    any a = "foo"; 
    map b;
    b, _ = (map) a;
    //TODO Handle error
    
    return b;
}

function testAnyNullToStringWithErrors() returns (string | error) {
    any a = null;
    string s;
    s, _ = (string) a;
    //TODO Handle error

    return s;
}

function testSameTypeCast() returns (int) {
    int a = 10;

    int b = (int) a;
    return b;
}

function testErrorOnCasting() returns (error, error, error, error) {

    // json to string
    json j1 = "hello";
    var s, err1 = (string) j1;

    // json to int
    json j2 = 4;
    var i, err2 = (int) j2; 

    // json to float
    json j3 = 4.2;
    var f, err3 = (float) j3; 

    // json to boolean
    json j4 = true;
    var b, err4 = (boolean) j4;

    return (err1, err2, err3, err4);
}

function testAnyToTable() returns (table, any) {
    table < Employee> tb = {};

    Employee e1 = {id:1, name:"Jane"};
    Employee e2 = {id:2, name:"Anne"};
    tb.add(e1);
    tb.add(e2);

    any anyValue = tb;
    table casted;
    error err;
    casted, err = (table) anyValue;
    return (casted, err);
}

function testAnyToTableWithErrors() returns (table | error) {
    any stringValue = "SomeString";
    table casted;
    casted =? (table) stringValue;
   //TODO Handle error
    return casted;
}

struct Employee {
    int id;
    string name;
}
