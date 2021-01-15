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

function narrowTypeInCaptureBindingPattern1(int|string v) returns string {
    match v {
        var a if a is int => {
            return a.toString();
        }
        _ => {
            return v;
        }
    }
}

function testNarrowTypeInCaptureBindingPattern1() {
    assertEquals("2", narrowTypeInCaptureBindingPattern1(2));
    assertEquals("str", narrowTypeInCaptureBindingPattern1("str"));
}

function narrowTypeInCaptureBindingPattern2(int|string|boolean v) returns string {
    match v {
        var a if a is int => {
            return a.toString();
        }
        var a if a is boolean => {
            return a.toString();
        }
        _ => {
            return v;
        }
    }
}

function testNarrowTypeInCaptureBindingPattern2() {
    assertEquals("str", narrowTypeInCaptureBindingPattern2("str"));
}

function narrowTypeInListBindingPattern1([int|string, int] v) returns string {
    match v {
        [var a, var b] if a is int => {
            return "";
        }
        _ => {
            return v[0];
        }
    }
}

function testNarrowTypeInListBindingPattern1() {
    assertEquals("str", narrowTypeInListBindingPattern1(["str", 2]));
}

function narrowTypeInListBindingPattern2([int|string|boolean, int] v) returns string {
    match v {
        [var a, var b] if a is int => {
            return "int";
        }
        [var a, var b] if a is boolean => {
            return "boolean";
        }
        _ => {
            return v[0];
        }
    }
}

function testNarrowTypeInListBindingPattern2() {
    assertEquals("str", narrowTypeInListBindingPattern2(["str", 2]));
}

function narrowTypeInListBindingPattern3([int|string, int|string] v) returns string {
    match v {
        [var a, var b] if a is int && b is int=> {
            return "match1";
        }
        [var a, var b] if a is int && b is string=> {
            return "match2";
        }
        [var a, var b] if a is string && b is int=> {
            return "match3";
        }
        _ => {
            return v[0] + v[1];
        }
    }
}

function testNarrowTypeInListBindingPattern3() {
    assertEquals("Hello World", narrowTypeInListBindingPattern3(["Hello ", "World"]));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
