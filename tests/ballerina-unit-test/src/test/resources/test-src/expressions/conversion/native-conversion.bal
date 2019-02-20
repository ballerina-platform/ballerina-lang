// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type Person record {
    string name = "";
    int age = 0;
    Person? parent = ();
    json info?;
    map<anydata>? address?;
    int[]? marks?;
    anydata a = ();
    float score = 0.0;
    boolean alive = false;
    Person[]? children?;
    !...;
};

type Person2 record {
    string name = "";
    int age = 0;
    !...;
};

type Person4 record {
    string name = "";
    Person4? parent = ();
    map<anydata>? address?;
    !...;
};

type Student record {
    string name = "";
    int age = 0;
    string school = "";
    !...;
};

function testStructToMap () returns (map<any> | error) {
    Person p = {name:"Child",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Colombo", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[67, 38, 91]
               };
    map<anydata> m =  check map<anydata>.convert(p);
    return m;
}


function testMapToStruct () returns Person|error {
    int[] marks = [87, 94, 72];
    Person parent = {
                        name:"Parent",
                        age:50,
                        parent:(),
                        address:(),
                        info:(),
                        marks:(),
                        a:(),
                        score:4.57,
                        alive:false,
                        children:()
                    };

    json info = {status:"single"};
    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<any> m = {name:"Child",
                age:25,
                parent:parent,
                address:addr,
                info:info,
                marks:marks,
                a:"any value",
                score:5.67,
                alive:true
            };
    Person p = check Person.convert(m);
    return p;
}

function testNestedMapToNestedStruct() returns Person|error {
    int[] marks = [87, 94, 72];
    map<any> parent = {
        name:"Parent",
        age:50,
        parent:(),
        address:(),
        info:(),
        marks:(),
        a:(),
        score:4.57,
        alive:false
    };

    json info = {status:"single"};
    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<any> m = {name:"Child",
        age:25,
        parent:parent,
        address:addr,
        info:info,
        marks:marks,
        a:"any value",
        score:5.67,
        alive:true
    };
    Person p = check Person.convert(m);
    return p;
}

function testStructToJson () returns json|error {
    Person p = {name:"Child",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Colombo", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[87, 94, 72],
                   a:"any value",
                   score:5.67,
                   alive:true,
                   children:()
               };

    json j = check json.convert(p);
    return j;
}

function testAnyRecordToAnydataMap() returns (map<anydata> | error) {
    Person4 p = {   name:"Waruna",
                    parent:{name:"Parent"},
                    address:{"city":"Colombo", "country":"SriLanka"}
    };
    map<anydata> m =  check map<anydata>.convert(p);
    return m;
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
                 alive:true
             };
    var p = Person.convert(j);
    return p;
}

function testMapToStructWithMapValueForJsonField() returns Person|error {
    int[] marks = [87, 94, 72];
    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<string> info = {status:"single"};
    map<any> m = {name:"Child",
                parent:(),
                age:25,
                address:addr,
                info:info,
                a:"any value",
                marks:marks,
                score:5.67,
                alive:true,
                children:()
            };
    Person p = check Person.convert(m);
    return p;
}

function testMapWithMissingOptionalFieldsToStruct () returns Person|error {
    int[] marks = [87, 94, 72];
    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<any> m = {name:"Child",
                parent:(),
                age:25,
                a:"any value",
                address:addr,
                marks:marks,
                score:5.67,
                alive:true
            };
    Person p = check Person.convert(m);
    return p;
}

function testMapWithIncompatibleArrayToStruct () returns Person {
    float[] marks = [87.0, 94.0, 72.0];
    Person parent = {
                        name:"Parent",
                        age:50,
                        parent:(),
                        address:(),
                        info:(),
                        marks:(),
                        a:(),
                        score:5.67,
                        alive:false
                    };
    json info = {status:"single"};
    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<any> m = {name:"Child",
                age:25,
                parent:parent,
                address:addr,
                info:info,
                marks:marks,
                a:"any value",
                score:5.67,
                alive:true
            };

    var p = Person.convert(m);
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
    map<string> address;
    int[] marks;
    !...;
};

function testMapWithIncompatibleStructToStruct () returns Employee {
    int[] marks = [87, 94, 72];
    Student s = {name:"Supun",
                    age:25,
                    school: "ABC College"
                };

    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<string> info = {status:"single"};
    map<any> m = {name:"Child",
                age:25,
                student:s,
                address:addr,
                info:info,
                marks:marks
            };
            
    var e = Employee.convert(m);
    if (e is Employee) {
        return e;
    } else {
        panic e;
    }
}

