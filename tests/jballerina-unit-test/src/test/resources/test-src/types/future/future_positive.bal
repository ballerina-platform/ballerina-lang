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

function testBasicTypes() {
    future<int> f1 = start add(5, 2);
    future<boolean> f2 = start status();
    future<string> f3 = start concat("foo");

    int result1 = wait f1;
    boolean result2 = wait f2;
    string result3 = wait f3;

    assertEquality(7, result1);
    assertEquality(true, result2);
    assertEquality("hello foo", result3);
}

function testBasicTypesWithoutFutureConstraint() {
    future f1 = start add(5, 2);
    future f2 = start status();
    future f3 = start concat("foo");

    any|error result1 = wait f1;
    any|error result2 = wait f2;
    any|error result3 = wait f3;

    assertEquality(7, result1);
    assertEquality(true, result2);
    assertEquality("hello foo", result3);
}

function testRefTypes() {
    future<xml> a = start xmlFile();
    future<json> b = start getJson();

    xml x = wait a;
    json y = wait b;

    assertEquality(xml `aaa`, x);
    assertEquality(5, y);
}

function testRefTypesWithoutFutureConstraint() {
    future a = start xmlFile();
    future b = start getJson();

    any|error x = wait a;
    any|error y = wait b;

    assertEquality(xml `aaa`, x);
    assertEquality(5, y);
}

function testArrayTypes() {
    future<int[]> a = start intArray();
    
    int[] x = wait a;
    
    assertEquality(intArray(), x);
}

function testArrayTypesWithoutFutureConstraint() {
    future a = start intArray();
    
    any|error x = wait a;
    
    assertEquality(intArray(), x);
}

function testRecordTypes() {
    future<Person> a = start getNewPerson();
    
    Person x = wait a;
    
    assertEquality(getNewPerson(), x);
}

function testRecordTypesWithoutFutureConstraint() {
    future a = start getNewPerson();
    
    any|error x = wait a;
    
    assertEquality(getNewPerson(), x);
}

function testObjectTypes() {
    future<PersonA> a = start getPersonAObject();

    PersonA x = wait a;
    string name = x.getName();
    
    assertEquality("sample name", name);
}

function testObjectTypesWithoutFutureConstraint() {
    future a = start getPersonAObject();

    any|error x = wait a;
    
    assertEquality("object PersonA", x.toString());
}

function testCustomErrorFuture() {
    future<error> te = start getError();

    error x = wait te;

    assertEquality("SimpleErrorType", x.message());
}

function testCustomErrorFutureWithoutConstraint() {
    future te = start getError();

    any|error x = wait te;

    assertEquality(getError().toString(), x.toString());
}

function add(int i, int j) returns int {
    int k = i + j;
    return k;
}

function concat(string name) returns string {
    return "hello " + name;
}

function status() returns boolean {
    return true;
}

function xmlFile() returns xml {
    xml x1 = xml `aaa`;
    return x1;
}

function getJson() returns json {
    json j = 5;
    return j;
}

function intArray() returns int[] {
    int[] j = [1, 2, 3];
    return j;
}

public class PersonA {
    public int age = 10;
    public string name = "sample name";

    public function getName() returns string {
        return self.name;
    }
}

public function getPersonAObject() returns PersonA {
    return new PersonA();
}

type Person record {
    string name = "John";
    int age = 30;
    string spouse?;
};

function getNewPerson() returns Person {
	Person p = {name: "Jane", age: 25, spouse: "John", "gender": "female"};
	return p;
}

function getError() returns error {
    error simpleError = error("SimpleErrorType", message = "Simple error occurred");
    return simpleError;
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if (expected is anydata && actual is anydata && expected == actual) {
        return;
    }

    if (expected === actual) {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                 message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
