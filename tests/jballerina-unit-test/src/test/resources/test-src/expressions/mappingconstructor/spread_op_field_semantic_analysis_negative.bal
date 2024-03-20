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

///////////////////////// Record Tests /////////////////////////

type Foo record {|
    string s;
    int i;
|};

type Bar record {|
    int s;
    float i;
|};

string s = "hello";

function testSpreadOpFieldOfIncorrectType() {
    boolean b = true;
    Foo f = {...s, s, i: 1, ...b};
}

function testFieldsOfIncorrectTypeViaSpreadOp() {
    Bar b = {s: 1, i: 1.0};
    record {
        boolean i = true;
    } r = {};
    Foo f = {...b, i: 1, ...r};
}

function testUnspecifiedClosedRecordFieldViaSpreadOp() {
    record {
        int i;
        boolean x;
    } r = {i: 1, x: true};
    Foo f = {s: "hello", ...r};
}

function testUndeclaredSpreadOpField() {
    Foo f = {s, i: 2, ...b};
}

function testFieldWithNeverType() {
    record {|string s; string i; never x?;|} rec1 = {s: "a", i: "1023"};
    Foo _ = {...rec1};

    record {|string s; never x?;|} rec2 = {s: "b"};
    Foo _ = {...rec2};

    record {|string s; never i?;|} rec3 = {s: "c"};
    Foo _ = {...rec3};

    record {|string s; never|never i?;|} rec4 = {s: "d"};
    Foo _ = {...rec4};

    record {|string s;|} rec5 = {s: "e"};
    record {|never age?;|} rec6 = {};
    Foo _ = {...rec5, ...rec6};
}

type Address record {
    string street;
};

type Street record {|
    string street;
|};

type Foz record {|
    string s;
    int i;
    int j;
    boolean b;
    string...;
|};

function testSpreadOfOpenRecordToCreateClosedRecord() {
    Address address1 = {street: "Main Street"};
    record {|string street;|} _ = {...address1};

    Address address2 = {street: "Maple street" };
    Street _ = {...address2};

    record {string street;} address3 = {street: "Jump Street"};
    Street _ = {...address3};

    record {string s;} foo1 = {s: "S"};
    record {int i;} foo2 = {i: 2};
    Foo _ = {...foo1, ...foo2};

    record {|string s;|} foz1 = {s: "s"};
    record {|int i;|} foz2 = {i: 1};
    record {int j;} foz3 = {j: 2};
    record {boolean b;} foz4 = {b: true};
    Foz _ =  {...foz1, ...foz2, ...foz3, ...foz4};
}

type Country record {|
    string name;
    string continent;
    string...;
|};

function testSpreadOpFieldOfMismatchingRestType() {
    record {string name; string continent; int population;} country1 = {name: "China", continent: "Asia", population: 1400};
    Country _ = {...country1};

    record {string name; string continent;} country2 = {name: "Sri Lanka", continent: "Asia"};
    Country _ = {...country2};

    record {string name;} country3 = {name: "India"};
    Country _ = {continent: "Asia", ...country3};

     record {|string name; string continent; string...;|} country4 = {name: "Sri Lanka", continent: "Asia"};
     record {|string name; string continent; int...;|} _ = {...country4};

     record {|string name; string continent; error...;|} country5 = {name: "India", continent: "Asia"};
     record {|string name; string continent;|} _ = {...country5};
}

///////////////////////// Map Tests /////////////////////////

function testMapSpreadOpFieldOfIncorrectType() {
    int iv = 1;
    map<int> f = {i: 1, ...iv};
}

function testMapFieldsOfIncorrectTypeViaSpreadOp() {
    Bar b = {s: 1, i: 1.0};
    record {
        boolean i = true;
    } r = {};
    map<string> f = {...b, i: "j", ...r};
}

function testUndeclaredMapSpreadOpField() {
    map<string|int> m = {s, i: 2, ...b, ...getFoo()};
}

function testJsonSpreadOpFieldOfIncorrectType() {
    map<anydata> m = {a: 1, b: "hi"};
    record {|
        string s;
        any...;
    |} r = {s: "str"};

    json j = {...r, c: 2, ...m};
    map<json> mj = {a: 1, ...r, ...m};
}

type Baz record {|
    int...;
|};

function testMapSpreadOpFieldOfIncorrectTypeForRecord() {
    map<string> strMap = {
        a: "abc",
        b: "bcd"
    };

    Baz bz = {...strMap};
    Bar br = {s: 1, i: 2.0, ...strMap};
}
