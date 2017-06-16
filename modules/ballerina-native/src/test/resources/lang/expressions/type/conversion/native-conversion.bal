import ballerina.lang.errors;

struct Person {
    string name;
    int age;
    Person parent;
    json info;
    map address;
    int[] marks;
    any a;
    float score;
    boolean alive;
}

struct Student {
    string name;
    int age;
}

function testStructToMap() (map) {
    Person p = { name:"Child", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Colombo", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[67,38,91]
               };
    errors:TypeConversionError err;
    map m;
    m, err = <map> p;
    return m;
}

function testMapToStruct() (Person) {
    int[] marks = [87,94,72];
    Person parent = {
                    name:"Parent", 
                    age:50,
                    parent: null,
                    address:null,
                    info:null,
                    marks:null,
                    a:null,
                    score: 4.57,
                    alive:false
               };

    json info = {status:"single"};
    map m = { name:"Child", 
               age:25, 
               parent:parent, 
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:info,
               marks:marks,
               a:"any value",
               score: 5.67,
               alive:true
             };
    return <Person> m;
}

function testStructToJson() (json) {
    Person p = { name:"Child", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Colombo", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[87,94,72]
               };
    json j = <json> p;
    return j;
}

function testJsonToStruct() (Person) {
    json j = { name:"Child", 
               age:25, 
               parent:{
                    name:"Parent", 
                    age:50,
                    parent: null,
                    address:null,
                    info:null,
                    marks:null,
                    a:null,
                    score: 4.57,
                    alive:false
               }, 
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:[56,79],
               a:"any value",
               score: 5.67,
               alive:true
             };
    Person p = <Person> j;
    return p;
}

function testIncompatibleMapToStruct() (Person) {
    int[] marks = [87,94,72];
    map m = { name:"Child", 
               age:25, 
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:marks
             };
    Person p = <Person> m;
    return p;
}

function testMapWithIncompatibleArrayToStruct() (Person) {
    float[] marks = [87,94,72];
    Person parent = {
                    name:"Parent", 
                    age:50,
                    parent: null,
                    address:null,
                    info:null,
                    marks:null,
                    a:null,
                    score: 5.67,
                    alive:false
               };
    json info = {status:"single"};
    map m = { name:"Child", 
               age:25,
               parent:parent,
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:info,
               marks:marks,
               a:"any value",
               score: 5.67,
               alive:true
             };
    Person p = <Person> m;
    return p;
}

struct Employee {
    string name;
    int age;
    Person partner;
    json info;
    map address;
    int[] marks;
}

function testMapWithIncompatibleStructToStruct() (Employee) {
    int[] marks = [87,94,72];
    Student s = { name:"Supun", 
                  age:25
                };

    map m = { name:"Child", 
               age:25,
               partner: s,
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:marks
             };
    Employee e = <Employee> m;
    return e;
}

function testIncompatibleJsonToStruct() (Person) {
    json j = { name:"Child", 
               age:25, 
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:[87,94,72]
             };
    Person p = <Person> j; 
    return p;
}

function testJsonWithIncompatibleMapToStruct() (Person) {
    json j = { name:"Child", 
               age:25,
               parent:{
                    name:"Parent", 
                    age:50,
                    parent: null,
                    address:"SriLanka",
                    info:null,
                    marks:null
               },
               address:{"city":"Colombo", "country":"SriLanka"},
               info:{status:"single"},
               marks:[87,94,72]
             };
    Person p = <Person> j; 
    return p;
}

function testJsonWithIncompatibleTypeToStruct() (Person) {
    json j = { name:"Child", 
               age:25.8,
               parent:{
                    name:"Parent", 
                    age:50,
                    parent: null,
                    address:"SriLanka",
                    info:null,
                    marks:null
               },
               address:{"city":"Colombo", "country":"SriLanka"},
               info:{status:"single"},
               marks:[87,94,72]
             };
    Person p = <Person> j; 
    return p;
}

function testJsonWithIncompatibleStructToStruct() (Person) {
    json j = { name:"Child", 
               age:25,
               parent:{
                    name:"Parent", 
                    age:50,
                    parent: "Parent",
                    address:{"city":"Colombo", "country":"SriLanka"},
                    info:null,
                    marks:null
               },
               address:{"city":"Colombo", "country":"SriLanka"},
               info:{status:"single"},
               marks:[87,94,72]
             };
    Person p = <Person> j; 
    return p;
}

