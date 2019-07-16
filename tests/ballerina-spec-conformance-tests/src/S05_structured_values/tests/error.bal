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

// error-type-descriptor := error [<reason-type-descriptor[, detail-type-descriptor]>]
// reason-type-descriptor := type-descriptor
// detail-type-descriptor := type-descriptor
// The reason-type-descriptor must be a subtype of string; the
// detail-type-descriptor must be a subtype of record { } (which is equivalent to map<anydata|error>).
@test:Config {}
function testErrorTypeDescriptor() {
    error error1 = error("Error One");
    error <string> error2 = error("Error Two");
    error <string, map<anydata|error>> error3 = error("Error Three", { detail: "failed" });
    error <string, map<error>> error4 = error("Error Four", { detailError: error1 });
    test:assertEquals(error4.detail().detailError.reason(), error1.reason(),
        msg = "expected error types detail to support map<error> type");
}

// An error value belongs to the error basic type, which is a basic type which is distinct from
// other structured types and is used only for representing errors. An error value contains the
// following information:
// ● a reason, which is a string identifier for the category of error
// ● a detail, which is a frozen mapping providing additional information about the error
// ● a stack trace
@test:Config {}
function testErrorDetailFrozenness() {
    error<string, map<anydata>> error1 = error("Error Three", { cause: "Core Error" });
    utils:assertPanic(function () { error1.detail().key1 = 1.0; },
                      "{ballerina}InvalidUpdate",
                      "invalid error on error detail update");
}
