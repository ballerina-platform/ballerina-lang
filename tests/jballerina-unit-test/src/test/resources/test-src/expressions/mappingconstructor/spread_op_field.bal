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

type Foo record {|
    string s;
    int i;
    float f;
    boolean|float...;
|};

type Bar record {|
    string s;
    int i;
|};

public function testMapRefAsSpreadOp() {
    map<float> m1 = {q: 1.0, w: 2.0};
    map<boolean|float> m2 = {...m1};

    assertEquality(2, m2.length());
    assertEquality(1.0, m2["q"]);
    assertEquality(2.0, m2["w"]);
}

public function testMapValueViaFuncAsSpreadOp() {
    map<int> m1 = {q: 1, w: 2};
    function () returns map<int> func = () => m1;

    map<boolean|int> m2 = {...func()};

    assertEquality(2, m2.length());
    assertEquality(1, m2["q"]);
    assertEquality(2, m2["w"]);
}

public function testRecordRefAsSpreadOp() {
    Bar b = {s: "str", i: 1};
    Foo f = {...b, f: 123.4};

    assertEquality(3, f.length());
    assertEquality("str", f["s"]);
    assertEquality(1, f["i"]);
    assertEquality(123.4, f["f"]);
}

type Quux record {
    int j;
    never i?;
};

public function testRecordRefWithNeverType() {
    Quux b = {j: 2, "k": 3};
    map<anydata> m = {i: 0, ...b};

    assertEquality(3, m.length());
    assertEquality(0, m["i"]);
}

public function testRecordValueViaFuncAsSpreadOp() {
    var fn = function () returns Bar => {s: "str", i: 1};
    Foo f = {...fn(), f: 123.4};

    assertEquality(3, f.length());
    assertEquality("str", f["s"]);
    assertEquality(1, f["i"]);
    assertEquality(123.4, f["f"]);
}

const map<float> constFloatMap = {z: 1.0, y: 2.0};

const map<float> constFloatMap2 = {...constFloatMap};

public function testSpreadOpInConstMap() {
    assertEquality(2, constFloatMap2.length());
    assertEquality(2.0, constFloatMap2["y"]);
    assertEquality(1.0, constFloatMap2["z"]);
}

map<int> globalIntMap = {a: 1, b: 2};

function getStringMap() returns map<string> {
    return {
        l: "el",
        m: "em",
        n: "en"
    };
}

map<int> globalMapInt = {...globalIntMap};
map<float> globalMapFloat = {...constFloatMap2};
map<string> globalMapString = {...getStringMap()};

function testSpreadOpInGlobalMap() {
    assertEquality(2, globalMapInt.length());
    assertEquality(1, globalMapInt["a"]);
    assertEquality(2, globalMapInt["b"]);

    assertEquality(2, globalMapFloat.length());
    assertEquality(1.0, globalMapFloat["z"]);
    assertEquality(2.0, globalMapFloat["y"]);

    assertEquality(3, globalMapString.length());
    assertEquality("el", globalMapString["l"]);
    assertEquality("em", globalMapString["m"]);
    assertEquality("en", globalMapString["n"]);
}

function testMappingConstrExprAsSpreadExpr() {
    Foo f = {s: "hello", ...{i: 1, f: 2.0}, "oth": true};

    assertEquality(4, f.length());
    assertEquality("hello", f.s);
    assertEquality(1, f.i);
    assertEquality(2.0, f.f);
    assertEquality(true, f["oth"]);
}

type Baz record {
    int i;
    string s;
};

type Qux record {|
    int i;
    boolean...;
|};

type Student record {
    string name;
};

type Grades record {|
    int physics;
    never name?;
    int...;
|};

type Address record {
    string street;
    never country?;
};

type Employee record {|
    int id;
    string name;
    string dept;
|};

type Candidate record {
    string name;
    never university?;
};

