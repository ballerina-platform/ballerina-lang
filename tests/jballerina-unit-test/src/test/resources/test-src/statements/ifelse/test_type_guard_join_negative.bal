// Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function testElseIfTerminatingBody(int|boolean|float x, boolean condition) {
    if x is float {
    } else if condition {
        return;
    }

    // All three members can reach this assignment.
    string y = x;
}

function testOptionalElseIfTerminatingBody(string? value) {
    if value is () {
    } else if value.length() > 10 {
        return;
    }
    int y = value;
}

function testNestedIfInElse(int|boolean|float x, boolean condition) {
    if x is float {
    } else {
        if condition { return; }
    }
    string y = x;
}

function testElseIfCompletingBody(int|boolean|float x, boolean condition) {
    if x is float {
    } else if condition {
    }
    string y = x;
}

function testOptionalElseIfWithTrailingElse(string? value) {
    if value is () {
    } else if value.length() > 10 {
        return;
    } else {
    }
    int y = value;
}
