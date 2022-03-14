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

function test1(int x) {
    if x == +1 {
        1 _ = x;
    } else {
        1 _ = x; // error incompatible types: expected '1', found 'int'
        int _ = x; // OK
    }
}

function test2(int x) {
    if x != +1 {
        1 _ = x; // error incompatible types: expected '1', found 'int'
    } else {
        1 _ = x; // OK
    }
}

function test3(3|"foo"|false x) {
    if 3 == x {
        3 _ = x;
    } else if x == "foo" {
        "foo" _ = x;
    } else {
        false _ = x;
    }

    int _ = x; // error incompatible types: expected 'int', found '3|foo|false'

    if x == (+3) {
        // Type not narrowed. issue #34928
        3 _ = x;
        +3 _ = x;
        (+3) _ = x;
        3.0 _ = x;
        3.0f _ = x;
        3.0d _ = x;
    } else if x == (("foo")) {
        // Type not narrowed. issue #34928
        "foo" _ = x;
        ("foo") _ = x;
        (("foo")) _ = x;
    } else {
        // Type not narrowed. issue #34928
        false _ = x;
    }
}

function test4(3|"foo"|false x) {
    if 3 == x {
        3 _ = x;
    } else if x != "foo" {
        false _ = x; // OK
    } else {
        "foo" _ = x; // OK
    }
}

function test5(1|"foo"|false? x) {
    if x == false {
        false _ = x;
    } else if "foo" == x {
        "foo" _ = x;
    } else {
        1 _ = x; // error incompatible types: expected '1', found '1?'
        1? _ = x; // OK
    }

    int _ = x; // error incompatible types: expected 'int', found '(false|1|foo)?'
}

function test6(1|"foo"|false? x) {
    if x != false {
        1|"foo"? _ = x; // OK
    } else if false == x {
        false _ = x; // OK
    }
}

function test7(+3|"foo" x) {
    if x == 3 {
        3 _ = x;
        return;
    }

    "foo" _ = x; // OK
}

function test8(+3|"foo" x) {
    if x != 3 {
        "foo" _ = x;
        return;
    }

    +3 _ = x; // OK
}

function test9(-1? x) {
    if () == x {
        () _ = x;
        return;
    }

    -1 _ = x; // OK
}

function test10(3? x) {
    if x == () {
        () _ = x;
        return;
    } else {
        3 _ = x;
    }

    3 _ = x; // type not narrowed. issue #34307
}

function test11("foo"|"bar"|"baz" x) {
    if x == "foo" {
        "foo" _ = x;
        return;
    } else if "bar" == x {
        "bar" _ = x;
        return;
    }

    "baz" _ = x; // type not narrowed. issue #34307
}

function test12("foo"|"bar"|"baz"? x) {
    if x == "bar" {
        "bar" _ = x;
        return;
    } else if x == "baz" {
        "baz" _ = x;
        return;
    }

    "foo" _ = x; // error incompatible types
    "foo"? _ = x; // type not narrowed. issue #34307
}

function test13("a"|"b"|"c"? x) {
    if x == "d" {
        "d" _ = x; // OK
        "e" _ = x; // No error. issue #34928
    } else if x == "e" {
        "baz" _ = x; // No error. issue #34928
    } else if x == "a" {
        "a" _ = x; // OK
        "c" _ = x; // error incompatible types: expected 'c', found 'a
        return;
    }

    "b"|"c"? _ = x; // type not narrowed. issue #34307
}

function test14(boolean? b) {
    if b == true {
        true _ = b;
        return;
    } else if b == false {
        false _ = b;
        return;
    } else {
        () _ = b; // type not narrowed. issue #30598, #33217
    }

    () _ = b; // // type not narrowed. issue #34307
}

function test15(true|false? b) {
    if b == true {
        true _ = b;
        return;
    } else if b == false {
        false _ = b;
        return;
    } else {
        () _ = b;
    }

    () _ = b; // type not narrowed. issue #34307
}

function test16(1|2.0|3.3? x) {
    if x == () {
        () _ = x;
    } else {
        1|2.0|3.3 _ = x; // OK

        if x is (2.0|3.0) {
            3 _ = x; // error incompatible types: expected '3', found '2.0f'

            if (x == 3.0) {
                return;
            }

            2.3 _ = x; // error incompatible types: expected '2.3f', found '2.0f'
            2.0 _ = x; // OK
        } else {
            int _ = x; // error incompatible types: expected 'int', found '(3.3f|1)'
            1|3.3 _ = x; // OK
        }
    }

    int _ = x; // error incompatible types: expected 'int', found '(3.3f|1|2.0f)?'
}
