// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function valueTypeWichAlwaysTrue() returns string {
    int x = 10;
    if (x is int) { // always true
        return "int";
    }

    return "n/a"; // unreachable code
}

function valueTypeWichAlwaysFalse() returns string {
    int x = 10;
    if (x is float) {
        return "int";
    }

    return "n/a";
}

function valueTypeAgainstUnionTypeWhichAlwaysTrue() returns string {
    int x = 10;
    if (x is float|int) { // always true
        return "float|int";
    }

    return "n/a"; // unreachable code
}

function valueTypeAgainstUnionTypeWhichAlwaysFalse() returns string {
    int x = 10;
    if (x is string|float) {
        return "string|float";
    }

    return "n/a";
}

function unionTypeAgainstUnionTypeWhichAlwaysTrue_1() returns string {
    int|string x = "hello";
    if (x is int|string) { // always true
        return "int|string";
    }

    return "n/a"; // unreachable code
}

function unionTypeAgainstUnionTypeWhichAlwaysTrue_2() returns string {
    int|string x = "hello";
    if (x is int|string|float) { // always true
        return "int|string|float";
    }

    return "n/a"; // unreachable code
}

function unionTypeAgainstUnionTypeWhichAlwaysFalse() returns string {
    int|string x = "hello";
    if (x is boolean|float) {
        return "boolean|float";
    }

    return "n/a";
}

type A record {
    int x = 0;
    string y = "";
};

type B record {
    int x = 0;
};

function testSimpleRecordTypes() returns string {
    A a = {};
    if (a is B) { // always true
        return "a is B";
    } else if (a is A) {
        return "a is A"; // unreachable code
    }

    // checking against undefined type (this moved to type-test-expr-semantics-negative.bal)
    // if (a is C) {
    //
    // }
    return "n/a"; // unreachable code
}

type X record {
    int p = 0;
    string q = "";
    A r = {};
};

type Y record {
    int p = 0;
    string q = "";
    B r = {};
};

function testNestedRecordTypes() returns string {
    X x = {};
    if (x is Y) { // always true
        return "x is B";
    } else if (x is X) {
        return "x is A"; // unreachable code
    }

    return "n/a"; // unreachable code
}

function testArrays() {
    int[] x = [1, 2, 3];
    int[][] y = [[1, 2, 3], [4, 5, 6]];

    boolean _ = x is int[] && y is int[][];
    boolean _ = x is float[];
    boolean _ = y is json;
    boolean _ = y is json[];
    boolean _ = y is json[][];
}

function testTuples() {
    [int, string] a = [4, "hello"];

    boolean _ = a is [int, string];
    boolean _ = a is [float, boolean];
    boolean _ = a is [any, any];
    boolean _ = a is [json, json];
}

function testTupleWithAssignableTypes() returns [boolean, boolean, boolean] {
    [X, Y] a = [{}, {}];
    boolean b0 = a is [X, X];
    boolean b1 = a is [X, Y];
    boolean b2 = a is [Y, Y];
    return [b0, b1, b2];
}

function testSimpleConstrainedMap() returns [boolean, boolean, boolean, boolean, boolean] {
    map<string> m = {"key1": "value1"};
    boolean b0 = m is map<any>;
    boolean b1 = m is map<any>;
    boolean b2 = m is map<string>;
    boolean b3 = m is json;
    boolean b4 = m is map<json>;
    return [b0, b1, b2, b3, b4];
}

type A3 record {
    int x = 0;
};

type B3 record {|
    int x = 0;
|};

function testSealedRecordTypes() returns string {
    A3 a = {};
     if (a is B3) {
        return "a is B3";
    } else if (a is A3) {
        return "a is A3";
    }

    return "n/a"; // unreachable code
}

function testRecordArrays() returns [boolean, boolean] {
    A[] a = [{}, {}];
    A[][] b = [[{}, {}], [{}, {}]];
    return [a is B[], b is B[][]];
}

function testJsonArrays() returns [boolean, boolean] {
    json[] x = [1, 2, 3];
    json[][] y = [[1, 2, 3], [4, 5, 6]];
    boolean b0 = x is int[];
    boolean b1 = y is int[][];
    return [b0, b1];
}

public type X1 object {
    public int p;
    public string q;
};

public type Y1 object {
    public float r;
    *X1;
};

public class Z1 {
    *Y1;
    public boolean s;

    public function init(int p, string q, float r, boolean s) {
        self.p = p;
        self.q = q;
        self.r = r;
        self.s = s;
    }
}

function testObjectEquivalency() returns [string, string] {
    Z1 z = new Z1(5, "foo", 6.7, true);
    string s1 = "";
    string s2 = "";

    if(z is X1) {
        s1 = "values: " + z.p.toString() + ", " + z.q;
    }

    if (z is Y1) {
        s2 = "values: " + z.p.toString() + ", " + z.q + ", " + z.r.toString();
    }

    return [s1, s2];
}

type FooBar "foo"|"bar";
type FooOne "foo"|1;

function testFiniteTypeAsAlwaysTrueBroaderType() {
    FooBar f1 = "foo";
    if (f1 is string) {
        string _ = f1;
    }

    FooOne f2 = "foo";
    if (f2 is string|int) {
        string|int _ = f2;
    }
}

type BazTwo "baz"|2;

function testFiniteTypeAsNeverMatchingFiniteType() {
    FooBar f1 = "foo";
    if (f1 is BazTwo) {
        BazTwo _ = f1;
    }
}

function testNonIntersectingUnions() {
    string|int x = 1;
    _ = x is float|boolean;
}

function testXMLNeverType() {
    xml<never> c = xml ``;
    'xml:Text d = xml ``;
    xml<xml<'xml:Text>> f = xml ``;
    xml<xml<never>> g = xml ``;
    xml e = xml ``;
    _ = c is string;
    _ = d is string;
    _ = f is string;
    _ = g is string;
    _ = e is string;
}

function testRecordNegative() {
    Baz|int val = 11;
    boolean _ = val is Bar;
    boolean _ = val is Qux;

    Bar val2 = {};
    boolean _ = val2 is Baz;
    boolean _ = val2 is Quux;

    Qux val3 = {code: new};
    boolean _ = val3 is Baz;
    boolean _ = val3 is Quux;

    Quux val4 = {"i": 1, "j": 2};
    boolean _ = val4 is Bar;
    boolean _ = val4 is Qux;
    boolean _ = val4 is record {|int i; boolean b;|};

    ClosedRecordWithIntField val5 = {i: 100};
    boolean _ = val5 is record {| int i; string s; |};
}

type Baz record {
};

type Foo record {
    readonly Class code = new;
};

type Bar record {
    readonly Class code = new;
};

type Qux record {
    readonly Class code;
};

readonly class Class {

}

type Quux record {|
    int...;
|};

type ClosedRecordWithIntField record {|
    int i;
|};

function testAnydataAgainstInvalidArray() {
    object {}[] arr1 = [];

    if arr1 is anydata {
        anydata[] _ = arr1;
    }

    anydata arr2 = [];

    if arr2 is object {}[] {
        object {}[] _ = arr2;
    }
}

type RecordWithIntFieldAndNeverRestField record {|
    int i;
    never...;
|};

type RecordWithIntFieldAndEffectivelyNeverRestField record {|
    int i;
    [never, int]...;
|};

type Record record {|
    int i;
    string s;
|};

function testRecordNegative2() {
    Record rec = {i: 1, s: ""};
    boolean _ = rec is RecordWithIntFieldAndNeverRestField;
    boolean _ = rec is RecordWithIntFieldAndEffectivelyNeverRestField;
}
