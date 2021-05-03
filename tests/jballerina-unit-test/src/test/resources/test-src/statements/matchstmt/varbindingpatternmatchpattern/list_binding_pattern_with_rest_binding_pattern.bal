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

function listBindingPattern1(any v) returns string {
    match v {
        var [a, ...b] => {
            return "match";
        }
    }
    return "No match";
}

function testListBindingPatternWithRest1() {
    assertEquals("match", listBindingPattern1([1]));
    assertEquals("match", listBindingPattern1([1, 2]));
    assertEquals("match", listBindingPattern1([1, 2, 3]));
}

function listBindingPattern2([int, int] v) returns int {
    match v {
        var [a, ...b] => {
            return b[0];
        }
    }
}

function testListBindingPatternWithRest2() {
    assertEquals(2, listBindingPattern2([1, 2]));
}

function listBindingPattern3([int, int, int, int] v) returns int {
    match v {
        var [a, b, ...c] => {
            return c[1];
        }
    }
}

function testListBindingPatternWithRest3() {
    assertEquals(5, listBindingPattern3([1, 2, 3, 5]));
    assertEquals(6, listBindingPattern3([1, 4, 3, 6]));
}

function listBindingPattern4([int, int, string] v) returns int|string {
    match v {
        var [a, b, ...c] => {
            return c[0];
        }
    }
}

function testListBindingPatternWithRest4() {
    assertEquals("str", listBindingPattern4([1, 2, "str"]));
}

function listBindingPattern5(any v) returns int|string {
    match v {
        var [a, b, ...c] => {
            if (c[0] is string) {
                return <string> checkpanic c[0];
            } else if (c[0] is int) {
                return <int> checkpanic c[0];
            }
        }
        _ => {
            return "No match";
        }
    }
    return "No match";
}

function testListBindingPatternWithRest5() {
    assertEquals(2, listBindingPattern5([1, "str", 2, 3]));
    assertEquals("s", listBindingPattern5([1, "str", "s", 3]));
    assertEquals("No match", listBindingPattern5([1]));
}

function listBindingPattern6([int, [int, int, int]] v) returns int {
    match v {
        var [a, [b, ...c]] => {
            return c[0];
        }
    }
}

function testListBindingPatternWithRest6() {
    assertEquals(3, listBindingPattern6([1, [2, 3, 4]]));
}

function listBindingPattern7(int[3] val) returns int {
    match val {
        var [a, b, ...c] => {
            return a + b + c[0];
        }
    }
}

function testRestBindingPatternWithClosedArray() {
    assertEquals(6, listBindingPattern7([1, 2, 3]));
}

function listBindingPattern8(json j) returns json[] {
    match j {
        var [x, ...y] => {
            y.push(x);
            return y;
        }
    }
    return [];
}

function testRestBindingPattern8() {
    assertEquals(["hello"], listBindingPattern8(["hello"]));
    assertEquals([1, "hello world"], listBindingPattern8(["hello world", 1]));
    assertEquals(["world", (), "hello"], listBindingPattern8(["hello", "world", ()]));
    assertEquals([], listBindingPattern8([]));
    assertEquals([], listBindingPattern8({}));
    assertEquals([], listBindingPattern8(1));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
