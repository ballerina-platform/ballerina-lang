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

map<string> countries = {"lk":"Sri Lanka", "us":"USA", "uk":"United Kingdom"};

function testLength() returns int {
    return countries.length();
}

function testGet(string key) returns string {
    return countries.get(key);
}

function testEntries() returns map<[string, string]> {
    return countries.entries();
}

function testRemove(string key) returns [string, map<string>] {
    map<string> countriesDup = {"lk":"Sri Lanka", "us":"USA", "uk":"United Kingdom"};
    return [countriesDup.remove(key), countriesDup];
}

function testRemoveAll() returns map<string> {
    map<string> countriesDup = {"lk":"Sri Lanka", "us":"USA", "uk":"United Kingdom"};
    countriesDup.removeAll();
    return countriesDup;
}

function testRemoveIfHasKey() {
    map<string> student = {id:"1", name:"Andrew", country:"Sri Lanka", city:"Colombo"};
    string? s = student.removeIfHasKey("name");
    if (s is ()) {
         panic error("Returned value should be an string.");
    }
    if (<string> s != "Andrew") {
         panic error("Returned value should equals 'Andrew'.");
    }

    string? age = student.removeIfHasKey("age");
    if !(age is ()) {
        panic error("Returned value should be nil.");
    }
}

function testHasKey(string key) returns boolean {
    return countries.hasKey(key);
}

function testKeys() returns string[] {
    return countries.keys();
}

function testMap() returns map<float> {
    map<int> m = {"1":1, "2":2, "3":3};
    return m.'map(function (int v) returns float { return v * 5.5; });
}

function testForEach() returns string {
    string result = "";
    countries.forEach(function (string val) {
        result += val;
    });
    return result;
}

function testFilter() returns map<decimal> {
    map<decimal> m = {"1":12.34, "2":3.45, "3":7.89, "4":21.2};
    map<decimal> filteredM = m.filter(function (decimal d) returns boolean { return d > 10; });
    return filteredM;
}

function testReduce() returns float {
    map<int> grades = {"maths":79, "physics":84, "chemistry":72, "ict":87};
    float avg = grades.reduce(function (float accum, int val) returns float {
        return accum + <float>val / grades.length();
    }, 0.0);
    return avg;
}

function testBasicToArray() {
    map<int> ints = {"one": 1, "two": 2, "three": 3, "four": 4, "five": 5};
    int[] intArr = ints.toArray();

    map<float> floats = {"one": 1.1, "two": 2.2, "three": 3.3, "four": 4.4, "five": 5.5};
    float[] floatArr = floats.toArray();

    map<decimal> decimals = {"one": 1.1, "two": 2.2, "three": 3.3, "four": 4.4, "five": 5.5};
    decimal[] decimalArr = decimals.toArray();

    map<string> strings = {"a": "A", "b": "B", "c": "C"};
    string[] stringArr = strings.toArray();

    map<boolean> booleans = {"a": true, "b": false, "c": true};
    boolean[] booleanArr = booleans.toArray();

    map<map<int>> maps = {"a": {"one": 1, "two": 2}, "b": {"three": 3, "four": 4}};
    map<int>[] mapArr = maps.toArray();

    assert(<int[]>[1, 2, 3, 4, 5], intArr);
    assert(<float[]>[1.1, 2.2, 3.3, 4.4, 5.5], floatArr);
    assert(<decimal[]>[1.1, 2.2, 3.3, 4.4, 5.5], decimalArr);
    assert(<string[]>["A", "B", "C"], stringArr);
    assert(<boolean[]>[true, false, true], booleanArr);
    assert(<map<int>[]>[{"one": 1, "two": 2}, {"three": 3, "four": 4}], mapArr);

    // Test modifying the resultant array
    intArr[1] = 22;
    intArr[9] = 10;
    assert(<int[]>[1, 22, 3, 4, 5, 0, 0, 0, 0, 10], intArr);

    floatArr[1] = 22.2;
    floatArr[7] = 7.7;
    assert(<float[]>[1.1, 22.2, 3.3, 4.4, 5.5, 0.0, 0.0, 7.7], floatArr);

    decimalArr[2] = 33.3;
    decimalArr[6] = 6.6;
    assert(<decimal[]>[1.1, 2.2, 33.3, 4.4, 5.5, 0.0, 6.6], decimalArr);

    stringArr[0] = "Z";
    _ = stringArr.remove(1);
    assert(<string[]>["Z", "C"], stringArr);

    booleanArr[1] = !booleanArr[1];
    booleanArr.push(true);
    assert(<boolean[]>[true, true, true, true], booleanArr);

    mapArr[0]["one"] = 10;
    assert(<map<int>[]>[{"one": 10, "two": 2}, {"three": 3, "four": 4}], mapArr);
}

