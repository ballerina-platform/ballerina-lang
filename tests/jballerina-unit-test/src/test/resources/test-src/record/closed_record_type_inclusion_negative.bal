// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Person record {
    string name;
    int age;
};

class PersonObj {
    string name = "";
    int age = 0;
}

type Student record {|
    *PersonObj;
    string school;
|};

type IntOrFloat int|float;

type Foo1 record {|
    *IntOrFloat;
|};

type FiniteT 1|2|3|"foo"|"bar";

type Foo2 record {|
    *FiniteT;
|};

type Foo3 record {|
    *int;
    *float;
    *boolean;
    *string;
    *byte;
    *json;
    *xml;
|};

type Gender "male"|"female";

type Person2 record {
    string name;
    Gender gender;
};

type Student2 record {|
    *Person2;
    string school;
|};

function testAttributeRetainment() {
    Student2 s = {name:"John Doe", school:"ABC"};
}

type Student3 record {|
    *Person;
    *Person2;
|};

type UserData1 record {|
    *Data;
|};

type UserData2 record {|
    int index;
    *Data;
|};

type Rec1 record {|
    int i;
    string...;
|};

type IncludingRec1 record {|
    boolean b;
    *Rec1;
|};

type Rec2 record {|
    float f;
    error...;
|};

type IncludingRec2 record {| // cannot use type inclusion with more than one open record
    *Rec1;
    *Rec2;
|};

type Rec3 record {|
    int i;
    string...;
|};

type Rec4 record {|
    int j;
    string...;
|};

type IncludingRec3 record {|
    int k;
    *Rec3;
    *Rec4;
|};

type Rec5 record {
    int i;
};

type Rec6 record {
    int j;
};

type IncludingRec4 record {|
    int k;
    *Rec5;
    *Rec6;
|};

function testRestTypeOverridingNegative() {
    IncludingRec1 rec1 = {i: 1, b: false, "s": "str", "f": 3.02}; // incompatible types: expected 'string', found 'float'
    IncludingRec3 rec2 = {i: 1, j: 2, k: 3, "s": error("Message")}; // incompatible types: expected 'string', found 'error'
    IncludingRec4 rec3 = {i: 1, j: 2, k: 3, "s": error("Message")}; // incompatible types: expected 'anydata', found 'error'
}

type Rec7 record {|
   int x;
   string...;
|};

type Rec8 record {|
   int y;
   string...;
|};

type Rec9 record {|
  Rec7...;
|};

type Rec10 record {|
  Rec8...;
|};

type Baz record {| // // cannot use type inclusion with more than one open record
  *Rec9;
  *Rec10;
|};

type PersonOne record {|
    *PersonOne;
    string name;
|};

type PersonTwo record {|
    string name;
    *Employee;
|};

type Employee record {|
    *PersonTwo;
    int age;
|};

public type Foo record {|
    float body;
|};

// Out of order inclusion test : added to test a NPE
type Bar record {|
    *Foo;
    Baz2 body;   // defined after the type definition
|};

type Baz2 record {|
    int id;
|};

readonly class ReadOnlyClass {
    int[] x = [1, 2, 3];
}

public type Qux readonly & record {|
    string a;
    ReadOnlyClass b;
|};

type Quux record {|
    anydata body;
|};

public type Corge record {|
    *Quux;
    Qux body; // error: included field 'body' of type 'anydata' cannot be overridden by a field of type 'Qux': expected a subtype of 'anydata'
|};