function testJsonArrayToStruct() (Person) {
    json j = [87,94,72];
    Person p = <Person> j; 
    return p;
}

struct Info {
    message msg;
}

function testStructWithMessageToJson() (json) {
    message m = {};
    Info info = {msg: m};
    json j = <json> info;
    return j; 
}

function testJsonIntToString() (string) {
    json j = 5;
    return <int>j; 
}

function testBooleanInJsonToInt() (int) {
    json j = true;
    return <int>j;
}

function testIncompatibleJsonToInt() (int) {
    json j = "hello";
    return <int>j;
}

function testIntInJsonToFloat() (float) {
    json j = 7;
    return <float>j;
}

function testIncompatibleJsonToFloat() (float) {
    json j = "hello";
    return <float>j;
}

function testIncompatibleJsonToBoolean() (boolean) {
    json j = "hello";
    return <boolean>j;
}

struct Address {
    string city;
    string country;
}

struct AnyArray {
    any[] a;
}

function testJsonToAnyArray() (AnyArray) {
    json j = {a:[4, "Supun", 5.36, true, {lname:"Setunga"}, [4,3,7], null]};
    AnyArray a = <AnyArray> j;
    return a;
}

struct IntArray {
    int[] a;
}

function testJsonToIntArray() (IntArray) {
    json j = {a:[4,3,9]};
    IntArray a = <IntArray> j;
    return a;
}

struct StringArray {
    string[] a;
}

function testJsonToStringArray() (StringArray) {
    json j = {a:["a","b","c"]};
    StringArray a = <StringArray> j;
    return a;
}

function testJsonIntArrayToStringArray() (StringArray) {
    json j = {a:[4,3,9]};
    StringArray a = <StringArray> j;
    return a;
}

struct XmlArray {
    xml[] a;
}

function testJsonToXmlArray() (XmlArray) {
    json j = {a:["a","b","c"]};
    XmlArray a = <XmlArray> j;
    return a;
}

function testNullJsonArrayToArray() (StringArray) {
    json j = {a:null};
    StringArray a = <StringArray> j;
    return a;
}

function testNullJsonToArray() (StringArray) {
    json j;
    StringArray a = <StringArray> j;
    return a;
}

function testNonArrayJsonToArray() (StringArray) {
    json j = {a:"im not an array"};
    StringArray a = <StringArray> j;
    return a;
}

function testNullJsonToString() (string) {
    json j;
    return <string>j;
}

function testNullJsonToInt() (int) {
    json j;
    return <int>j;
}

function testNullJsonToFloat() (float) {
    json j;
    return <float>j;
}

function testNullJsonToBoolean() (boolean) {
    json j;
    return <boolean>j;
}

function testNullJsonToStruct() (Person) {
    json j;
    Person p = <Person> j;
    return p;
}

function testNullMapToStruct() (Person) {
    map m;
    Person p = <Person> m;
    return p;
}

function testNullStructToJson() (json) {
    Person p;
    json j = <json> p;
    return j;
}

function testNullStructToMap() (map) {
    Person p;
    map m = <map> p;
    return m;
}

function testIncompatibleJsonToStructWithErrors() (Person, errors:TypeConversionError) {
    json j = { name:"Child", 
               age:25,
               parent:{
                    name:"Parent", 
                    age:50,
                    parent: "Parent",
                    address:{"city":"Colombo", "country":"SriLanka"},
                    info:null,
                    marks:null
               },
               address:{"city":"Colombo", "country":"SriLanka"},
               info:{status:"single"},
               marks:[87,94,72]
             };
    errors:TypeConversionError err;
    Person p;
    p, err = <Person> j; 
    return p, err;
}

struct PersonA {
    string name;
    int age;
}

function JsonToStructWithErrors() (PersonA, errors:TypeConversionError) {
    errors:TypeConversionError err;
    PersonA person;
    json j = {name:"supun"};
    
    person, err = <PersonA> j; 
    
    return person, err;
}