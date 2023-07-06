// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/foo.records;

function testDefaultValueFromCETBeingUsedWithReadOnlyFieldsInTheMappingConstructor() {
    records:Corge d = {readonly b: "ballerina"};
    string[] & readonly e = ["x", "y"];
    assertEquality({a: "hello", b: "ballerina", c: e}, d);
    assertTrue(d is record {|string a; readonly string b; string[] c;|});
    assertFalse(d is record {|string a; readonly string b; readonly string[] c;|});

    records:Corge f = {readonly b: "val", readonly c: ["a", "b", "c"]};
    assertEquality({a: "hello", b: "val", c: <readonly> ["a", "b", "c"]}, f);
    assertTrue(f is record {|string a; readonly string b; string[] c;|});
    assertTrue(f is record {|string a; readonly string b; readonly string[] c;|});
    assertFalse(f is record {|readonly string a; readonly string b; readonly string[] c;|});
}

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertFalse(anydata actual) {
    assertEquality(false, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
