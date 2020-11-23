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

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}