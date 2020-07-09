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

type Foo record {
    string a;
    int b?;
};

function removeRequiredOpen() {
    Foo foo = {a : "a", b : 1};
    _ = foo.remove("a");
}

function testRemoveRequiredOpen() {
    error? result = trap removeRequiredOpen();
    if (result is ()) {
        panic error("Expected error, found ()");
    } else {
        assertEquality("Operation not supported: failed to remove field: 'a' is a required field in '.:Foo'", result.message());
    }
}

type Bar record {|
    string a;
    int b?;
|};

function removeRequiredClosed() {
    Bar bar = {a : "a", b : 1};
    _ = bar.remove("a");
}

function testRemoveRequiredClosed() {
    error? result = trap removeRequiredClosed();
    if (result is ()) {
        panic error("Expected error, found ()");
    } else {
        assertEquality("Operation not supported: failed to remove field: 'a' is a required field in '.:Bar'", result.message());
    }
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error("AssertionError: expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
