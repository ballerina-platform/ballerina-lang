// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type A record {|
    string a;
|};

type B record {|
    int a;
|};

function f1(A|B x) {
    if x is A {
        return;
    } else {
        B _ = x; // OK
    }
}

function f2(A|B x) {
    if x is A {
        return;
    }
    B _ = x; // OK
}

type C record {|
    int i;
|};

type D record {|
    string i;
|};

type E C|D;

function f3(E v) {
    if v is C {

    } else {
        D _ = v; // OK
    }
}

type F readonly & record {|
    int i;
|};

type G readonly & record {|
    string i;
|};

type H F|G;

function f4(H v) {
    if v is F {

    } else {
        G _ = v; // OK
    }
}

type I readonly & record {|
    int i;
|};

type J readonly & record {|
    string i;
|};

function f5(I|J v) {
    if v is I {

    } else {
        J _ = v; // OK
    }
}

type K record {|
    int i;
|};

type L record {|
    string i;
|};

type M readonly & (K|L);

function f6(M v) {
    if v is K {

    } else {
        L _ = v; // OK
    }
}

type N record {|
    int i;
|};

type O record {|
    string i;
|};

function f7(readonly & (N|O) v) {
    if v is N {

    } else {
        O _ = v; // OK
    }
}

type P record {|
    int i;
|};

type Q record {|
    string i;
|};

type R record {|
    anydata i;
|};

function f8(P|Q v) {
    if v is R {

    } else {
        _ = v; // unreachable
    }
}

type S record {|
    int i;
|};

type T record {|
    int j;
|};

type U S|T;

function f9(U v) {
    if v is S {

    } else {
        T _ = v; // OK
    }
}

function f10() {
    int[]|error res = [];

    if res is int[] {
        int[] _ = res;
    } else {
        error x = res; // OK
    }
}

function f11() {
    int[] & readonly|string[] x = [1];

    if x is int[] {

    } else {
        string[] _ = x; // OK
    }
}

type V record {|
    int i;
|};

type W record {|
    string s;
|};

type X record {|
    int s;
|};

type Y record {|
    float f;
|};

function f12(V|W|X|Y v) {
    if v is V|W {

    } else {
        X|Y _ = v; // OK
    }

    if v is V|W|X {

    } else {
        Y _ = v; // OK
    }
}

function f13([int]|[string] x) {
    if x is [int] {
        [int] _ = x;
    } else {
        if x is [string] {
            return;
        }
        [int|string] _ = x; // unreachable
        [int] _ = x; // error: https://github.com/ballerina-platform/ballerina-lang/issues/36921
    }
}

function f14([int]|[string] x) {
    if x is [int] {
        [int] _ = x;
    } else {
        [string] _ = x; // OK
        [int] _ = x; // error
        [int|string] _ = x; // OK
        [int]|[string] _ = x; // OK
    }
}

function f15([int]|[string] & readonly x) {
    if x is [int] {
        [int] _ = x;
    } else {
        [int] & readonly _ = x; // error incompatible types: expected '[int] & readonly', found '([string] & readonly)'

        [string] _ = x; // OK
        [string] & readonly _ = x; // OK
    }
}

function f16([int]|[string] & readonly x) {
    if x is [string] {
        [string] & readonly _ = x; // OK
    } else {
        [int] _ = x; // OK
        [string] _ = x; // error incompatible types: expected '[string]', found '[int]'
    }

    if x is [string] & readonly {
        [string] & readonly _ = x;
    } else {
        [int] _ = x; // OK
        [int] & readonly _ = x; // error incompatible types: expected '[int] & readonly', found '[int]'
        [string] & readonly _ = x; // error incompatible types: expected '[string] & readonly', found '[int]'
    }
}

function f17([int] & readonly|[string] & readonly x) {
    if x is [int] {
        [int] _ = x;
        [int] & readonly _ = x; // OK
    } else {
        [string] _ = x; // OK
        [string] & readonly _ = x; // OK
        [int] _ = x; // error incompatible types: expected '[int]', found '([string] & readonly)'
    }

    if x is [string] & readonly {
        [string] _ = x; // OK
        [string] & readonly _ = x; // OK
        [int] _ = x; // error incompatible types: expected '[int]', found '[string] & readonly'
    } else {
        [int] _ = x; // OK
        [int] & readonly _ = x; // OK
    }
}

function f18(([int]|[string]) & readonly x) {
    if x is [int] {
        [int] _ = x;
        [int] & readonly _ = x;
    } else {
        [string] _ = x; // OK
        [string] & readonly _ = x; // OK
        [int] _ = x; // error incompatible types: expected '[int]', found '([string] & readonly)'
    }
}

