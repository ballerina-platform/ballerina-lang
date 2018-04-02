import ballerina/http;

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
    Person[] children;
}

struct Student {
    string name;
    int age;
}

function testStructToMap () (map) {
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

function testMapToStruct () (Person) {
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
    Person p;
    p, _ = <Person>m;
    return p;
}

function testStructToJson () (json) {
    Person p = {name:"Child",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Colombo", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[87, 94, 72]
               };

    json j;
    j, _ = <json>p;
    return j;
}

function testJsonToStruct () (Person) {
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
                            alive:false
                        },
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[56, 79],
                 a:"any value",
                 score:5.67,
                 alive:true
             };
    Person p;
    p, _ = <Person>j;
    return p;
}

function testIncompatibleMapToStruct () (Person) {
    int[] marks = [87, 94, 72];
    map m = {name:"Child",
                age:25,
                address:{"city":"Colombo", "country":"SriLanka"},
                info:{status:"single"},
                marks:marks
            };
    Person p;
    TypeConversionError e;
    p, e = <Person>m;
    if (e != null) {
        throw e;
    }
    return p;
}

function testMapWithMissingFieldsToStruct () (Person) {
    int[] marks = [87, 94, 72];
    map m = {name:"Child",
                age:25,
                address:{"city":"Colombo", "country":"SriLanka"},
                marks:marks
            };
    Person p;
    TypeConversionError e;
    p, e = <Person>m;
    if (e != null) {
        throw e;
    }
    return p;
}

