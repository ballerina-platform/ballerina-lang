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

import ballerina/lang.'int as ints;

type Person record {|
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
|};

type Person2 record {|
    string name = "";
    int age = 0;
|};

type Person4 record {|
    string name = "";
    Person4? parent = ();
    map<anydata>? address?;
|};

type Student record {|
    string name = "";
    int age = 0;
    string school = "";
|};

function testStructToMap () returns (map<anydata> | error) {
    Person p = {name:"Child",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Colombo", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[67, 38, 91]
               };
    map<anydata> m =  check map<anydata>.constructFrom(p);
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
    map<anydata> m = {name:"Child",
                age:25,
                parent:parent,
                address:addr,
                info:info,
                marks:marks,
                a:"any value",
                score:5.67,
                alive:true
            };
    Person p = check Person.constructFrom(m);
    return p;
}

function testNestedMapToNestedStruct() returns Person|error {
    int[] marks = [87, 94, 72];
    map<anydata> parent = {
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
    map<anydata> m = {name:"Child",
        age:25,
        parent:parent,
        address:addr,
        info:info,
        marks:marks,
        a:"any value",
        score:5.67,
        alive:true
    };
    Person p = check Person.constructFrom(m);
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

    json j = check json.constructFrom(p);
    return j;
}

function testAnyRecordToAnydataMap() returns (map<anydata> | error) {
    Person4 p = {   name:"Waruna",
                    parent:{name:"Parent"},
                    address:{"city":"Colombo", "country":"SriLanka"}
    };
    map<anydata> m =  check map<anydata>.constructFrom(p);
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
    var p = Person.constructFrom(j);
    return p;
}

function testMapToStructWithMapValueForJsonField() returns Person|error {
    int[] marks = [87, 94, 72];
    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<string> info = {status:"single"};
    map<anydata> m = {name:"Child",
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
    Person p = check Person.constructFrom(m);
    return p;
}

function testMapWithMissingOptionalFieldsToStruct () returns Person|error {
    int[] marks = [87, 94, 72];
    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<anydata> m = {name:"Child",
                parent:(),
                age:25,
                a:"any value",
                address:addr,
                marks:marks,
                score:5.67,
                alive:true
            };
    Person p = check Person.constructFrom(m);
    return p;
}

function testMapWithIncompatibleArrayToStruct () returns Person {
    string[] marks = ["87.0", "94.0", "72.0"];
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
    map<anydata> m = {name:"Child",
                age:25,
                parent:parent,
                address:addr,
                info:info,
                marks:marks,
                a:"any value",
                score:5.67,
                alive:true
            };

    var p = Person.constructFrom(m);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

type Employee record {|
    string name;
    int age;
    Person partner;
    json info;
    map<string> address;
    int[] marks;
|};

function testMapWithIncompatibleStructToStruct () returns Employee {
    int[] marks = [87, 94, 72];
    Student s = {name:"Supun",
                    age:25,
                    school: "ABC College"
                };

    map<string> addr = {"city":"Colombo", "country":"SriLanka"};
    map<string> info = {status:"single"};
    map<anydata> m = {name:"Child",
                age:25,
                student:s,
                address:addr,
                info:info,
                marks:marks
            };
            
    var e = Employee.constructFrom(m);
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

    var p = Person.constructFrom(j);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

type PersonWithChildren record {|
    string name = "";
    int age = 0;
    Person? parent = ();
    json info?;
    map<anydata>? address?;
    int[]? marks?;
    anydata a = ();
    float score = 0.0;
    boolean alive = false;
    Person[]? children;
|};

function testJsonToStructWithMissingRequiredFields () returns PersonWithChildren {
    json j = {name:"Child",
                 parent:(),
                 age:25,
                 address:{"city":"Colombo", "country":"SriLanka"},
                 info:{status:"single"},
                 a:"any value",
                 marks:[87, 94, 72],
                 score:5.67
             };

    var p = PersonWithChildren.constructFrom(j);
    if (p is PersonWithChildren) {
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

    var p = Person.constructFrom(j);
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

    var p = Person.constructFrom(j);
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

    var p = Person.constructFrom(j);
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

    var p = Person.constructFrom(j);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testJsonArrayToStruct () returns Person {
    json j = [87, 94, 72];

    var p = Person.constructFrom(j);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

type Info record {|
    map<anydata> foo;
|};

type Info2 record {|
    xml[] infoBlob = [];
|};

function testStructWithIncompatibleTypeMapToJson () returns (json) {
    xml xml1 = xml `ballerina`;
    xml[] x = [xml1];
    map<anydata> m = {bar:x};
    Info info = {foo:m};

    var j = json.constructFrom(info);
    if (j is json) {
        return j;
    } else {
        panic j;
    }
}

function testJsonIntToString () returns string|error {
    json j = 5;
    int value;
    value = check int.constructFrom(j);
    return  string.constructFrom(value);
}



function testBooleanInJsonToInt () returns int|error {
    json j = true;
    int value = check int.constructFrom(j);
    return value;
}

function testIncompatibleJsonToInt () returns int|error {
    json j = "hello";
    int value;
    value = check int.constructFrom(j);
    return value;
}

function testIntInJsonToFloat () returns float|error {
    json j = 7;
    float value;
    value = check float.constructFrom(j);
    return value;
}

function testIncompatibleJsonToFloat () returns float|error {
    json j = "hello";
    float value;
    value = check float.constructFrom(j);
    return value;
}

function testIncompatibleJsonToBoolean () returns boolean|error {
    json j = "hello";
    boolean value;
    value = check boolean.constructFrom(j);
    return value;
}

type Address record {|
    string city;
    string country;
|};

type AnyArray record {|
    anydata[] a;
|};

function testJsonToAnyArray () returns AnyArray|error {
    json j = {a:[4, "Supun", 5.36, true, {lname:"Setunga"}, [4, 3, 7], null]};
    AnyArray value = check AnyArray.constructFrom(j);
    return value;
}

type IntArray record {|
    int[] a;
|};

function testJsonToIntArray () returns IntArray|error {
    json j = {a:[4, 3, 9]};
    IntArray value = check IntArray.constructFrom(j);
    return value;
}


type StringArray record {|
    string[] a;
|};

function testJsonToStringArray () returns StringArray|error {
    json j = {a:["a", "b", "c"]};
    StringArray a = check StringArray.constructFrom(j);
    return a;
}

function testJsonIntArrayToStringArray () returns json|error {
    json j = {a:[4, 3, 9]};
    int[] a =  check int[].constructFrom(check j.a);
    string[] s =  [];
    foreach var i in a {
        s[s.length()] = i.toString();
    }
    json j2 = {a:s};
    return j2;
}

type XmlArray record {|
    xml[] a;
|};

function testJsonToXmlArray () returns XmlArray {
    json j = {a:["a", "b", "c"]};
    var a = XmlArray.constructFrom(j);
    if (a is XmlArray) {
        return a;
    } else {
        panic a;
    }
}

function testNullJsonArrayToArray () returns StringArray {
    json j = {a:null};
    var a =  StringArray.constructFrom(j);
    if (a is StringArray) {
        return a;
    } else {
        panic a;
    }
}

function testNullJsonToArray () returns StringArray {
    json j = ();
    var s = StringArray.constructFrom(j);
    if (s is StringArray) {
        return s;
    } else {
        panic s;
    }
}

function testNonArrayJsonToArray () returns StringArray {
    json j = {a:"im not an array"};
    var a = StringArray.constructFrom(j);
    if (a is StringArray) {
        return a;
    } else {
        panic a;
    }
}


function testNullJsonToStruct () returns Person {
    json j = ();
    var p = Person.constructFrom(j);
    if (p is Person) {
        return p;
    } else {
        panic p;
    }
}

function testNullStructToJson () returns json {
    Person? p = ();
    var j = json.constructFrom(p);
    if (j is json) {
        return j;
    } else {
        panic j;
    }
}

type PersonA record {|
    string name = "";
    int age = 0;
|};

function JsonToStructWithErrors () returns (PersonA | error) {
    json j = {name:"supun"};

    PersonA pA = check PersonA.constructFrom(j);

    return pA;
}

type PhoneBook record {|
    string[] names = [];
|};

function testStructWithStringArrayToJSON () returns json|error {
    PhoneBook phonebook = {names:["John", "Doe"]};
    var phonebookJson = json.constructFrom(phonebook);
    return phonebookJson;
}

type person record {|
    string fname = "";
    string lname = "";
    int age = 0;
|};

type movie record {|
    string title = "";
    int year = 0;
    string released = "";
    string[] genre = [];
    person[] writers = [];
    person[] actors = [];
|};

function testStructToMapWithRefTypeArray () returns [map<any>, int]|error {
    movie theRevenant = {title:"The Revenant",
                            year:2015,
                            released:"08 Jan 2016",
                            genre:["Adventure", "Drama", "Thriller"],
                            writers:[{fname:"Michael", lname:"Punke", age:30}],
                            actors:[{fname:"Leonardo", lname:"DiCaprio", age:35},
                                    {fname:"Tom", lname:"Hardy", age:34}]};

    map<anydata> m = check map<anydata>.constructFrom(theRevenant);

    anydata a = m["writers"];
    var writers = person[].constructFrom(a);
    if(writers is person[]){
        return [m, writers[0].age];
    } else {
        return [m, 0];
    }
}

type StructWithOptionals record {|
    string s?;
    int a?;
    float f?;
    boolean b?;
    json j?;
    byte[] blb?;
|};

function testEmptyJSONtoStructWithOptionals () returns (StructWithOptionals | error) {
    json j = {};
    var testStruct = check StructWithOptionals.constructFrom(j);

    return testStruct;
}

//function testNullStringToOtherTypes() (int, error,
//                                       float, error,
//                                       boolean, error,
//                                       json, error,
//                                       xml, error) {
//    string s;
//    var i, err1 = int.constructFrom(s);
//    var f, err2 = float.constructFrom(s);
//    var b, err3 = boolean.constructFrom(s);
//    var j, err4 = json.constructFrom(s);
//    var x, err5 = xml.constructFrom(s);
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
    map<anydata> m = {"a":a, "b":b, "c":c, "d":d, "e":e, "f":f, "g":g, "h":()};
    
    Info info = {foo : m};
    var js =  check json.constructFrom(info);
    return js;
}

type ComplexArrayStruct record {|
    int[] a;
    float[] b;
    boolean[] c;
    string[] d;
    map<anydata>[] e;
    PersonA[] f;
    json[] g;
|};

function structWithComplexArraysToJson() returns (json | error) {
    json g = {"foo":"bar"};
    map<anydata> m1 = {};
    map<anydata> m2 = {};
    PersonA p1 = {name:""};
    PersonA p2 = {name:""};
    ComplexArrayStruct t = {a:[4, 6, 9], b:[4.6, 7.5], c:[true, true, false], d:["apple", "orange"], e:[m1, m2], f:[p1, p2], g:[g]};
    var js = json.constructFrom(t);
    return js;
}

function testComplexMapToJson () returns json|error {
    map<anydata> m = {name:"Supun",
                age:25,
                gpa:2.81,
                status:true
            };
    json j2 = check json.constructFrom(m);
    return j2;
}

function testStructWithIncompatibleTypeToJson () returns json {
    xml xml1 = xml `Hello, world!`;
    Info2 info = {
        infoBlob : [xml1]
    };
    var j = json.constructFrom(info);
    if (j is json) {
        return j;
    } else {
        panic j;
    }
}

function testJsonToMapUnconstrained() returns map<any>|error {
    json jx = {
        x: 5,
        y: 10,
        z: 3.14,
        o: {
            a : "A",
            b : "B",
            c : true
        }
    };
    map<anydata> m = check map<anydata>.constructFrom(jx);
    return m;
}

function testJsonToMapConstrained1() returns map<any>|error {
    json j = {
        x: "A",
        y: "B"
    };

    return map<string>.constructFrom(j);
}

type T1 record {
    int x = 0;
    int y = 0;
};

function testJsonToMapConstrained2() returns map<any>|error {
    json j1 = {
        x: 5,
        y: 10
    };
    json j2 = {
        a : j1
    };
    map<T1> m;
    m = check map<T1>.constructFrom(j2);
    return m;
}

function testJsonToMapConstrainedFail() returns map<any> {
    json j1 = {
        a: {
            x: 5,
            y: "non-convertible"
        }
    };
    map<T1> m = {};
    var result = map<T1>.constructFrom(j1);
    if (result is map<T1>) {
        m = result;
    } else {
        panic result;
    }
    return m;
}

type T2 record {|
    int x = 0;
    int y = 0;
    int z = 0;
|};

function testStructArrayConversion1() returns T1|error {
    T1[] a = [];
    T2[] b = [];
    b[0] = {};
    b[0].x = 5;
    b[0].y = 1;
    b[0].z = 2;
    a = check T1[].constructFrom(b);
    return a[0];
}

function testStructArrayConversion2() returns T2|error {
    T1[] a = [];
    T2[] b = [];
    b[0] = {};
    b[0].x = 5;
    b[0].y = 1;
    b[0].z = 2;
    a = check T1[].constructFrom(b);
    b = check T2[].constructFrom(a);
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
//    var result = O2.constructFrom(b);
//    if (result is O2) {
//        a = result;
//    } else {
//        panic result;
//    }
//}

function testTupleConversion1() returns [T1, T1]|error {
    T1 a = {};
    T2 b = {};
    [T1, T2] x = [a, b];
    [T1, T1] x2;
    anydata y = x;
    x2 = check [T1, T1].constructFrom(y);
    return x2;
}

function testTupleConversion2() returns [int, string]|error {
    [int, string] x = [10, "XX"];
    anydata y = x;
    x = check [int, string].constructFrom(y);
    return x;
}



function testArrayToJson1() returns json|error {
    int[] x = [];
    x[0] = 10;
    x[1] = 15;
    json j = check json.constructFrom(x);
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
    json j = check json.constructFrom(x);
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
    json j = check json.constructFrom(x);
    x = check T1[].constructFrom(j);
    return x;
}

function testJsonToArray2() returns int[]|error {
    json j = [1, 2, 3];
    int[] x = check int[].constructFrom(j);
    return x;
}

function testJsonToArrayFail() {
    json j = {
        x: 1,
        y: 1.5
    };
    var result = int[].constructFrom(j);
    if (result is int[]) {
        int[] x = result;
    } else {
        panic result;
    }
}

type A record {
    float f = 0.0;
};

function testJsonFloatToRecordWithFloat() returns A|error {
    json j = {f : 3.0};
    return A.constructFrom(j);
}

function testRecordToJsonWithIsJson() returns boolean {
    Person2 p = {name:"Waruna", age:10};
    var personData = json.constructFrom(p);
    return personData is json;
}

function testImplicitConversionToInt() returns map<any>|error {
    json operationReq = {"fromString": "10", "fromInt": 200, "fromFloat":234.45};
    anydata fromByte = <byte> 5;
    anydata fromDecimal = 23.456d;
    anydata fromBoolean = true;
    int a = check int.constructFrom(check operationReq.fromInt);
    int b = check int.constructFrom(check operationReq.fromString);
    int c = check int.constructFrom(check operationReq.fromFloat);
    int d = check int.constructFrom(fromByte);
    int e = check int.constructFrom(fromDecimal);
    int f = check int.constructFrom(fromBoolean);
    return {"fromInt":a, "fromString":b ,"fromFloat":c, "fromByte":d, "fromDecimal": e, "fromBoolean": f };
}

function testImplicitConversionToString() returns map<any>|error {
    anydata fromBoolean = true;
    anydata fromInt = 200;
    anydata fromString = "hello";
    anydata fromFloat = 234.45;
    anydata fromByte = <byte> 5;
    anydata fromDecimal = 23.456d;

    string a = fromInt.toString();
    string b = fromString.toString();
    string c = fromFloat.toString();
    string d = fromByte.toString();
    string e = fromDecimal.toString();
    string f = fromBoolean.toString();
    return {"fromInt":a, "fromString":b ,"fromFloat":c, "fromByte":d, "fromDecimal": e, "fromBoolean": f };
}

function testConvertWithFuncCall() returns int|error {
    var val = check ints:fromString(getString("66"));
    return val;
}

function getString(any s) returns string {
    return "5";
}

function testConvertWithFuncReturnUnion() returns int {
    var val = getLength("125");
    if (val is int) {
        return val;
    } else {
        return -1;
    }
}

function getLength(string s) returns int|error {
    return ints:fromString(s);
}