function f19(int[]|string[] x) {
    if x is int[] {
        int[] _ = x;
    } else {
        string[] _ = x; // OK
        int[] _ = x; // error
        (int|string)[] _ = x; // OK
        int[]|string[] _ = x; // OK
    }
}

function f20(int[]|string[] & readonly x) {
    if x is int[] {
        int[] _ = x;
    } else {
        string[] _ = x; // OK
        string[] & readonly _ = x; // OK
        int[] & readonly _ = x; // error incompatible types: expected 'int[] & readonly', found '(string[] & readonly)'
    }
}

function f21(int[] & readonly|string[] & readonly x) {
    if x is int[] {
        int[] _ = x;
        int[] & readonly _ = x;
    } else {
        string[] _ = x; // OK
        int[] _ = x; // error incompatible types: expected 'int[]', found '(string[] & readonly)'
        string[] & readonly _ = x; // OK
    }
}

function f22((int[]|string[]) & readonly x) {
    if x is int[] {
        int[] _ = x;
        int[] & readonly _ = x;
    } else {
        int[] _ = x; // error incompatible types: expected 'int[]', found '(string[] & readonly)'
        string[] _ = x; // OK
        string[] & readonly _ = x; // OK
    }
}

function f23((int|string)[] & readonly x) {
    if x is int[] {
        int[] _ = x; // OK
        int[] & readonly _ = x; // OK
    } else {
        string[] _ = x; // error incompatible types: expected 'string[]', found '(int|string)[] & readonly'
        string[] & readonly _ = x; // error incompatible types: expected 'string[] & readonly', found '(int|string)[] & readonly'
        (int|string)[] _ = x; // OK
        (int|string)[] & readonly _ = x; // OK
    }

    if x is string[] & readonly {
        string[] _ = x; // OK
        string[] & readonly _ = x; // OK
    } else {
        int[] _ = x; // error incompatible types: expected 'int[]', found '(int|string)[] & readonly'
        int[] & readonly _ = x; // error incompatible types: expected 'int[] & readonly', found '(int|string)[] & readonly'
        (int|string)[] _ = x; // OK
        (int|string)[] & readonly _ = x; // OK
    }
}

type Z record {|
    xml i;
|};

function f24() {
    V|json a = ();

    if a is V {
        V _ = a; // OK
    } else {
        json _ = a; // OK, because `V` belongs to `json`.
    }

    Z|json b = {};

    if b is Z {
        Z _ = b; // OK
    } else {
        json _ = b; // OK
    }

    if b is json {
        json _ = b; // OK
    } else {
        Z _ = b; // OK
    }

    anydata|record {| stream<int> s; |} c = 1;

    if c is anydata {
        anydata _ = c; // OK
    } else {
        record {| stream<int> s; |} _ = c; // OK
    }

    if c is record {| stream<int> s; |} {
        record {| stream<int> s; |} _ = c; // OK
    } else {
        anydata _ = c; // OK
    }

    any|record {| error x; |} d = 1;

    if d is record {| error x; |} {
        record {| error x; |} _  = d;
    } else {
        any _ = d; // OK, since record {| error x; |} is a subtype of any
    }
}

function f25() {
    V|json a = ();

    if a is V {
        V _ = a; // OK
    } else {
        json _ = a; // OK, because `V` belongs to `json`.
    }

    Z|json|stream<int> b = {};

    if b is Z {
        Z _ = b; // OK
    } else {
        json|Z _ = b; // error incompatible types: expected '(json|Z)', found '(json|stream<int>)'
    }

    if b is json {
        json _ = b; // OK
    } else {
        Z|stream<int> _ = b; // OK
    }

    anydata|record {| stream<int> s; |}|future<string> c = 1;

    if c is anydata|future<string> {
        anydata|future<string> _ = c; // OK
    } else {
        record {| stream<int> s; |} _ = c; // OK
    }

    if c is record {| stream<int> s; |} {
        record {| stream<int> s; |} _ = c; // OK
    } else {
        anydata|record {| stream<int> s; |} _ = c; // error incompatible types: expected '(anydata|record {| stream<int> s; |})', found '(anydata|future<string>)'
    }

    any|record {| error x; |}|error d = 1;

    if d is record {| error x; |} {
        record {| error x; |} _  = d;
    } else {
        any|error y = d; // OK, since record {| error x; |} is a subtype of any|error
        _ = y is error;
    }

    Z|map<int>|xml e = {};

    if e is Z {
        Z _ = e;
    } else {
        map<int>|xml _ = e; // OK
    }
}

type A2 record {
    int b;
};

type A3 record {
    boolean a;
};

function f26(A|A2 x) {
    if x is A {
        A _ = x;
    } else {
        A2 _ = x; // OK
    }

    A|A2|A3 y = <A> {a: ""};

    if y is A {
        A _ = y;
    } else {
        A2|A3 _ = y; // OK
    }
}

