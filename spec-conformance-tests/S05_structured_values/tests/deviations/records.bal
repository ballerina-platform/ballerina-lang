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

// ● if the record-rest-descriptor is empty, then the value may contain extra fields
// belonging to any pure type
// TODO: Need to test adding errors to a default open record (not closed, no rest field)
// https://github.com/ballerina-platform/ballerina-lang/issues/13181
@test:Config {
    groups: ["deviation"]
}
function testDefaultOpenRecordWithErrorValuesBroken() {
    // string s1 = "test string 1";
    // error e1 = error("test error", { message: "test error message" });
    // error e2 = error("test error two", { message: "test error message two" });
    // OpenRecord r1 = { fieldOne: s1, fieldTwo: e1 };
    // r1.fieldThree = e2;
    // test:assertEquals(r1.fieldOne, s1);
    // test:assertEquals(r1.fieldTwo, e1);
    // test:assertEquals(r1.fieldThree, e2);
}