function testMapWithIncompatibleArrayToStruct () (Person) {
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
    Person p;
    TypeConversionError e;
    p, e = <Person>m;
    if (e != null) {
        throw e;
    }
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

function testMapWithIncompatibleStructToStruct () (Employee) {
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
    Employee e;
    e, _ = <Employee>m;
    return e;
}

function testJsonToStructWithMissingFields () (Person) {
    json j = {name:"Child",
                 age:25,
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };
    Person p;
    TypeConversionError e;
    p, e = <Person>j;
    if (e != null) {
        throw e;
    }
    return p;
}

function testIncompatibleJsonToStruct () (Person) {
    json j = {name:"Child",
                 age:"25",
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };
    Person p;
    TypeConversionError e;
    p, e = <Person>j;
    if (e != null) {
        throw e;
    }
    return p;
}

function testJsonWithIncompatibleMapToStruct () (Person) {
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
    Person p;
    TypeConversionError e;
    p, e = <Person>j;
    if (e != null) {
        throw e;
    }
    return p;
}

function testJsonWithIncompatibleTypeToStruct () (Person) {
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
    Person p;
    TypeConversionError e;
    p, e = <Person>j;
    if (e != null) {
        throw e;
    }
    return p;
}

function testJsonWithIncompatibleStructToStruct () (Person) {
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
    Person p;
    TypeConversionError e;
    p, e = <Person>j;
    if (e != null) {
        throw e;
    }
    return p;
}

function testJsonArrayToStruct () (Person) {
    json j = [87, 94, 72];
    Person p;
    TypeConversionError e;
    p, e = <Person>j;
    if (e != null) {
        throw e;
    }
    return p;
}

struct Info {
    map foo;
}

function testStructWithIncompatibleTypeMapToJson () (json) {
    blob b;
    map m = {bar:b};
    Info info = {foo:m};

    json j;
    TypeConversionError err;
    j, err = <json>info;
    if (err != null) {
        throw err;
    }
    return j;

}

function testJsonIntToString () (string) {
    json j = 5;
    int value;
    value, _ = (int)j;
    return <string>value;
}

function testBooleanInJsonToInt () (int) {
    json j = true;
    int value;
    value, _ = (int)j;
    return value;
}

function testIncompatibleJsonToInt () (int) {
    json j = "hello";
    int value;
    value, _ = (int)j;
    return value;
}

function testIntInJsonToFloat () (float) {
    json j = 7;
    float value;
    value, _ = (float)j;
    return value;
}

function testIncompatibleJsonToFloat () (float) {
    json j = "hello";
    float value;
    value, _ = (float)j;
    return value;
}

function testIncompatibleJsonToBoolean () (boolean) {
    json j = "hello";
    boolean value;
    value, _ = (boolean)j;
    return value;
}

struct Address {
    string city;
    string country;
}

struct AnyArray {
    any[] a;
}

function testJsonToAnyArray () (AnyArray) {
    json j = {a:[4, "Supun", 5.36, true, {lname:"Setunga"}, [4, 3, 7], null]};
    AnyArray a;
    a, _ = <AnyArray>j;
    return a;
}

struct IntArray {
    int[] a;
}

function testJsonToIntArray () (IntArray) {
    json j = {a:[4, 3, 9]};
    IntArray a;
    a, _ = <IntArray>j;
    return a;
}

struct StringArray {
    string[] a;
}

function testJsonToStringArray () (StringArray) {
    json j = {a:["a", "b", "c"]};
    StringArray a;
    a, _ = <StringArray>j;
    return a;
}

function testJsonIntArrayToStringArray () (StringArray) {
    json j = {a:[4, 3, 9]};
    StringArray a;
    a, _ = <StringArray>j;
    return a;
}

struct XmlArray {
    xml[] a;
}

function testJsonToXmlArray () (XmlArray) {
    json j = {a:["a", "b", "c"]};
    XmlArray a;
    TypeConversionError e;
    a, e = <XmlArray>j;
    if (e != null) {
        throw e;
    }
    return a;
}

function testNullJsonArrayToArray () (StringArray) {
    json j = {a:null};
    StringArray a;
    a, _ = <StringArray>j;
    return a;
}

function testNullJsonToArray () (StringArray) {
    json j;
    StringArray a;
    a, _ = <StringArray>j;
    return a;
}

function testNonArrayJsonToArray () (StringArray) {
    json j = {a:"im not an array"};
    StringArray a;
    TypeConversionError e;
    a, e = <StringArray>j;
    if (e != null) {
        throw e;
    }
    return a;
}

function testNullJsonToString () (string) {
    json j;
    //json to string is a cast, not a conversion
    string value;
    //value, _ = (string)j;
    return value;
}

function testNullJsonToInt () (int) {
    json j;
    //json to int is a cast, not a conversion
    int value;
    //value, _ = (int)j;
    return value;
}

function testNullJsonToFloat () (float) {
    json j;
    //json to float is a cast, not a conversion
    float value;
    //value, _ = (float)j;
    return value;
}

function testNullJsonToBoolean () (boolean) {
    json j;
    //json to boolean is a cast, not a conversion
    boolean value;
    //value, _ = (boolean)j;
    return value;
}

function testNullJsonToStruct () (Person) {
    json j;
    Person p;
    p, _ = <Person>j;
    return p;
}

function testNullMapToStruct () (Person) {
    //map m;
    Person p;
    //p, _ = <Person>m;
    return p;
}

function testNullStructToJson () (json) {
    //Person p;
    json j;
    //j, _ = <json>p;
    return j;
}

function testNullStructToMap () (map) {
    Person p;
    map m = <map>p;
    return m;
}

function testIncompatibleJsonToStructWithErrors () (Person, TypeConversionError) {
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
    TypeConversionError err;
    Person p;
    p, err = <Person>j;
    return p, err;
}

struct PersonA {
    string name;
    int age;
}

function JsonToStructWithErrors () (PersonA, TypeConversionError) {
    TypeConversionError err;
    PersonA person;
    json j = {name:"supun"};

    person, err = <PersonA>j;

    return person, err;
}

struct PhoneBook {
    string[] names;
}

function testStructWithStringArrayToJSON () (json) {
    PhoneBook phonebook = {names:["John", "Doe"]};
    var phonebookJson, error = <json>phonebook;
    return phonebookJson;
}

struct person {
    string fname;
    string lname;
    int age;
}

struct movie {
    string title;
    int year;
    string released;
    string[] genre;
    person[] writers;
    person[] actors;
}

function testStructToMapWithRefTypeArray () (map, int) {
    movie theRevenant = {title:"The Revenant",
                            year:2015,
                            released:"08 Jan 2016",
                            genre:["Adventure", "Drama", "Thriller"],
                            writers:[{fname:"Michael", lname:"Punke", age:30}],
                            actors:[{fname:"Leonardo", lname:"DiCaprio", age:35},
                                    {fname:"Tom", lname:"Hardy", age:34}]};

    map m = <map>theRevenant;

    any a = m["writers"];
    var writers, _ = (person[])a;

    return m, writers[0].age;
}

struct StructWithDefaults {
    string s = "string value";
    int a = 45;
    float f = 5.3;
    boolean b = true;
    json j;
    blob blb;
}

function testEmptyJSONtoStructWithDefaults () (StructWithDefaults) {
    json j = {};
    var testStruct, _ = <StructWithDefaults>j;

    return testStruct;
}

struct StructWithoutDefaults {
    string s;
    int a;
    float f;
    boolean b;
    json j;
    blob blb;
}

function testEmptyJSONtoStructWithoutDefaults () (StructWithoutDefaults) {
    json j = {};
    var testStruct, _ = <StructWithoutDefaults>j;

    return testStruct;
}

function testEmptyMaptoStructWithDefaults () (StructWithDefaults) {
    map m = {};
    var testStruct, _ = <StructWithDefaults>m;

    return testStruct;
}

function testEmptyMaptoStructWithoutDefaults () (StructWithoutDefaults) {
    map m = {};
    var testStruct, _ = <StructWithoutDefaults>m;

    return testStruct;
}
