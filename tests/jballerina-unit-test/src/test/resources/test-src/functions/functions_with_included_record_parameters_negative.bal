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
