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

function listMatchPattern1(any v) returns string {
    match v {
        [1, ...var a] => {
            return "match";
        }
    }
    return "No match";
}

function testListMatchPatternWithRest1() {
    assertEquals("match", listMatchPattern1([1]));
    assertEquals("match", listMatchPattern1([1, 2]));
    assertEquals("match", listMatchPattern1([1, 2, 3]));
}

function listMatchPattern2([int, int] v) returns int {
    match v {
        [1, ...var a] => {
            return a[0];
        }
    }
    return -1;
}

function testListMatchPatternWithRest2() {
    assertEquals(2, listMatchPattern2([1, 2]));
}

function listMatchPattern3([int, int, int] v) returns int {
    match v {
        [1, 2, ...var a] | [1, ...var a] => {
            return a[0];
        }
    }
    return -1;
}

function testListMatchPatternWithRest3() {
    assertEquals(3, listMatchPattern3([1, 2, 3]));
    assertEquals(4, listMatchPattern3([1, 4, 3]));
}

function listMatchPattern4([int, int, string] v) returns int|string {
    match v {
        [1, 2, ...var a] => {
            return a[0];
        }
    }
    return -1;
}

function testListMatchPatternWithRest4() {
    assertEquals("str", listMatchPattern4([1, 2, "str"]));
}

function listMatchPattern5(any v) returns int|string {
    match v {
        [1, "str", ...var a] => {
            if (a[0] is string) {
                return <string> checkpanic a[0];
            } else if (a[0] is int) {
                return <int> checkpanic a[0];
            }
        }
        [1, ...var a] => {
            if (a[0] is string) {
                return <string> checkpanic a[0];
            } else if (a[0] is int) {
                return <int> checkpanic a[0];
            }
        }
        _ => {
            return "No match";
        }
    }
    return "No match";
}

function testListMatchPatternWithRest5() {
    assertEquals(2, listMatchPattern5([1, "str", 2, 3]));
    assertEquals("s", listMatchPattern5([1, "str", "s", 3]));
    assertEquals(2, listMatchPattern5([1, 2, "s", 3]));
    assertEquals("No match", listMatchPattern5([1, true, "s", 3]));
}

function listMatchPattern6(anydata a) returns anydata {
    match a {
        [var p, ...var oth] if p is any[] => {
            anydata[] m = p;
            return m;
        }
        _ => {
            return "other";
        }
    }
}

function listMatchPattern7((int|error)[][] a) returns [int[], (int|error)[][]]|string {
    match a {
        [var p, ...var oth] if p is anydata => {
            int[] m = p;
            (int|error)[][] n = oth;
            return [m, n];
        }
        _ => {
            return "other";
        }
    }
}

function testListMatchPatternWithRestPatternWithArrayAndAnydataIntersection() {
    int[] m1 = [1, 2];
    anydata[] m2 = [m1, "hello"];
    assertEquals(m1, listMatchPattern6(m2));
    anydata[] m3 = [1, "foo"];
    assertEquals("other", listMatchPattern6(m3));
    assertEquals("other", listMatchPattern6("foo"));

    (int|error)[] m4 = [1, error("error!")];
    [int[], (int|error)[][]] res = <[int[], (int|error)[][]]> listMatchPattern7([m1, m4]);
    assertEquals(m1, res[0]);
    assertEquals(true, m4 === res[1][0]);
    assertEquals(1, res[1].length());
    assertEquals("other", <string> listMatchPattern7([[error("error!")]]));
    assertEquals("other", <string> listMatchPattern7([]));
}

function listMatchPattern8(int[3] val) returns int {
    match val {
        [var a, var b, ...var c] => {
            return a + b + c[0];
        }
    }
}

function testListMatchPatternWithClosedArray() {
    assertEquals(6, listMatchPattern8([1, 2, 3]));
}

function listMatchPatternWithRestPattern11(json j) returns json[] {
    match j {
        [var x, ...var y] => {
            y.push(x);
            return y;
        }
    }
    return [];
}

function testListMatchPatternWithRestPattern11() {
    assertEquals(["hello"], listMatchPatternWithRestPattern11(["hello"]));
    assertEquals([1, "hello world"], listMatchPatternWithRestPattern11(["hello world", 1]));
    assertEquals(["world", (), "hello"], listMatchPatternWithRestPattern11(["hello", "world", ()]));
    assertEquals([], listMatchPatternWithRestPattern11([]));
    assertEquals([], listMatchPatternWithRestPattern11({}));
    assertEquals([], listMatchPatternWithRestPattern11(1));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
