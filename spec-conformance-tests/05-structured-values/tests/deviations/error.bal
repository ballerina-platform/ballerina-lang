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
@test:Config {
    groups: ["broken"]
}
function testErrorTypeDescriptorBroken() {
    map<anydata> detail = {cause: "Core Error"};
    error<string, map<any>> error1 = error("Error Three", detail);
    detail.stacktrace = "xyz";
    test:assertEquals(error1.detail(), detail, msg = "expected detail map in error to be changed");
}

// An error type does not have an implicit initial value.
// TODO: Creating and error array should fail at compile time
@test:Config {
    groups: ["broken"]
}
function testErrorImplicitInitialValueBroken() {
    error[] errorArray = [];
    errorArray[1] = error("Error One");
}