function testJsonToStructWithMissingOptionalFields () returns Person {
    json j = {name:"Child",
                 parent:(),
                 age:25,
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 a:"any value",
                 marks:[87, 94, 72],
                 score:5.67,
                 alive:true
             };

    var p = Person.convert(j);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testJsonToStructWithMissingRequiredFields () returns Person {
    json j = {name:"Child",
                 parent:(),
                 age:25,
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 a:"any value",
                 marks:[87, 94, 72],
                 score:5.67
             };

    var p = Person.convert(j);
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

    var p = Person.convert(j);
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

    var p = Person.convert(j);
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

    var p = Person.convert(j);
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

    var p = Person.convert(j);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testJsonArrayToStruct () returns Person {
    json j = [87, 94, 72];

    var p = Person.convert(j);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

type Info record {
    map<any> foo;
    !...;
};

type Info2 record {
    byte[] infoBlob = [];
    !...;
};

function testStructWithIncompatibleTypeMapToJson () returns (json) {
    byte[] b = [];
    map<any> m = {bar:b};
    Info info = {foo:m};

    var j = json.convert(info);
    if (j is json) {
        return j;
    } else {
        panic j;
    }
}

function testJsonIntToString () returns string|error {
    json j = 5;
    int value;
    value = check int.convert(j);
    return  string.convert(value);
}

function testFloatToInt() returns (int) {
    float f = 10.05344;
    int i = int.convert(f);
    return i;
}

function testBooleanInJsonToInt () returns int|error {
    json j = true;
    int value = check int.convert(j);
    return value;
}

function testIncompatibleJsonToInt () returns int|error {
    json j = "hello";
    int value;
    value = check int.convert(j);
    return value;
}

function testIntInJsonToFloat () returns float|error {
    json j = 7;
    float value;
    value = check float.convert(j);
    return value;
}

function testIncompatibleJsonToFloat () returns float|error {
    json j = "hello";
    float value;
    value = check float.convert(j);
    return value;
}

function testIncompatibleJsonToBoolean () returns boolean|error {
    json j = "hello";
    boolean value;
    value = check boolean.convert(j);
    return value;
}

type Address record {
    string city;
    string country;
    !...;
};

type AnyArray record {
    anydata[] a;
    !...;
};

function testJsonToAnyArray () returns AnyArray|error {
    json j = {a:[4, "Supun", 5.36, true, {lname:"Setunga"}, [4, 3, 7], null]};
    AnyArray value = check AnyArray.convert(j);
    return value;
}

type IntArray record {
    int[] a;
    !...;
};

function testJsonToIntArray () returns IntArray|error {
    json j = {a:[4, 3, 9]};
    IntArray value = check IntArray.convert(j);
    return value;
}


type StringArray record {
    string[] a;
    !...;
};

function testJsonToStringArray () returns StringArray|error {
    json j = {a:["a", "b", "c"]};
    StringArray a = check StringArray.convert(j);
    return a;
}

function testJsonIntArrayToStringArray () returns json|error {
    json j = {a:[4, 3, 9]};
    int[] a =  check int[].convert(j["a"]);
    string[] s =  [];
    foreach var i in a {
        s[s.length()] = string.convert(i);
    }
    json j2 = {a:s};
    return j2;
}

type XmlArray record {
    xml[] a;
    !...;
};

function testJsonToXmlArray () returns XmlArray {
    json j = {a:["a", "b", "c"]};
    var a = XmlArray.convert(j);
    if (a is XmlArray) {
        return a;
    } else {
        panic a;
    }
}

function testNullJsonArrayToArray () returns StringArray {
    json j = {a:null};
    var a =  StringArray.convert(j);
    if (a is StringArray) {
        return a;
    } else {
        panic a;
    }
}

function testNullJsonToArray () returns StringArray {
    json j = ();
    var s = StringArray.convert(j);
    if (s is StringArray) {
        return s;
    } else {
        panic s;
    }
}

function testNonArrayJsonToArray () returns StringArray {
    json j = {a:"im not an array"};
    var a = StringArray.convert(j);
    if (a is StringArray) {
        return a;
    } else {
        panic a;
    }
}


function testNullJsonToStruct () returns Person {
    json j = ();
    var p = Person.convert(j);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testNullStructToJson () returns (json | error) {
    Person? p = ();
    var j = json.convert(p);
    return j;
}

type PersonA record {
    string name = "";
    int age = 0;
    !...;
};

function JsonToStructWithErrors () returns (PersonA | error) {
    json j = {name:"supun"};

    PersonA pA = check PersonA.convert(j);

    return pA;
}

type PhoneBook record {
    string[] names = [];
    !...;
};

function testStructWithStringArrayToJSON () returns json|error {
    PhoneBook phonebook = {names:["John", "Doe"]};
    var phonebookJson = json.convert(phonebook);
    return phonebookJson;
}

type person record {
    string fname = "";
    string lname = "";
    int age = 0;
    !...;
};

type movie record {
    string title = "";
    int year = 0;
    string released = "";
    string[] genre = [];
    person[] writers = [];
    person[] actors = [];
    !...;
};

function testStructToMapWithRefTypeArray () returns (map<any>, int)|error {
    movie theRevenant = {title:"The Revenant",
                            year:2015,
                            released:"08 Jan 2016",
                            genre:["Adventure", "Drama", "Thriller"],
                            writers:[{fname:"Michael", lname:"Punke", age:30}],
                            actors:[{fname:"Leonardo", lname:"DiCaprio", age:35},
                                    {fname:"Tom", lname:"Hardy", age:34}]};

    map<anydata> m = check map<anydata>.convert(theRevenant);

    anydata a = m["writers"];
    var writers = person[].convert(a);
    if(writers is person[]){
        return (m, writers[0].age);
    } else {
        return (m, 0);
    }
}

type StructWithOptionals record {
    string s?;
    int a?;
    float f?;
    boolean b?;
    json j?;
    byte[] blb?;
    !...;
};

function testEmptyJSONtoStructWithOptionals () returns (StructWithOptionals | error) {
    json j = {};
    var testStruct = check StructWithOptionals.convert(j);

    return testStruct;
}

function testSameTypeConversion() returns (int) {
    float f = 10.05;
    var i =  int.convert(f);
    i =  int.convert(i);
    return i;
}

//function testNullStringToOtherTypes() (int, error,
//                                       float, error,
//                                       boolean, error,
//                                       json, error,
//                                       xml, error) {
//    string s;
//    var i, err1 = int.convert(s);
//    var f, err2 = float.convert(s);
//    var b, err3 = boolean.convert(s);
//    var j, err4 = json.convert(s);
//    var x, err5 = xml.convert(s);
//    
//    return i, err1, f, err2, b, err3, j, err4, x, err5;
//}

function structWithComplexMapToJson() returns (json | error) {
    int a = 4;
    float b = 2.5;
    boolean c = true;
    string d = "apple";
    map<string> e = {"foo":"bar"};
    PersonA f = {};
    int [] g = [1, 8, 7];
    map<any> m = {"a":a, "b":b, "c":c, "d":d, "e":e, "f":f, "g":g, "h":()};
    
    Info info = {foo : m};
    var js =  json.convert(info);
    return js;
}

type ComplexArrayStruct record {
    int[] a;
    float[] b;
    boolean[] c;
    string[] d;
    map<anydata>[] e;
    PersonA[] f;
    json[] g;
    !...;
};

function structWithComplexArraysToJson() returns (json | error) {
    json g = {"foo":"bar"};
    map<anydata> m1 = {};
    map<anydata> m2 = {};
    PersonA p1 = {name:""};
    PersonA p2 = {name:""};
    ComplexArrayStruct t = {a:[4, 6, 9], b:[4.6, 7.5], c:[true, true, false], d:["apple", "orange"], e:[m1, m2], f:[p1, p2], g:[g]};
    var js = json.convert(t);
    return js;
}

function testComplexMapToJson () returns json|error {
    map<any> m = {name:"Supun",
                age:25,
                gpa:2.81,
                status:true
            };
    json j2 = check json.convert(m);
    return j2;
}

function testStructWithIncompatibleTypeToJson () returns json {
    Info2 info = {
        infoBlob : [1, 2, 3, 4, 5]
    };
    var j = json.convert(info);
    if (j is json) {
        return j;
    } else {
        panic j;
    }
}

function testJsonToMapUnconstrained() returns map<any>|error {
    json jx = {};
    jx.x = 5;
    jx.y = 10;
    jx.z = 3.14;
    jx.o = {};
    jx.o.a = "A";
    jx.o.b = "B";
    jx.o.c = true;
    map<anydata> m = check map<anydata>.convert(jx);
    return m;
}

function testJsonToMapConstrained1() returns map<any>|error {
    json j = {};
    j.x = "A";
    j.y = "B";
  
    return check map<string>.convert(j);
}

type T1 record {
    int x = 0;
    int y = 0;
};

function testJsonToMapConstrained2() returns map<any>|error {
    json j1 = {};
    j1.x = 5;
    j1.y = 10;
    json j2 = {};
    j2.a = j1;
    map<T1> m;
    m = check map<T1>.convert(j2);
    return m;
}

function testJsonToMapConstrainedFail() returns map<any> {
    json j1 = {};
    j1.x = 5;
    j1.y = 10.5;
    json j2 = {};
    j2.a = j1;
    map<T1> m = {};
    var result = map<T1>.convert(j2);
    if (result is map<T1>) {
        m = result;
    } else {
        panic result;
    }
    return m;
}

type T2 record {
    int x = 0;
    int y = 0;
    int z = 0;
    !...;
};

function testStructArrayConversion1() returns T1|error {
    T1[] a = [];
    T2[] b = [];
    b[0] = {};
    b[0].x = 5;
    b[0].y = 1;
    b[0].z = 2;
    a = T1[].convert(b);
    return a[0];
}

function testStructArrayConversion2() returns T2|error {
    T1[] a = [];
    T2[] b = [];
    b[0] = {};
    b[0].x = 5;
    b[0].y = 1;
    b[0].z = 2;
    a = T1[].convert(b);
    b = check T2[].convert(a);
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

//function testObjectRecordConversionFail() {
//    O2 a = new;
//    T3 b = {};
//    var result = O2.convert(b);
//    if (result is O2) {
//        a = result;
//    } else {
//        panic result;
//    }
//}

function testTupleConversion1() returns (T1, T1)|error {
    T1 a = {};
    T2 b = {};
    (T1, T2) x = (a, b);
    (T1, T1) x2;
    anydata y = x;
    x2 = check (T1, T1).convert(y);
    return x2;
}

function testTupleConversion2() returns (int, string)|error {
    (int, string) x = (10, "XX");
    anydata y = x;
    x = check (int, string).convert(y);
    return x;
}



function testArrayToJson1() returns json|error {
    int[] x = [];
    x[0] = 10;
    x[1] = 15;
    json j = json.convert(x);
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
    json j = check json.convert(x);
    return j;
}

public type TX record {
  int x = 0;
  int y = 0;
  byte[] b = [];
};

function testJsonToArray1() returns T1[]|error {
    T1[] x = [];
    x[0] = {};
    x[0].x = 10;
    json j = check json.convert(x);
    x = check T1[].convert(j);
    return x;
}

function testJsonToArray2() returns int[]|error {
    json j = [];
    j[0] = 1;
    j[1] = 2;
    j[2] = 3;
    int[] x = check int[].convert(j);
    return x;
}

function testJsonToArrayFail() {
    json j = {};
    j.x = 1;
    j.y = 1.5;
    var result = int[].convert(j);
    if (result is int[]) {
        int[] x = result;
    } else {
        panic result;
    }
}

function testAnydataToFloat() returns float|error {
    anydata a = 5;
    return check float.convert(a);
}

function testAnyToFloat() returns float|error {
    any a = 5;
    return check float.convert(a);
}

type A record {
    float f = 0.0;
};

function testJsonFloatToRecordWithFloat() returns A|error {
    json j = {f : 3.0};
    return check A.convert(j);
}

function testJsonIntToRecordWithFloat() returns A|error {
    json j = {f : 3};
    j.f = check float.convert(j.f);
    return check A.convert(j);
}

function testRecordToJsonWithIsJson() returns boolean {
    Person2 p = {name:"Waruna", age:10};
    var personData = json.convert(p);
    return personData is json;
}

function testImplicitConversionToInt() returns map<any>|error {
    json operationReq = {"fromString": "10", "fromInt": 200, "fromFloat":234.45};
    any fromByte = <byte> 5;
    anydata fromDecimal = <decimal> 23.456;
    any fromBoolean = true;
    int a = check int.convert(operationReq.fromInt);
    int b = check int.convert(operationReq.fromString);
    int c = check int.convert(operationReq.fromFloat);
    int d = check int.convert(fromByte);
    int e = check int.convert(fromDecimal);
    int f = check int.convert(fromBoolean);
    return {"fromInt":a, "fromString":b ,"fromFloat":c, "fromByte":d, "fromDecimal": e, "fromBoolean": f };
}

function testImplicitConversionToFloat() returns map<any>|error {
    json operationReq = {"fromString": "10.2", "fromInt": 200, "fromFloat":234.45};
    any fromByte = <byte> 5;
    anydata fromDecimal = <decimal> 23.456;
    any fromBoolean = true;
    float a = check float.convert(operationReq.fromInt);
    float b = check float.convert(operationReq.fromString);
    float c = check float.convert(operationReq.fromFloat);
    float d = check float.convert(fromByte);
    float e = check float.convert(fromDecimal);
    float f = check float.convert(fromBoolean);
    return {"fromInt":a, "fromString":b ,"fromFloat":c, "fromByte":d, "fromDecimal": e, "fromBoolean": f };
}

function testImplicitConversionToByte() returns map<any>|error {
    any fromString =  "10"; 
    anydata fromInt = <int>2 ;
    any fromFloat = <float>4.45;
    any fromByte = <byte> 5;
    anydata fromDecimal = <decimal> 3.456;
    any fromBoolean = true;
    byte a = check byte.convert(fromInt);
    byte b = check byte.convert(fromString);
    byte c = check byte.convert(fromFloat);
    byte d = check byte.convert(fromByte);
    byte e = check byte.convert(fromDecimal);
    byte f = check byte.convert(fromBoolean);
    return {"fromInt":a, "fromString":b ,"fromFloat":c, "fromByte":d, "fromDecimal": e, "fromBoolean": f };
}

function testImplicitConversionToString() returns map<any>|error {
    json operationReq = {"fromString": "hello", "fromInt": 200, "fromFloat":234.45};
    any fromByte = <byte> 5;
    anydata fromDecimal = <decimal> 23.456;
    any fromBoolean = true;
    string a = check string.convert(operationReq.fromInt);
    string b = check string.convert(operationReq.fromString);
    string c = check string.convert(operationReq.fromFloat);
    string d = string.convert(fromByte);
    string e = string.convert(fromDecimal);
    string f = string.convert(fromBoolean);
    return {"fromInt":a, "fromString":b ,"fromFloat":c, "fromByte":d, "fromDecimal": e, "fromBoolean": f };
}

function testImplicitConversionToDecimal() returns map<any>|error {
    json operationReq = {"fromString": "10.33", "fromInt": 200, "fromFloat":234.45};
    any fromByte = <byte> 5;
    anydata fromDecimal = <decimal> 23.456;
    any fromBoolean = true;
    decimal a = check decimal.convert(operationReq.fromInt);
    decimal b = check decimal.convert(operationReq.fromString);
    decimal c = check decimal.convert(operationReq.fromFloat);
    decimal d = check decimal.convert(fromByte);
    decimal e = check decimal.convert(fromDecimal);
    decimal f = check decimal.convert(fromBoolean);
    return {"fromInt":a, "fromString":b ,"fromFloat":c, "fromByte":d, "fromDecimal": e, "fromBoolean": f };
}

function testImplicitConversionToBoolean() returns map<any>|error {
    json operationReq = {"fromString": "true", "fromInt": 0, "fromFloat":0.0};
    any fromByte = <byte> 5;
    anydata fromDecimal = <decimal> 23.456;
    any fromBoolean = false;
    boolean a = check boolean.convert(operationReq.fromInt);
    boolean b = check boolean.convert(operationReq.fromString);
    boolean c = check boolean.convert(operationReq.fromFloat);
    boolean d = check boolean.convert(fromByte);
    boolean e = check boolean.convert(fromDecimal);
    boolean f = check boolean.convert(fromBoolean);
    return {"fromInt":a, "fromString":b ,"fromFloat":c, "fromByte":d, "fromDecimal": e, "fromBoolean": f };
}

function testConvertWithFuncCall() returns int {
    var val = int.convert(getString("66"));
    if (val is int) {
        return val;
    } else {
        return -1;
    }
}

function getString(any s) returns string {
    return "5";
}
