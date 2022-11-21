// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type IntOrStr int|string;

type IntStrOrBoolean IntOrStr|boolean;

function test1(IntOrStr[] data) returns error? {
    _ = from IntOrStr i in data
        where i is int
        select i * 2;

    string[] _ = from IntOrStr i in data
        where i !is int
        select i;

    from IntOrStr i in data
    where i is int
    do {
        int _ = i;
    };

    from IntOrStr i in data
    where i !is int
    do {
        string _ = i;
    };
}

function test2(IntOrStr[] data1, IntOrStr[] data2) returns error? {
    int[] _ = from IntOrStr i in data1
        from IntOrStr j in data2
        where i is int && j is int
        select i * j;

    string[] _ = from IntOrStr i in data1
        from IntOrStr j in data2
        where i !is int && j !is int
        select i + j;

    from IntOrStr i in data1
    from IntOrStr j in data2
    where i !is int && j !is int
    do {
        string _ = i + j;
    };
}

function test3(IntStrOrBoolean[] data) returns error? {
    string[] _ = from IntStrOrBoolean i in data
        where i !is boolean && i !is int
        select i;

    string[] _ = from IntStrOrBoolean i in data
        where i !is boolean
        where i !is int
        select i;

    int[][] _ = from IntStrOrBoolean i in data
        where i !is boolean
        select from int ii in 1 ... 3
            where i !is string
            select i * ii;

    from var item in data
    where item !is boolean && item !is int
    do {
        string _ = item;
    };

    from var i in data
    where i !is boolean
    do {
        int[] _ = from int ii in 1 ... 3
            where i is int
            select i * ii;
    };
}

type A record {|
    int i;
|};

type B record {|
    string i;
|};

type C record {|
    string j;
|};

type AorB A|B;

type AorC A|C;

type BorC B|C;

function test4(AorB[] data1, AorC[] data2, BorC[] data3) returns error? {
    _ = from AorB item in data1
        where item is A
        select aFn(item);

    _ = from AorB item in data1
        where item !is A
        select bFn(item); //error incompatible types: expected 'B', found 'AorB'

    from AorB item in data1
    where !(item is A)
    do {
        bFn(item); //error incompatible types: expected 'B', found 'AorB'
    };

    _ = from AorC item in data2
        where item is A
        select aFn(item);

    _ = from AorC item in data2
        where item !is A
        select cFn(item);

    _ = from BorC item in data3
        where item is B
        select bFn(item);

    _ = from BorC item in data3
        where item !is B
        select cFn(item);

    from AorC item in data2
    where item is A
    do {
        aFn(item);
    };

    from AorC item in data2
    where item !is A
    do {
        cFn(item);
    };

    from BorC item in data3
    where item is B
    do {
        bFn(item);
    };

    from BorC item in data3
    where item !is B
    do {
        cFn(item);
    };
}

function aFn(A x) {
}

function bFn(B x) {
}

function cFn(C x) {
}

type D record {
    int i;
};

type E record {
    string i;
};

type F record {
    string j;
};

type DorE D|E;

type DorF D|F;

type EorF E|F;

function dFn(D x) {
}

function eFn(E x) {
}

function fFn(F x) {
}

function test5(DorE[] x, DorF[] y, EorF[] z) returns error? {
    _ = from var item in x
        where item is D
        select dFn(item);

    _ = from var item in x
        where item !is D
        select eFn(item); // error incompatible types: expected 'E', found 'DorE'

    from var item in x
    where item is D
    do {
        dFn(item);
    };

    from var item in x
    where item !is D
    do {
        eFn(item); // error incompatible types: expected 'E', found 'DorE'
    };

    _ = from var item in y
        where item is D
        select dFn(item);

    _ = from var item in y
        where item !is D
        select fFn(item); // error incompatible types: expected 'F', found 'DorF'

    _ = from var item in z
        where item is E
        select eFn(item);

    _ = from var item in z
        where item !is D
        select fFn(item); // error incompatible types: expected 'F', found 'EorF'

    _ = from var item in z
        where item !is E
        select fFn(item); // error incompatible types: expected 'F', found 'EorF'
}

type S readonly & record {|
    int i;
|};

type J readonly & record {|
    string i;
|};

function sFn(S x) {
}