function testSpreadFieldWithRecordTypeHavingNeverField() {
    Grades grades = { physics: 75 };
    Address address= { street: "Main Street" };

    Student john = { name: "John Doe", ...grades };
    assertEquality("John Doe", john.name);
    assertEquality(75, john["physics"]);

    map<anydata> someMap = { name: "John Doe", ...grades };
    assertEquality("John Doe", someMap["name"]);
    assertEquality(75, someMap["physics"]);

    map<anydata> location = { country: "SL", ...address};
    assertEquality("SL", location["country"]);
    assertEquality("Main Street", location["street"]);

    Candidate candidate = {name: "Jack"};
    record {string name;} candidateInLine = {...candidate};
    assertEquality ("Jack", candidateInLine.name);

    record {|int i; string s; never n?;|} bar1InLine = {i: 1, s: "s"};
    Bar bar1 = {...bar1InLine};
    assertEquality(1, bar1.i);
    assertEquality("s", bar1.s);

    record {|int i; string s; never n?;|} bar2InLine = {i: 2, s: "S", n: ()};
    Bar bar2 = {...bar2InLine};
    assertEquality(2, bar2.i);
    assertEquality("S", bar2.s);

    record {|int id; string name; never dept?;|} empInLine = {id: 1023, name: "Joy", dept: ()};
    Employee emp1 = {dept: "a", ...empInLine};
    assertEquality(1023, emp1.id);
    assertEquality("Joy", emp1.name);

    record {|int i; string s; [never, int] x?;|} bar3InLine = {i: 3, s: "b", x: ()};
    Bar bar3 = {...bar3InLine};
    assertEquality(3, bar3.i);
    assertEquality("b", bar3.s);

    record {|int i; string s; never|never x?;|} bar4InLine = {i: 4, s: "c"};
    Bar bar4 = {...bar4InLine};
    assertEquality(4, bar4.i);
    assertEquality("c", bar4.s);

    record {|
        int i;
        string s;
        record {|
            never x;
            int z;
        |} y?;
    |} bar5InLine = {
        i: 5,
        s: "d"
    };
    Bar bar5 = {...bar5InLine};
    assertEquality(5, bar5.i);
    assertEquality("d", bar5.s);

    record {|string s; never x?;|} rec1 = {s: "e"};
    record {|int i; never x?;|} rec2 = {i: 6};
    Bar bar6 = {...rec1, ...rec2};
    assertEquality(6, bar6.i);
    assertEquality("e", bar6.s);

    record {|string s; never x1?;|} rec3 = {s: "f"};
    record {|int i; never x2?;|} rec4 = {i: 7};
    Bar bar7 = {...rec3, ...rec4};
    assertEquality(7, bar7.i);
    assertEquality("f", bar7.s);

    record {|string s; int i; never x?;|} rec5 = {s: "g", i: 8};
    record {|never x?;|} rec6 = {x: ()};

    Bar bar8 = {...rec5, ...rec6};
    assertEquality("g", bar8.s);
    assertEquality(8, bar8.i);
}

type Vehicle record {|
    int year;
    string manufacturer;
    string model;
    anydata...;
|};

type Truck record {
    int year;
    string manufacturer;
    string model;
    int loadCapacity;
};

type RecA record {|
    int i;
    string s;
    string m;
    any|error...;
|};

type RecB record {
    int i;
    string s;
    string m;
    error e;
};

function testSpreadFieldWithRecordTypeHavingRestDescriptor() {
    Truck truck = {year: 2023, manufacturer: "Tesla", model: "Cybertruck", loadCapacity: 15000};
    Vehicle vehicle = {...truck};
    assertEquality("Cybertruck", vehicle.model);
    assertEquality("Tesla", vehicle.manufacturer);
    assertEquality(2023, vehicle.year);
    assertEquality(15000, vehicle["loadCapacity"]);

    RecB recB = {i: 1, s: "s", m: "m", e: error("e")};
    RecA recA = {...recB};
    assertEquality(1, recA.i);
    assertEquality("s", recA.s);
    assertEquality("m", recA.m);
    assertEquality("e", (<error>recA["e"]).message());

    record {|int i; int...;|} r1 = {i: 1};
    record {|int i; string|int...;|} r2 = {...r1};
    assertEquality(1, r2.i);

    record {|string s; never...;|} r3 = {s: "s"};
    record {|string s; int...;|} r4 = {...r3};
    assertEquality("s", r4.s);
}

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
