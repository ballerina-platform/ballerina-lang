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
import utils;

// error-type-descriptor := error [<reason-type-descriptor[, detail-type-descriptor]>]
// reason-type-descriptor := type-descriptor
// detail-type-descriptor := type-descriptor
// The reason-type-descriptor must be a subtype of string; the
// detail-type-descriptor must be a subtype of record { } (which is equivalent to
// map<anydata|error>).
@test:Config {}
function testErrorTypeDescriptor() {
    error error1 = error("Error One");
    error<string> error2 = error("Error Two");
    error<string, map<any>> error3 = error("Error Three", {detail: "failed"});
}
