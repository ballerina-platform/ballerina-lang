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
        [1] => {
            return "[1]";
        }
        [1, 2] => {
            return "[1, 2]";
        }
        [1, 2, 3] => {
            return "[1, 2, 3]";
        }
        [1, "s"] => {
            return "[1, 's']";
        }
        [1, "s", 2] => {
            return "[1, 's', 2]";
        }
        [true, "s"] => {
            return "[true, 's']";
        }
        [true, false] => {
            return "[true, false]";
        }
        [true, 1, false] => {
            return "[true, 1, false]";
        }
    }

    return "No match";
}

function testListMatchPattern1() {
        assertEquals("[1]", listMatchPattern1([1]));
        assertEquals("[1, 2]", listMatchPattern1([1, 2]));
        assertEquals("[1, 2, 3]", listMatchPattern1([1, 2, 3]));
        assertEquals("[1, 's']", listMatchPattern1([1, "s"]));
        assertEquals("[1, 's', 2]", listMatchPattern1([1, "s", 2]));
        assertEquals("[true, 's']", listMatchPattern1([true, "s"]));
        assertEquals("[true, false]", listMatchPattern1([true, false]));
        assertEquals("[true, 1, false]", listMatchPattern1([true, 1, false]));
        assertEquals("No match", "No match");
}

function listMatchPattern2(any v) returns string {
    match v {
        [[1]] => {
            return "[[1]]";
        }
        [1, [2]] => {
            return "[1, [2]]";
        }
        [[1], [2]] => {
             return "[[1], [2]]";
        }
        [1, [2, 3]] => {
            return "[1, [2, 3]]";
        }
        [[1, "s"]] => {
            return "[[1, 's']]";
        }
        [[[1, "str"], "s"], 2] => {
            return "[[[1, 'str'], 's'], 2]";
        }
        [[true, 2], "s"] => {
            return "[[true, 2], 's']";
        }
        [[true, false], [true, 2]] => {
            return "[[true, false], [true, 2]]";
        }
        [[true, "s1"], [1, 2, "s2"], [false, "s3"]] => {
            return "[[true, 's1'], [1, 2, 's2'], [false, 's3']]";
        }
    }

    return "No match";
}

function testListMatchPattern2() {
    assertEquals("[[1]]", listMatchPattern2([[1]]));
    assertEquals("[1, [2]]", listMatchPattern2([1, [2]]));
    assertEquals("[[1], [2]]", listMatchPattern2([[1], [2]]));
    assertEquals("[1, [2, 3]]", listMatchPattern2([1, [2, 3]]));
    assertEquals("[[1, 's']]", listMatchPattern2([[1, "s"]]));
    assertEquals("[[[1, 'str'], 's'], 2]", listMatchPattern2([[[1, "str"], "s"], 2]));
    assertEquals("[[true, 2], 's']", listMatchPattern2([[true, 2], "s"]));
    assertEquals("[[true, false], [true, 2]]", listMatchPattern2([[true, false], [true, 2]]));
    assertEquals("[[true, 's1'], [1, 2, 's2'], [false, 's3']]",
                            listMatchPattern2([[true, "s1"], [1, 2, "s2"], [false, "s3"]]));
    assertEquals("No match", "No match");
}

const CONST1 = "Ballerina";
const CONST2 = 200;

function listMatchPattern3(any v) returns string {
    match v {
        [CONST1] => {
            return "[CONST1]";
        }
        [CONST1, CONST2] => {
            return "[CONST1, CONST2]";
        }
        [[CONST1, CONST2]] => {
            return "[[CONST1, CONST2]]";
        }
        [[CONST1], [CONST2]] => {
            return "[[CONST1], [CONST2]]";
        }
        [[CONST1], [CONST2], [CONST1, CONST2]] => {
            return "[[CONST1], [CONST2], [CONST1, CONST2]]";
        }
    }

    return "No match";
}

function testListMatchPattern3() {
    assertEquals("[CONST1]", listMatchPattern3([CONST1]));
    assertEquals("[CONST1]", listMatchPattern3(["Ballerina"]));
    assertEquals("[CONST1, CONST2]", listMatchPattern3(["Ballerina", 200]));
    assertEquals("[CONST1, CONST2]", listMatchPattern3([CONST1, CONST2]));
    assertEquals("[[CONST1, CONST2]]", listMatchPattern3([[CONST1, CONST2]]));
    assertEquals("[[CONST1, CONST2]]", listMatchPattern3([["Ballerina", 200]]));
    assertEquals("[[CONST1], [CONST2]]", listMatchPattern3([[CONST1], [CONST2]]));
    assertEquals("[[CONST1], [CONST2]]", listMatchPattern3([["Ballerina"], [200]]));
    assertEquals("[[CONST1], [CONST2], [CONST1, CONST2]]", listMatchPattern3([[CONST1], [CONST2], [CONST1, CONST2]]));
    assertEquals("[[CONST1], [CONST2], [CONST1, CONST2]]",
                                                    listMatchPattern3([["Ballerina"], [200], ["Ballerina", 200]]));
    assertEquals("No match", "No match");
}

function listMatchPattern4([int, string, CONST1] v) returns string {
    match v {
        [1, "str", CONST1] => {
            return "[1, 'str', CONST1]";
        }
    }

    return "No match";
}

function testListMatchPattern4() {
    assertEquals("[1, 'str', CONST1]", listMatchPattern4([1, "str", CONST1]));
    assertEquals("No match", listMatchPattern4([2, "str", CONST1]));
}

function listMatchPattern5([int, string, CONST1|CONST2] v) returns string {
    match v {
        [1, "s", CONST1] => {
            return "[1, 's', CONST1]";
        }
        [2, "a", CONST2] => {
            return "[2, 'a', CONST2]";
        }
        _ => {
            return "No match";
        }
    }
}

