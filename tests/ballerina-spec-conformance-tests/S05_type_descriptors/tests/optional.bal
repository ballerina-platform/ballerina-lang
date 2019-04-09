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

// optional-type-descriptor := type-descriptor ?

// A type T? means T optionally, and is exactly equivalent to T|().
@test:Config {}
function testOptionalTypeDescriptors() {
    string sv = "test string 1";

    string? s1 = sv;
    string|() s2 = sv;
    test:assertEquals(s1, sv, msg = "expected variable to hold the assigned value");
    test:assertEquals(s1, s2, msg = "expected values to be identified as equal");

    s1 = ();
    s2 = ();
    test:assertEquals(s1, (), msg = "expected variable to hold the assigned value");
    test:assertEquals(s1, s2, msg = "expected values to be identified as equal");

    map<string?> msn = { one: sv };
    msn.two = ();
    map<any> anyMap = msn;
    test:assertEquals(msn.one, sv, msg = "expected value to be the assigned value");
    test:assertEquals(msn.two, (), msg = "expected value to be the assigned value");

    utils:assertPanic(function () { anyMap["three"] = 1.0d; },
                      "{ballerina}InherentTypeViolation",
                      "invalid error on inherent type violating map insertion");
}
