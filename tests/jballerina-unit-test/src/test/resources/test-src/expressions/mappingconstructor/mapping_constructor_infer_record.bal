// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const ASSERTION_ERROR_REASON = "AssertionError";

type Person record {|
   string firstName;
   string lastName;
   int age;
|};

type PersonWithoutAge record {|
   string fn;
   string ln;
|};

public type IntAndStrings record {|
   int i;
   string...;
|};

Person p1 = {firstName: "Alex", lastName: "George", age: 23};
Person p2 = {firstName: "Ranjan", lastName: "Fonseka", age: 30};
Person p3 = {firstName: "John", lastName: "David", age: 33};

function testRecordInferringForMappingConstructorWithoutRestField() {
    Person[] personList = [p1, p2, p3];

    var outputList =
            from var person in personList
            select {
                   fn: person.firstName,
                   "ln": person.lastName
            };

    PersonWithoutAge[] arr = outputList;

    assertEquality(p1.firstName, arr[0].fn);
    assertEquality(p1.lastName, arr[0].ln);
    assertEquality(p2.firstName, arr[1].fn);
    assertEquality(p2.lastName, arr[1].ln);
    assertEquality(p3.firstName, arr[2].fn);
    assertEquality(p3.lastName, arr[2].ln);
}

public function testRecordInferringForMappingConstructorWithRestField1() returns IntAndStrings[] {
    Person[] personList = [p1, p2, p3];
    string key = "str";

    var outputList =
            from var person in personList
            select {
                   i: person.age,
                   [key]: person.firstName
            };

    return outputList;
}

public function testRecordInferringForMappingConstructorWithRestField2() {
    Person[] personList = [p1, p2, p3];
    string key = "str";

    var arr =
            from var person in personList
            select {
                   i: person.age,
                   [key]: person.firstName
            };

    assertEquality(p1.age, arr[0].i);
    assertEquality(p1.firstName, arr[0]["str"]);
    assertEquality(p2.age, arr[1].i);
    assertEquality(p2.firstName, arr[1]["str"]);
    assertEquality(p3.age, arr[2].i);
    assertEquality(p3.firstName, arr[2]["str"]);
}

public function testRecordInferringForMappingConstructorWithRestField3() {
    Person[] personList = [p1, p2, p3];
    string key = "str";
    string key2 = "str2";
    boolean b = false;

    var arr =
            from var person in personList
            select {
                   i: person.age,
                   "j": "hello",
                   [key]: person.firstName,
                   [key2]: b
            };

    var rec = arr[0];
    any a = rec;

    assertEquality(a is record {|int i; string j; string|boolean...;|}, true);
    assertEquality(p1.age, rec.i);
    assertEquality("hello", rec.j);
    assertEquality(p1.firstName, rec[key]);
    assertEquality(false, rec[key2]);
}

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