function testListMatchPattern5() {
    assertEquals("[1, 's', CONST1]", listMatchPattern5([1, "s", CONST1]));
    assertEquals("[2, 'a', CONST2]", listMatchPattern5([2, "a", CONST2]));
    assertEquals("No match", listMatchPattern5([3, "a", CONST2]));
}

function listMatchPattern6(any v) returns string {
    match v {
        [var a] => {
            return "[var a]";
        }
        [var a, var b] => {
            return "[var a, var b]";
        }
        [var a, var b, var c] => {
            return "[var a, var b, var c]";
        }
    }
    return "No match";
}

function testListMatchPattern6() {
    assertEquals("[var a]", listMatchPattern6([1]));
    assertEquals("[var a, var b]", listMatchPattern6([1, "str"]));
    assertEquals("[var a, var b, var c]", listMatchPattern6([1, "str", true]));
    assertEquals("No match", listMatchPattern6(1));
}

function listMatchPattern7(any v) returns string {
    match v {
        [var a, var b] if a is int => {
            return "[var a, var b] if a is int";
        }
        [var a, var b] if b is int => {
            return "[var a, var b] if b is int";
        }
    }
    return "No match";
}

function testListMatchPattern7() {
    assertEquals("[var a, var b] if a is int", listMatchPattern7([1, 2]));
    assertEquals("[var a, var b] if b is int", listMatchPattern7(["str", 2]));
    assertEquals("No match", listMatchPattern7([1]));
}

function listMatchPattern8(any v) returns string {
    match v {
        [1] | [1, 2] | [1, 2, 3] => {
            return "match1";
        }
        [1, "s"] | [1, "s", 2] => {
            return "match2";
        }
        [true, "s"] | [true, false] | [true, 1, false] => {
            return "match3";
        }
    }

    return "No match";
}

function testListMatchPattern8() {
    assertEquals("match1", listMatchPattern8([1]));
    assertEquals("match1", listMatchPattern8([1, 2]));
    assertEquals("match1", listMatchPattern8([1, 2, 3]));
    assertEquals("match2", listMatchPattern8([1, "s"]));
    assertEquals("match2", listMatchPattern8([1, "s", 2]));
    assertEquals("match3", listMatchPattern8([true, "s"]));
    assertEquals("match3", listMatchPattern8([true, false]));
    assertEquals("match3", listMatchPattern8([true, 1, false]));
    assertEquals("No match", "No match");
}

function listMatchPattern9(any v) returns string {
    match v {
        [CONST1] | [CONST1, CONST2] => {
            return "match1";
        }
        [[CONST1, CONST2]] | [[CONST1], [CONST2]] => {
            return "match2";
        }
    }

    return "No match";
}

function testListMatchPattern9() {
    assertEquals("match1", listMatchPattern9([CONST1]));
    assertEquals("match1", listMatchPattern9(["Ballerina"]));
    assertEquals("match1", listMatchPattern9(["Ballerina", 200]));
    assertEquals("match1", listMatchPattern9([CONST1, CONST2]));
    assertEquals("match2", listMatchPattern9([[CONST1, CONST2]]));
    assertEquals("match2", listMatchPattern9([["Ballerina", 200]]));
    assertEquals("match2", listMatchPattern9([[CONST1], [CONST2]]));
    assertEquals("match2", listMatchPattern9([["Ballerina"], [200]]));
    assertEquals("No match", "No match");
}

function listMatchPattern10(any v) returns string {
    match v {
        [var a, 5] | [var a, 7] => {
            return "match1";
        }
        [var a, var b, 5] | [var a, var b, 7] => {
            return "match2";
        }
    }

    return "No match";
}

function testListMatchPattern10() {
    assertEquals("match1", listMatchPattern10([2, 5]));
    assertEquals("match1", listMatchPattern10([2, 7]));
    assertEquals("match1", listMatchPattern10(["s", 7]));
    assertEquals("match2", listMatchPattern10(["s", true, 5]));
    assertEquals("match2", listMatchPattern10([10, false, 7]));
    assertEquals("No match", listMatchPattern10([1]));
}

function listMatchPattern11([int, int] v) returns string {
    match v {
        [var a, 5] | [var a, 7] => {
            return "match1";
        }
        [var a, 2] | [3, var a] => {
            return "match2";
        }
    }

    return "No match";
}

function testListMatchPattern11() {
    assertEquals("match1", listMatchPattern11([2, 5]));
    assertEquals("match1", listMatchPattern11([2, 7]));
    assertEquals("match2", listMatchPattern11([2, 2]));
    assertEquals("match2", listMatchPattern11([3, 8]));
    assertEquals("No match", listMatchPattern11([1, 1]));
}

function listMatchPattern12([int, int] v) returns int|string {
    match v {
        [var a, 5] | [var a, 7] => {
            return a;
        }
        [var a, 2] | [3, var a] => {
            return a + 20;
        }
    }

    return "No match";
}

function testListMatchPattern12() {
    assertEquals(2, listMatchPattern12([2, 5]));
    assertEquals(2, listMatchPattern12([2, 7]));
    assertEquals(22, listMatchPattern12([2, 2]));
    assertEquals(28, listMatchPattern12([3, 8]));
    assertEquals("No match", listMatchPattern12([1, 1]));
}

function listMatchPattern13([int, int, int]|[boolean, boolean, boolean] v) returns int|boolean {
    match v {
        [var a, var b, 5] | [var a, var b, false] => {
            return a;
        }
    }

    return false;
}

function testListMatchPattern13() {
    assertEquals(2, listMatchPattern13([2, 2, 5]));
    assertEquals(true, listMatchPattern13([true, true, false]));
    assertEquals(false, listMatchPattern13([1, 1, 1]));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
