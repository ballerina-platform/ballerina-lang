struct Person {
    string name;
    int age;
    Person | null parent;
    json info;
    map| null address;
    int[] | null marks;
    any a;
    float score;
    boolean alive;
    Person[]|null children;
}

struct Student {
    string name;
    int age;
}

function testStructToMap () returns (map) {
    Person p = {name:"Child",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Colombo", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[67, 38, 91]
               };
    map m = <map>p;
    return m;
}


function testMapToStruct () returns (Person | error) {
    int[] marks = [87, 94, 72];
    Person parent = {
                        name:"Parent",
                        age:50,
                        parent:null,
                        address:null,
                        info:null,
                        marks:null,
                        a:null,
                        score:4.57,
                        alive:false
                    };

    json info = {status:"single"};
    map m = {name:"Child",
                age:25,
                parent:parent,
                address:{"city":"Colombo", "country":"SriLanka"},
                info:info,
                marks:marks,
                a:"any value",
                score:5.67,
                alive:true
            };
    Person p =? <Person> m;
    return p;
}

function testStructToJson () returns (json) {
    Person | null p = {name:"Child",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Colombo", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[87, 94, 72]
               };

    json j =? <json>p;
    return j;
}

function testJsonToStruct () returns (Person | error) {
    json j = {name:"Child",
                 age:25,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent:null,
                            address:null,
                            info:null,
                            marks:null,
                            a:null,
                            score:4.57,
                            alive:false,
                            children:null
                        },
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[56, 79],
                 a:"any value",
                 score:5.67,
                 alive:true,
                 children:null
             };
    var p = <Person>j;
    return p;
}

function testIncompatibleMapToStruct () returns (Person) {
    int[] marks = [87, 94, 72];
    map m = {name:"Child",
                age:25,
                address:{"city":"Colombo", "country":"SriLanka"},
                info:{status:"single"},
                marks:marks
            };
    Person p =? <Person>m;
    return p;
}

function testMapWithMissingFieldsToStruct () returns (Person) {
    int[] marks = [87, 94, 72];
    map m = {name:"Child",
                age:25,
                address:{"city":"Colombo", "country":"SriLanka"},
                marks:marks
            };
    Person p =? <Person>m;
    return p;
}

function testMapWithIncompatibleArrayToStruct () returns (Person) {
    float[] marks = [87, 94, 72];
    Person parent = {
                        name:"Parent",
                        age:50,
                        parent:null,
                        address:null,
                        info:null,
                        marks:null,
                        a:null,
                        score:5.67,
                        alive:false
                    };
    json info = {status:"single"};
    map m = {name:"Child",
                age:25,
                parent:parent,
                address:{"city":"Colombo", "country":"SriLanka"},
                info:info,
                marks:marks,
                a:"any value",
                score:5.67,
                alive:true
            };

    var p =? <Person>m;
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

function testMapWithIncompatibleStructToStruct () returns (Employee) {
    int[] marks = [87, 94, 72];
    Student s = {name:"Supun",
                    age:25
                };

    map m = {name:"Child",
                age:25,
                partner:s,
                address:{"city":"Colombo", "country":"SriLanka"},
                info:{status:"single"},
                marks:marks
            };
            
    var e =? <Employee>m;
    return e;
}

function testJsonToStructWithMissingFields () returns (Person) {
    json j = {name:"Child",
                 age:25,
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };

    var p =? <Person>j;
    return p;
}

function testIncompatibleJsonToStruct () returns (Person) {
    json j = {name:"Child",
                 age:"25",
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };

    var p =? <Person>j;
    return p;
}

function testJsonWithIncompatibleMapToStruct () returns (Person) {
    json j = {name:"Child",
                 age:25,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent:null,
                            address:"SriLanka",
                            info:null,
                            marks:null
                        },
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };

    var p =? <Person>j;
    return p;
}

function testJsonWithIncompatibleTypeToStruct () returns (Person) {
    json j = {name:"Child",
                 age:25.8,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent:null,
                            address:"SriLanka",
                            info:null,
                            marks:null
                        },
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };

    var p =? <Person>j;
    return p;
}

function testJsonWithIncompatibleStructToStruct () returns (Person) {
    json j = {name:"Child",
                 age:25,
                 parent:{
                            name:"Parent",
                            age:50,
                            parent:"Parent",
                            address:{"city":"Colombo", "country":"SriLanka"},
                            info:null,
                            marks:null
                        },
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };

    var p =? <Person>j;
    return p;
}

function testJsonArrayToStruct () returns (Person) {
    json j = [87, 94, 72];

    var p =? <Person>j;
    return p;
}

struct Info {
    map foo;
}

function testStructWithIncompatibleTypeMapToJson () returns (json) {
    blob b;
    map m = {bar:b};
    Info info = {foo:m};

    var j =? <json>info;
    return j;
}

function testJsonIntToString () returns (string) {
    json j = 5;
    int value;
    value =? <int>j;
    return <string>value;
}

function testBooleanInJsonToInt () returns (int) {
    json j = true;
    int value =? <int>j;
    return value;
}

function testIncompatibleJsonToInt () returns (int) {
    json j = "hello";
    int value;
    value =? <int>j;
    return value;
}

function testIntInJsonToFloat () returns (float) {
    json j = 7;
    float value;
    value =? <float>j;
    return value;
}

function testIncompatibleJsonToFloat () returns (float) {
    json j = "hello";
    float value;
    value =? <float>j;
    return value;
}

function testIncompatibleJsonToBoolean () returns (boolean) {
    json j = "hello";
    boolean value;
    value =? <boolean>j;
    return value;
}

struct Address {
    string city;
    string country;
}

struct AnyArray {
    any[] a;
}

function testJsonToAnyArray () returns (AnyArray) {
    json j = {a:[4, "Supun", 5.36, true, {lname:"Setunga"}, [4, 3, 7], null]};
    AnyArray value =? <AnyArray>j;
    return value;
}

struct IntArray {
    int[] a;
}

function testJsonToIntArray () returns (IntArray) {
    json j = {a:[4, 3, 9]};
    IntArray value =? <IntArray>j;
    return value;
}

