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

type T1 A[];

type A int;

// ---------------------------------------------------------------------------------------------------------------------

type T2 [B, C];

type B int;

type C string;

// ---------------------------------------------------------------------------------------------------------------------

type T3 map<D>;

type D int;

// ---------------------------------------------------------------------------------------------------------------------

type T4 E;

type E string;

// ---------------------------------------------------------------------------------------------------------------------

type T5 record { F f = ""; };

type F string;

// ---------------------------------------------------------------------------------------------------------------------

class T6 { G g = ""; }

type G string;

// ---------------------------------------------------------------------------------------------------------------------

class T7Dash { public G g = ""; }
type T7 int[]|A[]|[B, C]|map<string>|map<D>|E|int|record { F f; }|T7Dash|error;

// ---------------------------------------------------------------------------------------------------------------------

type T8 [int[], A[], [B, C], map<string>, map<D>, E, int, record { F f; }, T7Dash, error];

// ---------------------------------------------------------------------------------------------------------------------

type T9 H|I;

type T10 J|K|T9|L;

type H A[];

type I [A, B];

type J map<A>;

type K record { F f = ""; };

type L error|T7Dash;

// ---------------------------------------------------------------------------------------------------------------------

type T11 [T7, T10];

// ---------------------------------------------------------------------------------------------------------------------

type T12 xml;

// ---------------------------------------------------------------------------------------------------------------------

class FBClass { string f; function init(string f) { self.f = f; }}
type FB "A" | FBClass;

class Foo {
    string f;

    function init(string f) {
        self.f = f;
    }
}

type FB2 "A" | record { string f; };

type FB3 "A" | record {| string f; |};

// ---------------------------------------------------------------------------------------------------------------------
type IntArray int[];
type Int_String [int, string];

type AssertionError error;

// ---------------------------------------------------------------------------------------------------------------------
type ACTION GET|POST;

const GET = "GET";
const POST = "POST";

function testTypeConstants() returns ACTION {
    return GET;
}

// ---------------------------------------------------------------------------------------------------------------------
distinct class DistinctFoo {
    int i = 0;
}

distinct class DistinctBar {
    *DistinctFoo;

    function init(int i) {
        self.i = i;
    }
}

distinct class ClassA {
    *ClassB;

    function init(int i, int j) {
        self.i = i;
        self.j = j;
    }
}

distinct class ClassB {
    int i = 0;
    int j = 0;
}

distinct class ClassY {
    *ClassX;

    function init(int i) {
        self.i = i;
    }
}

class ClassX {
    int i;

    function init(int i) {
        self.i = i;
    }
}

class Baz {
    *DistinctFoo;

    function init(int i) {
        self.i = i;
    }
}

class Claz {

}

// ---------------------------------------------------------------------------------------------------------------------
type DistinctError distinct error;

type DistinctCustomError distinct error<record { int i; }>;