function jFn(J x) {
}

type SorJ S|J;

function test6(SorJ[] data) returns error? {
    _ = from SorJ item in data
        where item is S
        select sFn(item);

    _ = from SorJ item in data
        where item !is S
        select jFn(item);

    from SorJ item in data
    where item is S
    do {
        sFn(item);
    };
}

type K record {|
    int i;
|};

type L record {|
    string i;
|};

type M readonly & (K|L);

function kFn(K x) {
}

function lFn(L x) {
}

function test7(M[] data) returns error? {
    _ = from M item in data
        where item is K
        select kFn(item);

    _ = from M item in data
        where item !is K
        select lFn(item);

    from M item in data
    where item !is K
    do {
        lFn(item);
    };
}

type N record {|
    int i;
|};

type P record {|
    string i;
|};

function nFn(N x) {
}

function pFn(P x) {
}

type readonlyNorP readonly & (N|P);

function test8(readonlyNorP[] data) {
    _ = from var item in data
        where item is N
        select nFn(item);

    _ = from var item in data
        where item !is N
        select pFn(item);
}

type V record {|
    int i;
|};

type W record {|
    string s;
|};

type Y record {|
    int s;
|};

type Z record {|
    float f;
|};

function vwFn(V|W x) {
}

function yzFn(Y|Z x) {
}

function vwyFn(V|W|Y x) {
}

function zFn(Z x) {
}

function test9((V|W|Y|Z)[] data) returns error? {
    _ = from var item in data
        where item is V|W
        select vwFn(item);

    _ = from var item in data
        where item !is V|W
        select yzFn(item); // error incompatible types: expected '(Y|Z)', found '(W|Y|Z)'

    from var item in data
    where item !is V|W
    do {
        yzFn(item); // error incompatible types: expected '(Y|Z)', found '(W|Y|Z)'
    };

    _ = from var item in data
        where item is V|W|Y
        select vwyFn(item);

    _ = from var item in data
        where item !is V|W|Y
        select zFn(item);
}

type Q record {|
    string a;
|};

type R record {
    int b;
};

type T record {
    boolean a;
};

function qFn(Q x) {
}

function rFn(R x) {
}

function rtFn(R|T x) {
}

function test10((Q|R)[] data1, (Q|R|T)[] data2) returns error? {
    _ = from var item in data1
        where item is Q
        select qFn(item);

    _ = from var item in data1
        where item !is Q
        select rFn(item); // error incompatible types: expected 'R', found '(Q|R)'

    from var item in data1
    where item !is Q
    do {
        rFn(item); // error incompatible types: expected 'R', found '(Q|R)'
    };

    _ = from var item in data2
        where item is Q
        select qFn(item);

    _ = from var item in data2
        where item !is Q
        select rtFn(item); // error incompatible types: expected '(R|T)', found '(Q|R|T)'
}

type Integers 1|2|3;

type Chars "A"|"B"|"C";

function test11(Integers[] numbers, Chars[] chars) returns error? {
    1[] _ = from int item in numbers
        where item == 1
        select item;

    (1|2)[] _ = from int item in numbers
        where item == 1 || item == 2
        select item;

    from Integers item in numbers
    where item == 1 || item == 2
    do {
        1 _ = item; // error incompatible types: expected '1', found '(1|2)'
    };

    1[] _ = from int item in numbers
        where item is 1
        select item;

    "C"[] _ = from Chars item in chars
        where item == "C"
        select item;

    from Chars item in chars
    where item == "C"
    do {
        "C" _ = item;
    };

    int?[] a = [1, 2, 3];
    var _ = from var item in a
        where item == 1
        let () varName = item // error incompatible types: expected '()', found '1'
        select item;

    1[] _ = from var item in 1 ... 3
        let int? one = 1
        where one == 1
        select one;

    from var item in 1 ... 3
    let int? one = 1
    where one == 1
    do {
        2 two = one; // error incompatible types: expected '2', found '1'
    };

    (int|string)[] arr = [1, 2, 3];
    from int|string item in arr
    where item is int
    let int num = item
    do {
        int _ = num * 2;
    };

    IntStrOrBoolean[] intArr = [1, 2, 3, 4];

    from var item in intArr
    where item is int && item == 2
    do {
        2 _ = item;
    };
}