type A4 record {|
    string a;
|};

type A5 record {|
    int a;
    never...;
|};

type A6 record {|
    int b;
    never...;
|};

function f27(A4|A5 v, A4|A6 w) {
    if v is A4 {

    } else {
        A5 _ = v; // OK
    }

    if w is A4 {

    } else {
        A6 _ = w; // OK
    }
}

function f28() {
    A|int|string[] v = 1;

    if v is A|int {
        A|int _ = v;
    } else {
        string[] _ = v; // OK
    }

    A|A6|boolean w = true;

    if w is A|boolean {
        A|boolean _ = w;
    } else {
        A6 _ = w; // OK
    }
}

function f29() {
    json|xml|stream<string[], error?> content = 1;

    if content is string[][] || content is stream<string[], error?> {
        stream<string[], error?>|string[][] _ = content; // OK
    } else {
        json|xml _ = content; // OK
        xml _ = content; // error incompatible types: expected 'xml', found '(json|xml)'
    }

    if content is string[][]|stream<string[], error?> {
        stream<string[], error?>|string[][] _ = content; // OK
    } else {
        json|xml _ = content; // OK
        json _ = content; // error incompatible types: expected 'json', found '(json|xml)'
    }

    if content is json {
        json _ = content; // OK
    } else {
        xml|stream<string[], error?> _  = content; // OK
        stream<string[], error?> _ = content; // error incompatible types: expected 'stream<string[],error?>', found '(xml|stream<string[],error?>)'
    }
}

function f30() {
    int[]|boolean[]|stream<string[], error?> content = [1, 2];

    if content is int[] || content is stream<string[], error?> {
        int[]|stream<string[], error?> _ = content; // OK
    } else {
        boolean[] _ = content; // OK
        boolean[]|int[] _ = content; // OK
    }

    if content is int[]|stream<string[], error?> {
        int[]|stream<string[], error?> _ = content; // OK
    } else {
        boolean[]|int[] _ = content; // OK
        int[] _ = content; // error incompatible types: expected 'int[]', found 'boolean[]'
    }
}

function f31() {
    int[]|boolean[]|xml content = [1];

    if content is int[]|float[] {
        int[] _ = content; // OK
    } else {
        boolean[] _ = content; // error incompatible types: expected 'boolean[]', found '(boolean[]|xml)'
        anydata[]|xml _ = content; // OK
        boolean[]|xml|int[] _ = content; // OK
    }
}

public type Utc readonly & [int, decimal];

public type ValueType Utc|record {}|byte[];

function f32(ValueType x) {
    if x is byte[] {

    } else if x is Utc {

    } else {
        record {} _ = x; // OK
        byte[] _ = x; // error incompatible types: expected 'byte[]', found 'record {| anydata...; |}'
    }

    if x is byte[]|Utc {

    } else {
        record {} _ = x; // OK
        byte[]|Utc _ = x; // error incompatible types: expected '(byte[]|Utc)', found 'record {| anydata...; |}'
    }

    if x is byte[]|record {} {

    } else {
        byte[] _ = x; // error incompatible types: expected 'byte[]', found 'Utc'
        Utc _ = x; // OK
    }

    if x is byte[] {

    } else {
        byte[]|Utc _ = x; // error incompatible types: expected (byte[]|Utc)', found '(Utc|record {| anydata...; |})'
        Utc|record {} _ = x; // OK
    }

    if x is Utc {

    } else {
        byte[]|record {} _ = x; // OK
        Utc|record {} _ = x; // error incompatible types: expected '(Utc|record {| anydata...; |})', found '(record {| anydata...; |}|byte[])'
    }

    Utc|byte[] y = <byte[]> [];

    if y is byte[] {

    } else {
        Utc _ = y; // OK
        byte[] _ = y; // error incompatible types: expected 'byte[]', found 'Utc'
    }

    [int, decimal]|record {}|byte[] z = {};

    if z is byte[] {

    } else if z is [int, decimal] {

    } else {
        record {} _ = z; // OK
    }

    if z is byte[]|[int, decimal] {
        if z is byte[] {

        } else {
            [int, int] _ = z; // error incompatible types: expected '[int,int]', found '[int,decimal]'
        }
    } else {
        record {} _ = z; // OK
        byte[]|[int, decimal] _ = z; // error incompatible types: expected '(byte[]|[int,decimal])', found 'record {| anydata...; |}'
    }

    if z is byte[]|record {} {

    } else {
        [int, decimal] _ = z; // OK
    }

    if z is byte[] {

    } else {
        [int, int]|record {} _ = z; // error incompatible types: expected '([int,int]|record {| anydata...; |})', found '([int,decimal]|record {| anydata...; |})'
    }

    if z is [int, decimal] {

    } else {
        byte[]|record {} _ = z; // OK
    }
}
