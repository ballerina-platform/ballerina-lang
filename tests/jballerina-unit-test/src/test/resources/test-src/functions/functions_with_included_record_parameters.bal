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

public type NewPerson record {
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

type Foo5 record {
    never a?;
    int b;
};

public type KeyValues record {|int[]...;|};
public type Pairs record {|float[]...;|};

function functionOfFunctionTypedParamWithIncludedRecordParam(*NewPerson person) returns string {
    return person.firstName + " " + person.secondName;
}

function functionWithIncludedRecordParamAfterDefaultParam(string middleName = " ", *NewPerson person) returns string {
    return person.firstName + middleName + person.secondName;
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

function functionWithIncludedRecordParamAfterDefaultParam2(int a = 10, int b = 15, *Foo foo) returns int {
    return a + b + <int> foo["value"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam8(int c, int d, *Foo4 foo, *Bar bar) returns int {
    anydata value = foo["value"];
    return bar.a + bar.b + c+ d + <int> value;
}

function functionWithIncludedRecordParamAfterDefaultParam3(int c, int d = 10, *Foo4 foo, *Bar bar) returns int {
    anydata value = foo["value"];
    return bar.a + bar.b + c+ d + <int> value;
}

function functionOfFunctionTypedParamWithIncludedRecordParam9(string message, string level = "abc", *KVPairs pairs) returns string {
    return message + level + <string> pairs["path"] + <string> pairs["filename"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam10(*Address address) returns string {
    return address.country;
}

function functionOfFunctionTypedParamWithIncludedRecordParam11(*Foo2 foo2) {
}

function functionOfFunctionTypedParamWithIncludedRecordParam12(string email, int tel, *NewStudent student, *Grades grades) returns string {
    return email;
}

function functionOfFunctionTypedParamWithIncludedRecordParam13(*Grades grades) returns int {
    return grades.chemistry + grades.physics + grades.maths;
}

function functionOfFunctionTypedParamWithIncludedRecordParam14(int a, int b = 5, *Foo3 foo, *Foo4 foo2) returns int {
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

function functionOfFunctionTypedParamWithIncludedRecordParam21(*KeyValues values) returns int[] {
    return <int[]>values["a"];
}

function functionOfFunctionTypedParamWithIncludedRecordParam22(*Pairs values) returns float[] {
    return <float[]>values["a"];
}

function testFuctionWithIncludedRecordParameters() {
    string fullName = functionOfFunctionTypedParamWithIncludedRecordParam(firstName = "chiran", secondName = "sachintha");
    string fullName2 = functionWithIncludedRecordParamAfterDefaultParam(firstName = "chiran", secondName = "sachintha");
    string fullName3 = functionWithIncludedRecordParamAfterDefaultParam(middleName = " Peter ", firstName = "Steven", secondName = "Smith");

    assertEquality("chiran sachintha", fullName);
    assertEquality("chiran sachintha", fullName2);
    assertEquality("Steven Peter Smith", fullName3);
}

function testFuctionWithIncludedRecordParameters2() {
    [string, int] details = functionOfFunctionTypedParamWithIncludedRecordParam2(firstName = "chiran", secondName = "sachintha", age = 24);
    assertEquality("chiran sachintha", details[0]);
    assertEquality(24, details[1]);

}

function testFuctionWithIncludedRecordParameters3() {
    string Address = functionOfFunctionTypedParamWithIncludedRecordParam3();
    assertEquality("Sri Lanka", Address);
}

function testFuctionWithIncludedRecordParameters4() {
    anydata valueType = functionOfFunctionTypedParamWithIncludedRecordParam4(a = 3, b = 4, value = "Integer");
    assertEquality("Integer", valueType);
}

function testFuctionWithIncludedRecordParameters5() {
    [anydata, anydata, anydata, anydata, anydata] details = functionOfFunctionTypedParamWithIncludedRecordParam5(a = 30, b = 400.0, value = "Integer", isCorrect = true, path = "c/usr/filename");
    assertEquality("Integer", details[0]);
    assertEquality(30, details[1]);
    assertEquality(400.0, details[2]);
    assertEquality(true, details[3]);
    assertEquality("c/usr/filename", details[4]);
}

function testFuctionWithIncludedRecordParameters6() {
    anydata a = ();
    anydata valueType = functionOfFunctionTypedParamWithIncludedRecordParam6(a = 3, b = 4);
    assertEquality(a, valueType);
}

function testFuctionWithIncludedRecordParameters7() {
    anydata value = functionOfFunctionTypedParamWithIncludedRecordParam7(a = 3, b = 4, value = 13);
    anydata value2 = functionWithIncludedRecordParamAfterDefaultParam2(value = 5);
    anydata value3 = functionWithIncludedRecordParamAfterDefaultParam2(a = 5, b = 5, value = 5);
    anydata value4 = functionWithIncludedRecordParamAfterDefaultParam2(b = 15,value = 5, a = 50);
    assertEquality(20, value);
    assertEquality(30, value2);
    assertEquality(15, value3);
    assertEquality(70, value4);
}

function testFuctionWithIncludedRecordParameters8() {
    anydata value = functionOfFunctionTypedParamWithIncludedRecordParam8(a = 5, b = 3, c = 3, d = 4, value = 10);
    anydata value2 = functionWithIncludedRecordParamAfterDefaultParam3(a = 5, b = 3, c = 3, value = 10);
    anydata value3 = functionWithIncludedRecordParamAfterDefaultParam3(a = 5, b = 3, c = 3, value = 10, d = 4);
    assertEquality(25, value);
    assertEquality(31, value2);
    assertEquality(25, value3);
}

function testFuctionWithIncludedRecordParameters9() {
    anydata path = functionOfFunctionTypedParamWithIncludedRecordParam9("x", "y", filename = "xyz.abc", path = "a/b/c");
    anydata path2 = functionOfFunctionTypedParamWithIncludedRecordParam9("x", filename = "xyz.abc", path = "a/b/c");
    anydata path3 = functionOfFunctionTypedParamWithIncludedRecordParam9("x", filename = "xyz.abc", path = "a/b/c", level = "y");

    assertEquality("xya/b/cxyz.abc", path);
    assertEquality("xabca/b/cxyz.abc", path2);
    assertEquality("xya/b/cxyz.abc", path3);
}

function testFuctionWithIncludedRecordParameters10() {
    string country = functionOfFunctionTypedParamWithIncludedRecordParam10(country = "Brazil");
    assertEquality("Brazil", country);
}

function testFuctionWithIncludedRecordParameters11() {
    functionOfFunctionTypedParamWithIncludedRecordParam11();
}

function testFuctionWithIncludedRecordParameters12() {
    string email = functionOfFunctionTypedParamWithIncludedRecordParam12(firstName = "chiran", secondName = "sachintha", age = 24, email = "chirans", tel = 785, physics = 75, maths = 80, chemistry = 85);
    assertEquality("chirans", email);
}

function testFuctionWithIncludedRecordParameters13() {
    int totalMarks = functionOfFunctionTypedParamWithIncludedRecordParam13(maths = 80, physics = 75, chemistry = 85);
    assertEquality(240, totalMarks);
}

function testFuctionWithIncludedRecordParameters14() {
    int sum = functionOfFunctionTypedParamWithIncludedRecordParam14(10, 25);
    int sum2 = functionOfFunctionTypedParamWithIncludedRecordParam14(5);

    assertEquality(35, sum);
    assertEquality(10, sum2);
}

function testFuctionWithIncludedRecordParameters15() {
    int sum = functionOfFunctionTypedParamWithIncludedRecordParam15(a = 10, b = 15, c = 5);
    assertEquality(20, sum);
}

function testFuctionWithIncludedRecordParameters16() {
    anydata anyVal = ();
    anydata val = functionOfFunctionTypedParamWithIncludedRecordParam16(a = 10, b = 15, c = 5);
    assertEquality(anyVal, val);
}

function testFuctionWithIncludedRecordParameters17() {
    anydata anyVal = ();
    Foo2 foo = {
        b : "ballerina"
    };
    anydata val = functionOfFunctionTypedParamWithIncludedRecordParam17(foo);
    assertEquality("ballerina", val);
}

function testFuctionWithIncludedRecordParameters18() {
    int sum = functionOfFunctionTypedParamWithIncludedRecordParam18(a = 10, b = 15, c = 5, d = 20);
    assertEquality(50, sum);
}

function testFuctionWithIncludedRecordParameters19() {
    int val = functionOfFunctionTypedParamWithIncludedRecordParam19(c = 10);
    assertEquality(10, val);
}

function testFuctionWithIncludedRecordParameters20() {
    int val = functionOfFunctionTypedParamWithIncludedRecordParam20(c = 5);
    assertEquality(5, val);
}

function testFuctionWithIncludedRecordParameters21() {
    int[] x = [1, 2];
    int[] val = functionOfFunctionTypedParamWithIncludedRecordParam21(a = [1, 2]);
    assertEquality(x, val);
}

function testFuctionWithIncludedRecordParameters22() {
    float[] x = [1, 2];
    float[] val = functionOfFunctionTypedParamWithIncludedRecordParam22(a = [1, 2]);
    assertEquality(x, val);
}

function functionWithIncludedRecordParamWhichHasNeverField(int a, *Foo5 foo) returns int {
    return a + foo.b;
}

function testFuctionWithIncludedRecordParameters24() {
    assertEquality(functionWithIncludedRecordParamWhichHasNeverField(a = 2, foo = {b: 4}), 6);
}

public class TestClass {
    public string firstName;
    public string secondName;
    public function init(*NewPerson p) {
        self.firstName = p.firstName;
        self.secondName = p.secondName;
    }
}

function testFuctionWithIncludedRecordParameters23() {
    TestClass tclass = new (firstName = "chiran", secondName = "sachintha");
    string fullName = tclass.firstName + " " + tclass.secondName;
    assertEquality("chiran sachintha", fullName);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
