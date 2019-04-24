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

// An error value contains the following information:
// ● a stack trace
// TODO: Provide a function to extract the stack trace of an error.
// https://github.com/ballerina-platform/ballerina-lang/issues/13206
@test:Config {
    groups: ["deviation"]
}
function testErrorTypeDescriptorsStackTraceBroken() {
    error <string, map<anydata>> error1 = error("Error One", { detail: "failed" });
    // test:assertNotEquals(error1.stackTrace(), (), msg = "expected stack trace to be a non-nil value");
}
