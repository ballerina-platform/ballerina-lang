// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testORRestFieldInOR() {
    records:OpenBar ob = {x: 1.0};
    records:OpenFoo of = {name: "Open Foo"};
    of["ob"] = ob;
    assertValueEquality({name: "Open Foo", ob: {x: 1.0}}, of);
}

function testORRestFieldInCR() {
    records:OpenBar ob = {x: 2.0};
    records:ClosedFoo cf = {name: "Closed Foo"};
    cf["ob"] = ob;

    assertValueEquality({name: "Closed Foo", ob: {x: 2.0}}, cf);
}

function testCRRestFieldInOR() {
    records:ClosedBar cb = {x: 3.0};
    records:OpenFoo2 of = {name: "Open Foo"};
    of["cb"] = cb;
    assertValueEquality({name: "Open Foo", cb: {x: 3.0}}, of);
}

function testCRRestFieldInCR() {
    records:ClosedBar cb = {x: 4.0};
    records:ClosedFoo2 cf = {name: "Closed Foo"};
    cf["cb"] = cb;
    assertValueEquality({name: "Closed Foo", cb: {x: 4.0}}, cf);
}

const ASSERTION_ERROR_REASON = "AssertionError";

isolated function isEqual(anydata|error actual, anydata|error expected) returns boolean {
    if (actual is anydata && expected is anydata) {
        return (actual == expected);
    } else {
        return (actual === expected);
    }
}

function assertValueEquality(anydata|error expected, anydata|error actual) {
    if isEqual(actual, expected) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
