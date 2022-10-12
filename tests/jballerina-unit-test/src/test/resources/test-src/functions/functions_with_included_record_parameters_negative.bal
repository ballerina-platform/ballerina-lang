// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type NewPerson record {
    string firstName;
    string secondName;
};

type NewStudent record {
    string firstName;
    string secondName;
    int age;
    Grades grades?;
};

type Address record {|
    string city;
    string country;
|};

type Grades record {|
    int maths;
    int physics;
    int chemistry;

    int...;
|};

type Foo record {
    int a = 10;
    string b;
};

function functionOfFunctionTypedParamWithIncludedRecordParam(*NewPerson person, *NewStudent student) returns string {
    return person.firstName + " " + person.secondName;
}

function functionWithIncludedRecordParam1(*Foo foo) returns string {
    return foo.b;
}

function testFunctionWithIncludedRecordParam() {
    string s = functionWithIncludedRecordParam1();
    string firstName = functionWithIncludedRecordParam2(firstName = "chiran", secondName = "sachintha", age = "24");
    string name = functionWithIncludedRecordParam2();
    string country = functionWithIncludedRecordParam3(city = "Matara", country = "Sri Lanka", zip = 81000);
}

function functionWithIncludedRecordParam2(*NewPerson person) returns string {
    return person.firstName;
}

function functionWithIncludedRecordParam3(*Address address) returns string {
    return address.firstName;
}

type Bar record {
    never a?;
    never b?;
};

function functionWithIncludedRecordParam4(*Bar bar) returns int? {
    return bar["a"];
}

function testfunctionWithIncludedRecordParam2() {
    int? a = functionWithIncludedRecordParam4(a = 10);
    anydata a1 = functionWithIncludedRecordParam5(abc = 10);
}

type Bar2 record {|
    never a?;
    never b?;
|};

function functionWithIncludedRecordParam5(*Bar2 bar) returns anydata {
    return bar["abc"];
}

type Foo2 record {
    int a;
    int b;
    int c;
};

function functionWithIncludedRecordParam6(*Bar bar, *Foo2 foo) returns anydata {
    return bar["abc"];
}

function testfunctionWithIncludedRecordParam3() {
    anydata a1 = functionWithIncludedRecordParam6(a = 10, b = 10, c = 10, abc = 10);
}

type Foo3 record {|
    int a;
    int b;
|};

type Foo4 record {
    never a?;
    never b?;
};

function functionWithIncludedRecordParam7(int c, *Foo4 foo4, *Foo3 foo3) returns anydata {
    return foo4["abc"];
}

function testfunctionWithIncludedRecordParam4() {
    anydata a1 = functionWithIncludedRecordParam7(25, a = 10, b = 10, abc = 10);
}

type Foo5 record {
    never a?;
    never b?;
    never d?;
};

function functionWithIncludedRecordParam8(int d, *Foo2 foo4, *Foo5 foo5) returns anydata {
    return foo5["abc"];
}

function testfunctionWithIncludedRecordParam5() {
    anydata a1 = functionWithIncludedRecordParam8(25, a = 10, b = 10, abc = 10);
}

function functionWithIncludedRecordParam9(int a, int b, int d, *Foo4 foo4, *Foo5 foo5) returns anydata {
    return foo5["abc"];
}

function testfunctionWithIncludedRecordParam6() {
    anydata a1 = functionWithIncludedRecordParam9(10, 10, 25, abc = 10);
}

function functionWithIncludedRecordParam10(*Foo5 foo5) returns anydata {
    return foo5["abc"];
}

type Options record {|
    string name;
|};

type OthRecord record {|
    float id?;
    boolean name?;
    int...;    
|};

function functionWithIncludedRecordParam11(int id, *OthRecord othRec, *Options options) {
    string? name = options?.name;
}

function testfunctionWithIncludedRecordParam8() {
    functionWithIncludedRecordParam11(1, name = "bar", options = {name: "baz"});
}

function foo(*int a) {
}

public type KeyValues record {|int[]...;|};

function bal(*KeyValues baz) returns string {
    return "";
}

function testfunctionWithIncludedRecordParam9() {
    string result = bal(username = 2);
}

function functionWithIncludedRecordParam12(*Options options, int a) {
    string? name = options?.name;
}

function functionWithIncludedRecordParam13(*Options options, int a = 10) {
    string? name = options?.name;
}

function functionWithIncludedRecordParam14(int a, *Options options, int c = 10) {
    string? name = options?.name;
}

function functionWithIncludedRecordParam15(int a, int b = 12, *Options options, int c = 10) {
    string? name = options?.name;
}

type Record1 record {
    int id;
    string name;
};

function fn1(*Record1 record1) {

}

type Record2 record {|
    int id?;
    string name?;
|};

function fn2(*Record2 record2) {

}

function functionWithIncludedRecordParam16() {
    fn1(id = 2, record1 = {id: 2, name: ""}); // error
    fn1(record1 = {id: 2, name: ""}, id = 2); // error
    fn1(record1 = {id: 2, name: ""}, name = "2"); // error
    fn1({id: 2, name: ""}, name = "2"); // error
    fn1({id: 2, name: "", addr: ""}, name = "2"); // error
    
    fn2(id = 2, record2 = {name: ""}); // error
    fn2(record2 = {name: ""}, id = 2); // error
    fn2(record2 = {name: ""}, name = ""); // error
}

type Record3 record {|
    int idNew?;
    string nameNew?;
|};

function fn3(*Record2 record2, *Record3 record3) {
    
}

function functionWithIncludedRecordParam17() {
    fn3(record2 = {id: 2, name: ""}, record3 = {idNew: 3, nameNew: ""});
    fn3(record2 = {id: 2, name: ""}, idNew = 21, record3 = {idNew: 3, nameNew: ""}); // error
    fn3(record2 = {id: 2, name: ""}, idNew = 21, record3 = {nameNew: ""}); // error
    fn3({id: 2, name: ""}, {nameNew: ""}, idNew = 21); // error
    fn3({name: ""}, {nameNew: ""}, idNew = 21, id = 2); // error
    fn3(id = 2, record2 = {id: 2, name: ""}, record3 = {idNew: 3, nameNew: ""}); // error
    fn3(idNew = 2, record2 = {id: 2, name: ""}, record3 = {idNew: 3, nameNew: ""}); // error
    fn3(idNew = 2, id = 3, record2 = {id: 2, name: ""}, record3 = {idNew: 3, nameNew: ""}); // error
}

type Record4 record {|
    int idNew?;
    string nameNew?;
    Record3 rec?;
|};

function fn4(*Record4 record4) {
    
}

function functionWithIncludedRecordParam18() {
    fn4(rec = {}, record4 = {}); // error
}

type Coo int;

function fn(*Boo f) {
}

class MyClass {
    function fn(*Boo f) {
    }

    function fn2(*Coo f) {
    }
}

function testInvokingFunctionContainingIncludedRecordParamOfTypeOtherThanRecordOrUndefinedUsingNamedArg() {
     // No errors will be logged for bellow lines, but we are testing the call path
    fn(f = {});
    MyClass obj = new;
    _ = obj.fn(f = {});
    _ = obj.fn2(f = {});
}
