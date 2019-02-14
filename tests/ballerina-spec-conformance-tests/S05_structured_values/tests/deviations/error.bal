// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;

// An error value belongs to the error basic type, which is a basic type which is distinct from
// other structured types and is used only for representing errors. An error value contains the
// following information:
// ● a reason, which is a string identifier for the category of error
// ● a detail, which is a frozen mapping providing additional information about the error
// ● a stack trace
// TODO: Non-frozen values are accepted for the detail. Stack trace is not supported.
// https://github.com/ballerina-platform/ballerina-lang/issues/13171
@test:Config {
    groups: ["deviation"]
}
function testErrorTypeDescriptorBroken() {
    error <string, map<anydata>> error1 = error("Error Three", { cause: "Core Error" });
    error1.detail().stacktrace = "xyz"; // Should fail
    test:assertEquals(error1.detail(), <map<anydata>>{ cause: "Core Error" },
        msg = "expected detail map in error to be changed");
}

// detail-type-descriptor must be a subtype of record { } (which is equivalent to map<anydata|error>).
// TODO: The detail type descriptor must be a subtype of map<anydata|error>.
// https://github.com/ballerina-platform/ballerina-lang/issues/13205
@test:Config {}
function testErrorTypeDescriptorsDetailValueBroken() {
    error <string, map<anydata>> error1 = error("Error One", { detail: "failed" });
}

// An error value contains the following information:
// ● a stack trace
// TODO: Provide a function to extract the stack trace of an error.
// https://github.com/ballerina-platform/ballerina-lang/issues/13206
@test:Config {}
function testErrorTypeDescriptorsStackTraceBroken() {
    error <string, map<anydata>> error1 = error("Error One", { detail: "failed" });
    // test:assertNotEquals(error1.stackTrace(), (), msg = "expected stack trace to be a non-nil value");
}
