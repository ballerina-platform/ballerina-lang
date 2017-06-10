import ballerina.lang.errors;

function floattoint(float value)(int) {
    int result;
    result = (int)value;
    return result;
}
 
function inttofloat(int value)(float) {
    float result;
    result = value;
    return result;
}

function stringtoint(string value)(int) {
    int result;
    result = (int)value;
    return result;
}

function stringtofloat(string value)(float) {
    float result;
    result = (float)value;
    return result;
}

function inttostring(int value)(string) {
    string result;
    result = (string)value;
    return result;
}

function floattostring(float value)(string) {
    string result;
    result = (string)value;
    return result;
}

function booleantostring(boolean value)(string) {
    string result;
    result = (string)value;
    return result;
}

function booleanappendtostring(boolean value)(string) {
    string result;
    result = value + "-append-" + value;
    return result;
}

function intarrtofloatarr()(float[]) {
    float[] numbers;
    numbers = [999,95,889];
    return numbers;
}

function testJsonToString(json j) (string) {
    return (string)j;
}

function testJsonToInt() (int){
    json j = 5;
    return (int)j;
}

function testJsonToFloat() (float){
    json j = 7.65;
    return (float)j;
}

function testJsonToBoolean() (boolean){
    json j = true;
    return (boolean)j;
}

function testStringToJson(string s) (json) {
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

function testStructToStruct() (Student) {
    Person p = { name:"Supun", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Kandy", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[24, 81]
               };
    return (Student) p;
}

function testJsonIntToString() (string) {
    json j = 5;
    return (int)j; 
}

function testBooleanInJsonToInt() (int) {
    json j = true;
    return (int)j;
}

function testIncompatibleJsonToInt() (int) {
    json j = "hello";
    return (int)j;
}

function testIntInJsonToFloat() (float) {
    json j = 7;
    return (float)j;
}

function testIncompatibleJsonToFloat() (float) {
    json j = "hello";
    return (float)j;
}

function testIncompatibleJsonToBoolean() (boolean) {
    json j = "hello";
    return (boolean)j;
}

struct Address {
    string city;
    string country;
}

function testNullJsonToString() (string) {
    json j;
    return (string)j;
}

function testNullJsonToInt() (int) {
    json j;
    return (int)j;
}

function testNullJsonToFloat() (float) {
    json j;
    return (float)j;
}

function testNullJsonToBoolean() (boolean) {
    json j;
    return (boolean)j;
}

function testNullStructToStruct() (Student) {
    Person p;
    return (Student) p;
}

function testAnyIntToJson() (json) {
    any a = 8;
    return (json) a;
}

function testAnyStringToJson() (json) {
    any a = "Supun";
    return (json) a;
}

function testAnyBooleanToJson() (json) {
    any a = true;
    return (json) a;
}

function testAnyFloatToJson() (json) {
    any a = 8.73;
    return (json) a;
}

function testAnyMapToJson() (json) {
    map m = {name:"supun"};
    any a = m;
    return (json) a;
}

function testAnyStructToJson() (json) {
    Address adrs = {city:"CA"};
    any a = adrs;
    return (json) a;
}

function testAnyNullToJson() (json) {
    any a = null;
    return (json) a;
}

function testAnyJsonToJson() (json) {
    json j = {home:"SriLanka"};
    any a = j;
    return (json) a;
}

function testAnyArrayToJson() (json) {
    any a = [8,4,6];
    return (json) a;
}

function testAnyMessageToJson() (json) {
    message m = {};
    any a = m;
    return (json) a;
}

function testStructAsAnyToStruct() (Person) {
    Person p1 = { name:"Supun", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Kandy", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[24, 81]
               };
    any a = p1;
    Person p2 = (Person) a;
    return p2;
}

function testAnyToStruct() (Person) {
    any a = { name:"Supun", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Kandy", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[24, 81]
               };
    Person p2 = (Person) a;
    return p2;
}

function testAnyNullToStruct() (Person) {
    any a;
    return (Person) a;
}

function testAnyNullToMap() (map) {
    any a;
    return (map) a;
}

function testAnyNullToXml() (xml) {
    any a;
    return (xml) a;
}

function testMapToAny() (any) {
    map m = {};
    return (any) m;
}

function testStructToAnyExplicit() (any) {
    Person p = { name:"Supun", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Kandy", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[24, 81]
               };
    return (any) p;
}

function testMapToAnyExplicit() (any) {
    map m = {name:"supun"};
    return (any) m;
}

struct A {
    string x;
    int y;
}

struct B {
    string x;
}

function testCompatibleStructForceCasting()(A, errors:TypeCastError) {
    A a = {x: "x-valueof-a", y:4};
    B b = {x: "x-valueof-b"};
    A c;
    
    b = (B) a;
    errors:TypeCastError err;
    c, err = (A) b;
    
    a.x = "updated-x-valueof-a";
    return c, err;
}

function testInCompatibleStructForceCasting()(A, errors:TypeCastError) {
    B b = {x: "x-valueof-b"};
    A a;
    errors:TypeCastError err;
    a, err = (A) b;
    
    return a, err;
}

function testAnyToStringWithErrors()(string, errors:TypeCastError) {
    any a = 5; 
    string s;
    errors:TypeCastError err;
    s, err = (string) a;
    
    return s, err;
}

function testAnyNullToStringWithErrors()(string, errors:TypeCastError) {
    any a = null; 
    string s;
    errors:TypeCastError err;
    s, err = (string) a;
    
    return s, err;
}

function testAnyToBooleanWithErrors()(boolean, errors:TypeCastError) {
    any a = 5; 
    boolean b;
    errors:TypeCastError err;
    b, err = (boolean) a;
    
    return b, err;
}

function testAnyNullToBooleanWithErrors()(boolean, errors:TypeCastError) {
    any a = null; 
    boolean b;
    errors:TypeCastError err;
    b, err = (boolean) a;
    
    return b, err;
}

function testAnyToIntWithErrors()(int, errors:TypeCastError) {
    any a = "foo"; 
    int b;
    errors:TypeCastError err;
    b, err = (int) a;
    
    return b, err;
}

function testAnyNullToIntWithErrors()(int, errors:TypeCastError) {
    any a = null; 
    int b;
    errors:TypeCastError err;
    b, err = (int) a;
    
    return b, err;
}

function testAnyToFloatWithErrors()(float, errors:TypeCastError) {
    any a = "foo"; 
    float b;
    errors:TypeCastError err;
    b, err = (float) a;
    
    return b, err;
}

function testAnyNullToFloatWithErrors()(float, errors:TypeCastError) {
    any a = null; 
    float b;
    errors:TypeCastError err;
    b, err = (float) a;
    
    return b, err;
}

function testAnyToMapWithErrors()(map, errors:TypeCastError) {
    any a = "foo"; 
    map b;
    errors:TypeCastError err;
    b, err = (map) a;
    
    return b, err;
}

function testErrorInForceCasting()(A, errors:Error) {
    B b = {x: "x-valueof-b"};
    A a;
    errors:TypeCastError castError;
    a, castError = (A) b;
    
    errors:Error error;
    if (castError != null) {
        error = (errors:Error) castError;
    }
    
    return a, error;
}