function testLargeMapToArray() {
    var fn = function () returns int[] {
        int[] arr = [];
        foreach var i in 0...999 {
            arr[i] = i + 1;
        }
        return arr;
    };

    assert(fn(), getLargeMap().toArray());
}

function getLargeMap() returns map<int> {
    map<int> m = {};
    foreach var i in 1...1000 {
        m[i.toString()] = i;
    }
    return m;
}

class Person {
    string name;

    function init(string n) {
        self.name = n;
    }

    function getName() returns string => self.name;
}

function testMapOfUnionToArray() {
    map<int|string|Person> m = {"i": 10, "s": "foo", "p": new Person("Pubudu")};
    (int|string|Person)[] arr = m.toArray();

    assert(10, <anydata>arr[0]);
    assert("foo", <anydata>arr[1]);
    assertSameRef(m["p"], arr[2]);
}

type Foo record {|
    string name;
    int age;
    float weight;
    decimal height;
    boolean isStudent;
|};

function testRecordToArray() {
    Foo foo = {
        name: "John Doe",
        age: 25,
        weight: 65.5,
        height: 172.3,
        isStudent: true
    };

    var arr = foo.toArray();

    assert(<(string|int|float|decimal|boolean)[]>["John Doe", 25, 65.5, 172.3d, true], arr);
}

type OpenFoo record {
    string name;
    int age;
    float weight;
    decimal height;
    boolean isStudent;
};

function testOpenRecordToArray() {
    OpenFoo foo = {
        name: "John Doe",
        age: 25,
        weight: 65.5,
        height: 172.3,
        isStudent: true,
        "location": "Sri Lanka",
        "postalCode": 12500
    };

    var arr = foo.toArray();

    assert(<(string|int|float|decimal|boolean)[]>["John Doe", 25, 65.5, 172.3d, true, "Sri Lanka", 12500], arr);
}

type Bar record {|
    byte a;
    byte b;
    byte...;
|};

function testRecordWithSameTypeFieldsToArray() {
    Bar bar = {a: 10, b: 20, "c": 30, "d": 40};
    byte[] arr = bar.toArray();
    byte[] exp = [10, 20, 30, 40];
    assert(exp, arr);
}


// Util functions

function assert(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }
    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                        + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error("{AssertionError}", message = msg);
}

function assertSameRef(any expected, any actual) {
    if (expected === actual) {
        return;
    }
    typedesc<any> expT = typeof expected;
    typedesc<any> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                        + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error("{AssertionError}", message = msg);
}


function testAsyncFpArgsWithMaps() returns [int, map<int>] {
    map<int> marks = {a: 12, b: 34, c: 76};
    map<int> newMarks = marks.map(function (int entry) returns int {
        future<int> f1 = start getRandomNumber(entry);
        return wait f1;
    });

    map<int> passMarks = newMarks.filter(function (int entry) returns boolean {
        future<int> f1 = start getRandomNumber(entry);
        int n = wait f1;
        return n > 35;
    });

    int total = 0;
    passMarks.forEach(function (int entry) {
        future<int> f1 = start getRandomNumber(entry);
        int n = wait f1;
        total = total + n;

    });

    int finalResult = passMarks.reduce(function (int sum, int entry) returns int {
        future<int> f1 = start getRandomNumber(entry);
        int n = wait f1;
        return sum + n;

    }, 0);
    return [finalResult, passMarks];
}

function getRandomNumber(int i) returns int {
    return i + 2;
}
