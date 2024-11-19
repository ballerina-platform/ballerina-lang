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
    _ = start add(5, 2);
    future<int> f1 = start add(5, 2);
    future<boolean> f2 = start status();
    future<string> f3 = start concat("foo");

    int result1 = checkpanic wait f1;
    boolean result2 = checkpanic wait f2;
    string result3 = checkpanic wait f3;

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

    xml x = checkpanic wait a;
    json y = checkpanic wait b;

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
    
    int[] x = checkpanic wait a;
    
    assertEquality(intArray(), x);
}

function testArrayTypesWithoutFutureConstraint() {
    future a = start intArray();

    any|error x = wait a;

    assertEquality(intArray(), x);
}

function testRecordTypes() {
    future<Person> a = start getNewPerson();
    
    Person x = checkpanic wait a;
    
    assertEquality(getNewPerson(), x);
}

function testRecordTypesWithoutFutureConstraint() {
    future a = start getNewPerson();

    any|error x = wait a;

    assertEquality(getNewPerson(), x);
}

function testObjectTypes() {
    future<PersonA> a = start getPersonAObject();

    PersonA x = checkpanic wait a;
    string name = x.getName();
    
    assertEquality("sample name", name);
}

function testObjectTypesWithoutFutureConstraint() {
    future a = start getPersonAObject();

    any x = checkpanic wait a;
    PersonA personA = <PersonA>x;

    assertEquality(getPersonAObject().age, personA.age);
    assertEquality(getPersonAObject().name, personA.name);
}

function testCustomErrorFuture() {
    future<error> te = start getError();

    error x = wait te;

    assertEquality("SimpleErrorType", x.message());
}

function testCustomErrorFutureWithoutConstraint() {
    future te = start getError();

    any|error x = wait te;
    string str = (<error>x).toString();

    assertEquality(getError().toString(), str);
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

function testFutureTyping() {
    Foo foo1 = {i: 1, "j": "hello"};
    Foo foo2 = {i: 2, "j": "world"};

    future<Foo> f1 = start getFoo();
    any a1 = f1;
    assertEquality(true, a1 is future<Foo>);
    assertEquality(true, a1 is future<record {}>);
    assertEquality(true, a1 is future<int|Foo>);
    assertEquality(true, a1 is future<record {}|int[]>);
    assertEquality(false, a1 is future<Bar>);

    future<Foo|Bar> f2 = start getFoo();
    future<Foo|Bar|int[]> f3 = f2;
    any a2 = f3;
    assertEquality(true, a2 is future<Foo>);
    assertEquality(true, a2 is future<record {}>);
    assertEquality(true, a2 is future<int|Foo>);
    assertEquality(true, a2 is future<record {}|int[]>);
    assertEquality(false, a2 is future<Bar>);
}

type Foo record {
    int i;
};

type Bar record {|
    float f;
|};

function getFoo() returns Foo => {i: 1, "j": "hello"};

function getFooOrBar() returns Foo|Bar => {i: 2, "j": "world"};


const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if (expected is anydata && actual is anydata && expected == actual) {
        return;
    }

    if (expected === actual) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                 message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
