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

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
