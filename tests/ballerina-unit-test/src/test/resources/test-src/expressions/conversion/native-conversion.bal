type Person record {
    string name = "";
    int age = 0;
    Person? parent = ();
    json info = ();
    map? address = ();
    int[]? marks = ();
    any a = ();
    float score = 0.0;
    boolean alive = false;
    Person[]? children = ();
    !...
};

type Person2 record {
    string name = "";
    int age = 0;
    !...
};

type Person3 record {
    string name = "";
    int age = 0;
    string gender = "";
    !...
};

type Student record {
    string name = "";
    int age = 0;
    !...
};

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


function testMapToStruct () returns Person|error {
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
    map addr = {"city":"Colombo", "country":"SriLanka"};
    map m = {name:"Child",
                age:25,
                parent:parent,
                address:addr,
                info:info,
                marks:marks,
                a:"any value",
                score:5.67,
                alive:true
            };
    Person p = check <Person> m;
    return p;
}

function testNestedMapToNestedStruct() returns Person|error {
    int[] marks = [87, 94, 72];
    map parent = {
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
    map addr = {"city":"Colombo", "country":"SriLanka"};
    map m = {name:"Child",
        age:25,
        parent:parent,
        address:addr,
        info:info,
        marks:marks,
        a:"any value",
        score:5.67,
        alive:true
    };
    Person p = check <Person> m;
    return p;
}

function testStructToJson () returns json|error {
    Person p = {name:"Child",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Colombo", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[87, 94, 72]
               };

    json j = check <json>p;
    return j;
}

function testStructToJsonConstrained1() returns json|error {
    Person p = {   name:"Child",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Colombo", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[87, 94, 72]
               };
    json<Person2> j = check <json<Person2>> p;
    return j;
}

function testStructToJsonConstrained2() returns json|error {
    Person2 p = {   name:"Child",
                    age:25
                };
    json<Person2> j = check <json<Person2>> p;
    return j;
}

function testStructToJsonConstrainedNegative() returns json {
    Person2 p = {   name:"Child",
                    age:25
                };
    json<Person3> j = ();
    var result = <json<Person3>> p;
    if (result is json<Person3>) {
        j = result;
    } else if (result is error) {
        panic result;
    }
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

function testMapToStructWithMapValueForJsonField() returns Person|error {
    int[] marks = [87, 94, 72];
    map addr = {"city":"Colombo", "country":"SriLanka"};
    map info = {status:"single"};
    map m = {name:"Child",
                age:25,
                address:addr,
                info:info,
                marks:marks
            };
    Person p = check <Person>m;
    return p;
}

function testMapWithMissingFieldsToStruct () returns Person|error {
    int[] marks = [87, 94, 72];
    map addr = {"city":"Colombo", "country":"SriLanka"};
    map m = {name:"Child",
                age:25,
                address:addr,
                marks:marks
            };
    Person p = check <Person>m;
    return p;
}

function testMapWithIncompatibleArrayToStruct () returns Person {
    float[] marks = [87.0, 94.0, 72.0];
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
    map addr = {"city":"Colombo", "country":"SriLanka"};
    map m = {name:"Child",
                age:25,
                parent:parent,
                address:addr,
                info:info,
                marks:marks,
                a:"any value",
                score:5.67,
                alive:true
            };

    var p = <Person>m;
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

type Employee record {
    string name;
    int age;
    Person partner;
    json info;
    map address;
    int[] marks;
    !...
};

function testMapWithIncompatibleStructToStruct () returns Employee {
    int[] marks = [87, 94, 72];
    Student s = {name:"Supun",
                    age:25
                };

    map addr = {"city":"Colombo", "country":"SriLanka"};
    map info = {status:"single"};
    map m = {name:"Child",
                age:25,
                partner:s,
                address:addr,
                info:info,
                marks:marks
            };
            
    var e = <Employee>m;
    if (e is Employee) {
        return e;
    } else {
        panic e;
    }
}

function testJsonToStructWithMissingFields () returns Person {
    json j = {name:"Child",
                 age:25,
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };

    var p = <Person>j;
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testIncompatibleJsonToStruct () returns Person {
    json j = {name:"Child",
                 age:"25",
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 marks:[87, 94, 72]
             };

    var p = <Person>j;
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testJsonWithIncompatibleMapToStruct () returns Person {
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

    var p = <Person>j;
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testJsonWithIncompatibleTypeToStruct () returns Person {
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

    var p = <Person>j;
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testJsonWithIncompatibleStructToStruct () returns Person {
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

    var p = <Person>j;
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testJsonArrayToStruct () returns Person {
    json j = [87, 94, 72];

    var p = <Person>j;
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

type Info record {
    map foo;
    !...
};

function testStructWithIncompatibleTypeMapToJson () returns json {
    byte[] b = [];
    map m = {bar:b};
    Info info = {foo:m};

    var j = <json>info;
    if (j is json) {
        return j;
    } else {
        panic j;
    }
}

function testJsonIntToString () returns string|error {
    json j = 5;
    int value;
    value = check <int>j;
    return <string>value;
}

function testBooleanInJsonToInt () returns int|error {
    json j = true;
    int value = check <int>j;
    return value;
}

function testIncompatibleJsonToInt () returns int|error {
    json j = "hello";
    int value;
    value = check <int>j;
    return value;
}

function testIntInJsonToFloat () returns float|error {
    json j = 7;
    float value;
    value = check <float>j;
    return value;
}

function testIncompatibleJsonToFloat () returns float|error {
    json j = "hello";
    float value;
    value = check <float>j;
    return value;
}

function testIncompatibleJsonToBoolean () returns boolean|error {
    json j = "hello";
    boolean value;
    value = check <boolean>j;
    return value;
}

type Address record {
    string city;
    string country;
    !...
};

type AnyArray record {
    any[] a;
    !...
};

function testJsonToAnyArray () returns AnyArray|error {
    json j = {a:[4, "Supun", 5.36, true, {lname:"Setunga"}, [4, 3, 7], null]};
    AnyArray value = check <AnyArray>j;
    return value;
}

type IntArray record {
    int[] a;
    !...
};

function testJsonToIntArray () returns IntArray|error {
    json j = {a:[4, 3, 9]};
    IntArray value = check <IntArray>j;
    return value;
}


type StringArray record {
    string[]? a;
    !...
};

function testJsonToStringArray () returns StringArray|error {
    json j = {a:["a", "b", "c"]};
    StringArray a = check <StringArray>j;
    return a;
}

function testJsonIntArrayToStringArray () returns StringArray|error {
    json j = {a:[4, 3, 9]};
    StringArray a = check <StringArray>j;
    return a;
}

type XmlArray record {
    xml[] a;
    !...
};

function testJsonToXmlArray () returns XmlArray {
    json j = {a:["a", "b", "c"]};
    var a = <XmlArray>j;
    if (a is XmlArray) {
        return a;
    } else {
        panic a;
    }
}

function testNullJsonArrayToArray () returns StringArray|error {
    json j = {a:null};
    StringArray a = check <StringArray>j;
    return a;
}

function testNullJsonToArray () returns StringArray {
    json j = ();
    var s = <StringArray>j;
    if (s is StringArray) {
        return s;
    } else {
        panic s;
    }
}

function testNonArrayJsonToArray () returns StringArray {
    json j = {a:"im not an array"};
    var a = <StringArray>j;
    if (a is StringArray) {
        return a;
    } else {
        panic a;
    }
}

function testNullJsonToStruct () returns Person {
    json j = ();
    var p = <Person>j;
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testNullStructToJson () returns (json | error) {
    Person? p = ();
    var j = check <json> p;
    return j;
}

function testIncompatibleJsonToStructWithErrors () returns (Person | error) {
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
             
    Person p  = check <Person>j;
    return p;
}

type PersonA record {
    string name = "";
    int age = 0;
    !...
};

function JsonToStructWithErrors () returns (PersonA | error) {
    json j = {name:"supun"};

    PersonA pA = check <PersonA>j;

    return pA;
}

type PhoneBook record {
    string[] names = [];
    !...
};

function testStructWithStringArrayToJSON () returns json|error {
    PhoneBook phonebook = {names:["John", "Doe"]};
    var phonebookJson = check <json>phonebook;
    return phonebookJson;
}

type person record {
    string fname = "";
    string lname = "";
    int age = 0;
    !...
};

type movie record {
    string title = "";
    int year = 0;
    string released = "";
    string[] genre = [];
    person[] writers = [];
    person[] actors = [];
    !...
};

function testStructToMapWithRefTypeArray () returns (map, int)|error {
    movie theRevenant = {title:"The Revenant",
                            year:2015,
                            released:"08 Jan 2016",
                            genre:["Adventure", "Drama", "Thriller"],
                            writers:[{fname:"Michael", lname:"Punke", age:30}],
                            actors:[{fname:"Leonardo", lname:"DiCaprio", age:35},
                                    {fname:"Tom", lname:"Hardy", age:34}]};

    map m = <map>theRevenant;

    any a = m["writers"];
    var writers = check <person[]> a;

    return (m, writers[0].age);
}

type StructWithDefaults record {
    string s = "string value";
    int a = 45;
    float f = 5.3;
    boolean b = true;
    json j = ();
    byte[] blb = [];
    !...
};

function testEmptyJSONtoStructWithDefaults () returns (StructWithDefaults | error) {
    json j = {};
    var testStruct = check <StructWithDefaults>j;

    return testStruct;
}

type StructWithoutDefaults record {
    string s = "";
    int a = 0;
    float f = 0.0;
    boolean b = false;
    json j = {};
    byte[] blb = [];
    !...
};

function testEmptyJSONtoStructWithoutDefaults () returns (StructWithoutDefaults | error) {
    json j = {};
    var testStruct = check <StructWithoutDefaults>j;

    return testStruct;
}

function testEmptyMaptoStructWithDefaults () returns StructWithDefaults|error {
    map m = {};
    var testStruct = check <StructWithDefaults>m;

    return testStruct;
}

function testEmptyMaptoStructWithoutDefaults () returns StructWithoutDefaults|error {
    map m = {};
    var testStruct = check <StructWithoutDefaults>m;

    return testStruct;
}

function testSameTypeConversion() returns (int) {
    float f = 10.05;
    var i= <int> f;
    i = <int>i;
    return i;
}

//function testNullStringToOtherTypes() (int, error,
//                                       float, error,
//                                       boolean, error,
//                                       json, error,
//                                       xml, error) {
//    string s;
//    var i, err1 = <int> s;
//    var f, err2 = <float> s;
//    var b, err3 = <boolean> s;
//    var j, err4 = <json> s;
//    var x, err5 = <xml> s;
//    
//    return i, err1, f, err2, b, err3, j, err4, x, err5;
//}

function structWithComplexMapToJson() returns (json | error) {
    int a = 4;
    float b = 2.5;
    boolean c = true;
    string d = "apple";
    map e = {"foo":"bar"};
    PersonA f = {};
    int [] g = [1, 8, 7];
    map m = {"a":a, "b":b, "c":c, "d":d, "e":e, "f":f, "g":g, "h":null};
    
    Info info = {foo : m};
    var js = check <json> info;
    return js;
}

type ComplexArrayStruct record {
    int[] a = [];
    float[] b = [];
    boolean[] c = [];
    string[] d = [];
    map[] e = [];
    PersonA[] f = [];
    json[] g = [];
    !...
};

function structWithComplexArraysToJson() returns (json | error) {
    json g = {"foo":"bar"};
    map m1 = {};
    map m2 = {};
    PersonA p1 = {name:""};
    PersonA p2 = {name:""};
    ComplexArrayStruct t = {a:[4, 6, 9], b:[4.6, 7.5], c:[true, true, false], d:["apple", "orange"], e:[m1, m2], f:[p1, p2], g:[g]};
    var js = check <json> t;
    return js;
}

function testComplexMapToJson () returns json|error {
    map m = {name:"Supun",
                age:25,
                gpa:2.81,
                status:true
            };
    json j2 = check <json> m;
    return j2;
}

function testJsonToMapUnconstrained() returns map|error {
    json jx = {};
    jx.x = 5;
    jx.y = 10;
    jx.z = 3.14;
    jx.o = {};
    jx.o.a = "A";
    jx.o.b = "B";
    jx.o.c = true;
    map m = check <map> jx;
    return m;
}

function testJsonToMapConstrained1() returns map|error {
    json j = {};
    j.x = "A";
    j.y = "B";
  
    return check <map<string>> j;
}

type T1 record {
    int x = 0;
    int y = 0;
    !...
};

function testJsonToMapConstrained2() returns map|error {
    json j1 = {};
    j1.x = 5;
    j1.y = 10;
    json j2 = {};
    j2.a = j1;
    map<T1> m;
    m = check <map<T1>> j2;
    return m;
}

function testJsonToMapConstrainedFail() returns map {
    json j1 = {};
    j1.x = 5;
    j1.y = 10.5;
    json j2 = {};
    j2.a = j1;
    map<T1> m = {};
    var result = <map<T1>> j2;
    if (result is map<T1>) {
        m = result;
    } else if (result is error){
        panic result;
    }
    return m;
}

type T2 record {
    int x = 0;
    int y = 0;
    int z = 0;
    !...
};

function testStructArrayConversion1() returns T1 {
    T1[] a = [];
    T2[] b = [];
    b[0] = {};
    b[0].x = 5;
    b[0].y = 1;
    b[0].z = 2;
    a = <T1[]> b;
    return a[0];
}

function testStructArrayConversion2() returns T2|error {
    T1[] a = [];
    T2[] b = [];
    b[0] = {};
    b[0].x = 5;
    b[0].y = 1;
    b[0].z = 2;
    a = <T1[]> b;
    b = check <T2[]> a;
    return b[0];
}

public type T3 record {
  int x = 0;
  int y = 0;
};

public type O1 object {
  public int x = 0;
  public int y = 0;
};

public type O2 object {
  public int x = 0;
  public int y = 0;
  public int z = 0;
};

function testObjectRecordConversionFail() {
    O2 a = new;
    T3 b = {};
    var result = <O2> b;
    if (result is O2) {
        a = result;
    } else {
        panic result;
    }
}

function testTupleConversion1() returns (T1, T1)|error {
    T1 a = {};
    T2 b = {};
    (T1, T2) x = (a, b);
    (T1, T1) x2;
    any y = x;
    x2 = check <(T1, T1)> y;
    return x2;
}

function testTupleConversion2() returns (int, string)|error {
    (int, string) x = (10, "XX");
    any y = x;
    x = check <(int, string)> y;
    return x;
}

function testTupleConversionFail() {
    T1 a = {};
    T1 b = {};
    (T1, T1) x = (a, b);
    (T1, T2) x2;
    any y = x;
    var result = <(T1, T2)> y;
    if (result is (T1, T2)) {
        x2 = result;
    } else if (result is error) {
        panic result;
    }
}

function testArrayToJson1() returns json|error {
    int[] x = [];
    x[0] = 10;
    x[1] = 15;
    json j = check <json> x;
    return j;
}

function testArrayToJson2() returns json|error {
    T1[] x = [];
    T1 a = {};
    T1 b = {};
    a.x = 10;
    b.x = 15;
    x[0] = a;
    x[1] = b;
    json j = check <json> x;
    return j;
}

public type TX record {
  int x = 0;
  int y = 0;
  byte[] b = [];
};

function testArrayToJsonFail() {
    TX[] x = [];
    TX a = {};
    TX b = {};
    a.x = 10;
    b.x = 15;
    x[0] = a;
    x[1] = b;
    var result = <json> x;
    if (result is json) {
        json j = result;
    } else {
        panic result;
    }
}

function testJsonToArray1() returns T1[]|error {
    T1[] x = [];
    x[0] = {};
    x[0].x = 10;
    json j = check <json> x;
    x = check <T1[]> j;
    return x;
}

function testJsonToArray2() returns int[]|error {
    json j = [];
    j[0] = 1;
    j[1] = 2;
    j[2] = 3;
    int[] x = check <int[]> j;
    return x;
}

function testJsonToArrayFail() {
    json j = {};
    j.x = 1;
    j.y = 1.5;
    var result = <int[]> j;
    if (result is int[]) {
        int[] x = result;
    } else if (result is error) {
        panic result;
    }
}

function anyToFloat() returns float|error {
    any a = 5;
    return check <float> a;
}

type A record {
    float f = 0.0;
};

function testJsonIntToFloat() returns A|error {
    json j = {f : 3};
    return check <A> j;
}
