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
    string city = "Matara";
    string country = "Sri Lanka";
|};

type Grades record {|
    int maths;
    int physics;
    int chemistry;
|};

type Foo record {
    never a?;
    never b?;
};

type Foo2 record {
    int a?;
    string b?;
    string c?;
    string d?;
};

type Bar record {|
    int a;
    int b;
|};

type Bar2 record {|
    int c;
    int d?;
|};

type Baz record {
    
};

type KVPairs record {
	never message?;
	never level?; 
};

type Val record {|
    never a?;
    never b?;
    int...;
|};

type Foo3 record {
    never a?;
    never b?;
};

type Foo4 record {
    never a?;
    never b?;
    never c?;
    never d?;
};

function functionOfFunctionTypedParamWithIncludedRecordParam(*NewPerson person) returns string {
    return person.firstName + " " + person.secondName;
}

function functionOfFunctionTypedParamWithIncludedRecordParam2(*NewStudent person) returns [string, int] {
    return [person.firstName + " " + person.secondName, person.age];
}

function functionOfFunctionTypedParamWithIncludedRecordParam3(*Address address) returns string {
    return address.country;
}

function functionOfFunctionTypedParamWithIncludedRecordParam4(*Foo foo, *Bar bar) returns anydata {
    return foo["value"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam5(*Baz baz) returns [anydata, anydata, anydata, anydata, anydata] {
    return [baz["value"], baz["a"], baz["b"], baz["isCorrect"], baz["path"]];
}

function functionOfFunctionTypedParamWithIncludedRecordParam6(*Foo foo, *Bar bar) returns anydata {
    return foo["value"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam7(int a, int b, *Foo foo) returns int {
    return a + b + <int> foo["value"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam8(int c, int d, *Foo4 foo, *Bar bar) returns int {
    anydata value = foo["value"];
    return bar.a + bar.b + c+ d + <int> value;
}

function functionOfFunctionTypedParamWithIncludedRecordParam9(string message, string level, *KVPairs pairs) returns string {
    return message + level + <string> pairs["path"] + <string> pairs["filename"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam10(*Address address) returns string {
    return address.country;
}

function functionOfFunctionTypedParamWithIncludedRecordParam11(*Foo2 foo2) {
}

function functionOfFunctionTypedParamWithIncludedRecordParam12(*NewStudent student, *Grades grades, string email, int tel) returns string {
    return email;
}

function functionOfFunctionTypedParamWithIncludedRecordParam13(*Grades grades) returns int {
    return grades.chemistry + grades.physics + grades.maths;
}

function functionOfFunctionTypedParamWithIncludedRecordParam14(int a, int b, *Foo3 foo, *Foo4 foo2) returns int {
    return a + b;
}

function functionOfFunctionTypedParamWithIncludedRecordParam15(*Bar2 bar2, *Bar bar) returns int {
    return bar2.c+ <int> bar.b;
}

function functionOfFunctionTypedParamWithIncludedRecordParam16(*Bar2 bar2, *Bar bar) returns anydata {
    return bar2["d"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam17(*Foo2 foo2) returns anydata {
    return foo2["b"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam18(int a, int b, *Val val) returns int {
    return a + b + <int> val["c"] + <int> val["d"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam19(*Foo foo) returns int {
    return <int> foo["c"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam20(*Bar2 bar) returns int {
    return <int> bar.c;
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam() {
    string fullName = functionOfFunctionTypedParamWithIncludedRecordParam(firstName = "chiran", secondName = "sachintha");
    assertEquality("chiran sachintha", fullName);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam2() {
    [string, int] details = functionOfFunctionTypedParamWithIncludedRecordParam2(firstName = "chiran", secondName = "sachintha", age = 24);
    assertEquality("chiran sachintha", details[0]);
    assertEquality(24, details[1]);

}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam3() {
    string Address = functionOfFunctionTypedParamWithIncludedRecordParam3();
    assertEquality("Sri Lanka", Address);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam4() {
    anydata valueType = functionOfFunctionTypedParamWithIncludedRecordParam4(a = 3, b = 4, value = "Integer");
    assertEquality("Integer", valueType);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam5() {
    [anydata, anydata, anydata, anydata, anydata] details = functionOfFunctionTypedParamWithIncludedRecordParam5(a = 30, b = 400.0, value = "Integer", isCorrect = true, path = "c/usr/filename");
    assertEquality("Integer", details[0]);
    assertEquality(30, details[1]);
    assertEquality(400.0, details[2]);
    assertEquality(true, details[3]);
    assertEquality("c/usr/filename", details[4]);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam6() {
    anydata a = ();
    anydata valueType = functionOfFunctionTypedParamWithIncludedRecordParam6(a = 3, b = 4);
    assertEquality(a, valueType);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam7() {
    anydata value = functionOfFunctionTypedParamWithIncludedRecordParam7(a = 3, b = 4, value = 13);
    assertEquality(20, value);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam8() {
    anydata value = functionOfFunctionTypedParamWithIncludedRecordParam8(a = 5, b = 3, c = 3, d = 4, value = 10);
    assertEquality(25, value);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam9() {
    anydata path = functionOfFunctionTypedParamWithIncludedRecordParam9("x", "y", filename = "xyz.abc", path = "a/b/c");
    assertEquality("xya/b/cxyz.abc", path);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam10() {
    string country = functionOfFunctionTypedParamWithIncludedRecordParam10(country = "Brazil");
    assertEquality("Brazil", country);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam11() {
    functionOfFunctionTypedParamWithIncludedRecordParam11();
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam12() {
    string email = functionOfFunctionTypedParamWithIncludedRecordParam12(firstName = "chiran", secondName = "sachintha", age = 24, email = "chirans", tel = 785, physics = 75, maths = 80, chemistry = 85);
    assertEquality("chirans", email);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam13() {
    int totalMarks = functionOfFunctionTypedParamWithIncludedRecordParam13(maths = 80, physics = 75, chemistry = 85);
    assertEquality(240, totalMarks);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam14() {
    int sum = functionOfFunctionTypedParamWithIncludedRecordParam14(10, 25);
    assertEquality(35, sum);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam15() {
    int sum = functionOfFunctionTypedParamWithIncludedRecordParam15(a = 10, b = 15, c = 5);
    assertEquality(20, sum);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam16() {
    anydata anyVal = ();
    anydata val = functionOfFunctionTypedParamWithIncludedRecordParam16(a = 10, b = 15, c = 5);
    assertEquality(anyVal, val);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam17() {
    anydata anyVal = ();
    Foo2 foo = {
        b : "ballerina"
    };
    anydata val = functionOfFunctionTypedParamWithIncludedRecordParam17(foo);
    assertEquality("ballerina", val);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam18() {
    int sum = functionOfFunctionTypedParamWithIncludedRecordParam18(a = 10, b = 15, c = 5, d = 20);
    assertEquality(50, sum);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam19() {
    int val = functionOfFunctionTypedParamWithIncludedRecordParam19(c = 10);
    assertEquality(10, val);
}

function testFunctionOfFunctionTypedParamWithIncludedRecordParam20() {
    int val = functionOfFunctionTypedParamWithIncludedRecordParam20(c = 5);
    assertEquality(5, val);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
