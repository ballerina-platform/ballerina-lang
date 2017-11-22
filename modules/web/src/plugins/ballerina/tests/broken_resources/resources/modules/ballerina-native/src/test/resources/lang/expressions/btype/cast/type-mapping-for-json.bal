function testSimpleJsonToMap() (map) {
    json j = {fname:"Supun", "lname":"Setunga"};
    return j;
}

function testComplexJsonToMap() (map) {
    json j = {name:"Supun", 
              age:25,
              gpa:2.81,
              status:true,
              info:null, 
              address:{city:"Colombo", "country":"SriLanka"}, 
              marks:[1,5,7]};
    return j;
}

function testSimpleMapToJson() (json) {
    map m = {fname:"Supun", "lname":"Setunga"};
    return m;
}
    
function testComplexMapToJson() (json) {
    Person p = {name:"Supun"};
    json j = {title:"SE"};
    map m = { name:"Supun", 
              age:25,
              gpa:2.81,
              status:true,
              info:null, 
              address:{city:"CA", "country":"USA"},
              intArray:[7,8,9],
              addressArray:[
                    {address:{city:"Colombo", "country":"SriLanka"}},
                    {address:{city:"Kandy", "country":"SriLanka"}},
                    {address:{city:"Galle", "country":"SriLanka"}}
              ],
              parent:p,
              occupation:j
            };
    return m;
}

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
    map address;
    int[] marks;
}

function testStructToMap() (map) {
    Person p = { name:"Child", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Colombo", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[67,38,91]
               };
    return p;
}

function testMapToStruct() (Person) {
    int[] marks = [87,94,72];
    map m = { name:"Child", 
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
               marks:marks,
               a:"any value",
               score: 5.67,
               alive:true
             };
    return m;
}

function testStructToJson() (json) {
    Person p = { name:"Child", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Colombo", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[87,94,72]
               };
    return p;
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
    return j;
}

function testStructToStruct() (Student) {
    Person p = { name:"Supun", 
                 age:25, 
                 parent:{name:"Parent", age:50}, 
                 address:{"city":"Kandy", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[24, 81]
               };
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
    return m;
}

function testMapWithIncompatibleArrayToStruct() (Person) {
    float[] marks = [87,94,72];
    map m = { name:"Child", 
               age:25,
               parent:{
                    name:"Parent", 
                    age:50,
                    parent: null,
                    address:null,
                    info:null,
                    marks:null,
                    a:null,
                    score: 5.67,
                    alive:false
               },
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:marks,
               a:"any value",
               score: 5.67,
               alive:true
             };
    return m;
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
                  age:25, 
                  address:{"city":"Kandy", "country":"SriLanka"}, 
                  marks:[24, 81]
                };
                
    map m = { name:"Child", 
               age:25,
               partner: { name:"PartnerEmployee", 
                           age:50,
                           parent:s,
                           address:{"city":"Colombo", "country":"SriLanka"}, 
                           info:{status:"single"},
                           marks:marks
               },
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:marks
             };
    return m;
}

function testIncompatibleJsonToStruct() (Person) {
    json j = { name:"Child", 
               age:25, 
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:[87,94,72]
             };
    return j;
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
    return j;
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
    return j;
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
    return j;
}

function testJsonArrayToStruct() (Person) {
    json j = [87,94,72];
    return j;
}

struct Killer {
    string name;
    int age;
    Student partner;
    json info;
    map address;
    int[] marks;
}

function testInnerStructToStruct() (Killer) {
    Employee p = { name:"Supun", 
                 age:35, 
                 partner:{name:"StudentPartner", age:27}, 
                 address:{"city":"Kandy", "country":"SriLanka"}, 
                 info:{status:"single"},
                 marks:[24, 81]
               };
    return p;
}

struct Doctor {
    string name;
    int age;
    map partner;
    json info;
    map address;
    int[] marks;
}

function testStructWithIncompatibleInnerMapToStruct() (Employee) {
        Doctor d = { name:"Child", 
               age:25,
               partner: { name:"PartnerEmployee", 
                           age:50,
                           address:{"city":"Colombo", "country":"SriLanka"}, 
                           info:{status:"single"},
                           marks:[1,7,9]
               },
               address:{"city":"Colombo", "country":"SriLanka"}, 
               info:{status:"single"},
               marks:[98,47,5]
             };
    Employee e = <Employee> d;
    return e;
}

function testMapWithXmlToJson() (json) {
    xml x= `<name>Supun</name>`;
    map m = {info: x};
    return m; 
}

struct Info {
    xml name;
}

function testStructWithXmlToJson() (json) {
    xml x= `<name>Supun</name>`;
    Info info = {name: x};
    return info; 
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

function testArrayToJson() (json) {
    Address adrs = {city:"Colombo"};
    int[] intArray = [4,3,7];
    json j = {status:"single"};
    
    any[] a = [4, "Supun", 5.36, true, {lname:"Setunga"}, adrs, intArray, null, j];
    map m = {array:a};
    return m;
}

function testArrayWithIncompatibleTypesToJson() (json) {
    xml x= `<name>Supun</name>`;
    any[] a = [4, x];
    map m = {array:a};
    return m;
}

struct AnyArray {
    any[] a;
}

function testJsonToAnyArray() (AnyArray) {
    json j = {a:[4, "Supun", 5.36, true, {lname:"Setunga"}, [4,3,7], null]};
    return j;
}

struct IntArray {
    int[] a;
}

function testJsonToIntArray() (IntArray) {
    json j = {a:[4,3,9]};
    return j;
}

struct StringArray {
    string[] a;
}

function testJsonToStringArray() (StringArray) {
    json j = {a:["a","b","c"]};
    return j;
}

function testJsonIntArrayToStringArray() (StringArray) {
    json j = {a:[4,3,9]};
    return j;
}

struct XmlArray {
    xml[] a;
}

function testJsonToXmlArray() (XmlArray) {
    json j = {a:["a","b","c"]};
    return j;
}

function testNullJsonArrayToArray() (StringArray) {
    json j = {a:null};
    return j;
}

function testNullJsonToArray() (StringArray) {
    json j;
    return j;
}

function testNonArrayJsonToArray() (StringArray) {
    json j = {a:"im not an array"};
    return j;
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

function testNullJsonToMap() (map) {
    json j;
    return j;
}

function testNullJsonToStruct() (Person) {
    json j;
    return j;
}

function testPrimitiveJsonToMap() (map) {
    json j = "im a primitive";
    return j;
}

function testNullMapToJson() (json) {
    map m;
    return m;
}

function testNullMapToStruct() (Person) {
    map m;
    return m;
}

function testNullStructToStruct() (Student) {
    Person p;
    return p;
}

function testNullStructToJson() (json) {
    Person p;
    return p;
}

function testNullStructToMap() (map) {
    Person p;
    return p;
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

function testAnyXmlToJson() (json) {
    xml x = `<name>Supun<name>`;
    any a = x;
    return (json) a;
}