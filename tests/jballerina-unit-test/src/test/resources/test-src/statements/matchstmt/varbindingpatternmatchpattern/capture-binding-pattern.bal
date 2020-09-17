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

function captureBindingPattern1(any v) returns string {
    match v {
        var a => {
            return "Matched";
        }
    }
}

function testCaptureBindingPattern1() {
    assertEquals(captureBindingPattern1(2), "Matched");
    assertEquals(captureBindingPattern1("str"), "Matched");
    assertEquals(captureBindingPattern1(true), "Matched");
}

function captureBindingPattern2(any v) returns anydata {
    match v {
        var a if a is int => {
            return a;
        }
        var a if a is string => {
            return a;
        }
        var a if a is boolean => {
            return a;
        }
    }

    return "Not matched.";
}

function testCaptureBindingPattern2() {
    assertEquals(captureBindingPattern2(2), 2);
    assertEquals(captureBindingPattern2("str"), "str");
    assertEquals(captureBindingPattern2(true), true);
    assertEquals(captureBindingPattern2([]), "Not matched.");
}

function captureBindingPattern3(any v) returns string {
    match v {
        var a if a is int && a == 2 => {
            return "a == 2";
        }
        var a if a is string && a == "str" => {
            return "a == str";
        }
        var a if a is boolean && a == true => {
            return "a == true";
        }
    }

    return "Not matched.";
}

function testCaptureBindingPattern3() {
    assertEquals(captureBindingPattern3(2), "a == 2");
    assertEquals(captureBindingPattern3("str"), "a == str");
    assertEquals(captureBindingPattern3(true), "a == true");
    assertEquals(captureBindingPattern3("Not matched"), "Not matched.");
}

function captureBindingPattern4(int v) returns string {
    string s;

    match v {
        1 => {
            s = "ONE";
        }
        2 => {
            s = "TWO";
        }
        3 => {
            s = "THREE";
        }
        var a => {
            s = "OTHER";
        }
    }
    return s;
}

function testCaptureBindingPattern4() {
    assertEquals("ONE", captureBindingPattern4(1));
    assertEquals("TWO", captureBindingPattern4(2));
    assertEquals("THREE", captureBindingPattern4(3));
    assertEquals("OTHER", captureBindingPattern4(4));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
