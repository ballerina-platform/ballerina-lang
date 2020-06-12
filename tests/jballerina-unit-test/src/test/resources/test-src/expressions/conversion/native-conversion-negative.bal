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
import ballerina/io;

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

type Student record {|
    string name = "";
    int age = 0;
|};

type Person2 record {|
    string name = "";
    int age = 0;
|};

type StructWithoutDefaults record {|
    string s = "";
    int a;
    float f = 0.0;
    boolean b = false;
    json j = {};
    byte[] blb = [];
|};

type T1 record {
    int x = 0;
    int y = 0;
};

type T2 record {|
    int x = 0;
    int y = 0;
    int z = 0;
|};
    
public type TX record {
    int x = 0;
    int y = 0;
    xml[] xmls = [xml `hello`];
};

type Manager record {
    string name = "";
    int age = 0;
    Manager? parent = ();
};

type Engineer record {
    string name = "";
    int age = 0;
    Engineer? parent = ();
};


function testIncompatibleJsonToStructWithErrors () returns (Person | error) {
    json j = {  name:"Child",
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
    Person p  = check j.cloneWithType(Person);
    return p;
}


function testEmptyJSONtoStructWithoutDefaults () returns (StructWithoutDefaults | error) {
    json j = {};
    var testStruct = check j.cloneWithType(StructWithoutDefaults);
    return testStruct;
}

function testEmptyMaptoStructWithoutDefaults () returns StructWithoutDefaults|error {
    map<anydata> m = {};
    var testStruct = check m.cloneWithType(StructWithoutDefaults);
    return testStruct;
}

function testTupleConversionFail() returns [T1, T2] | error {
    T1 a = {};
    T1 b = {};
    [T1, T1] x = [a, b];
    [T1, T2] x2;
    anydata y = x;
    var result = y.cloneWithType([T1, T2]);
    return result;
}

function testArrayToJsonFail() returns json|error {
    TX[] x = [];
    TX a = {};
    TX b = {};
    a.x = 10;
    b.x = 15;
    x[0] = a;
    x[1] = b;
    return x.cloneWithType(json);
}

function testIncompatibleImplicitConversion() returns int|error {
    json operationReq = { "toInt": "abjd" };
    var abjd = check operationReq.toInt;
    return abjd.cloneWithType(int);
}

function testConvertRecordToRecordWithCyclicValueReferences() returns Engineer|error {
    Manager p = { name: "Waruna", age: 25, parent: () };
    Manager p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    Engineer|error e =  trap p.cloneWithType(Engineer); // Cyclic value will be check with isLikeType method.
    return e;
}

function testConvertRecordToJsonWithCyclicValueReferences() returns json|error {
    Manager p = { name: "Waruna", age: 25, parent: () };
    Manager p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    json j =  check trap p.cloneWithType(json); // Cyclic value will be check with isLikeType method.
    return j;
}

function testConvertRecordToMapWithCyclicValueReferences() returns map<anydata>|error {
    Manager p = { name: "Waruna", age: 25, parent: () };
    Manager p2 = { name: "Milinda", age: 25, parent:p };
    p.parent = p2;
    anydata a = p;
    basicMatch(a);
    map<anydata> m =  check trap p.cloneWithType(map<anydata>); // Cyclic value will be check when stamping the value.
    return m;
}

function basicMatch(any a) {
    match a {
            var {var1, var2, var3} => {io:println("Matched");}
    }
}