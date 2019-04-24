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

// union-type-descriptor := type-descriptor | type-descriptor

// The value space of a union type T1|T2 is the union of T1 and T2.
type FLOAT_ONE 1.0;
type FLOAT_TWO 2.0;
type FLOAT_ONE_OR_TWO FLOAT_ONE|FLOAT_TWO;

@test:Config {}
function testUnionTypeDescriptors() {
    string sv = "test string 1";
    int iv = 1000;

    string|int si = sv;
    test:assertEquals(si, sv, msg = "expected variable to hold the assigned value");

    si = iv;
    test:assertEquals(si, iv, msg = "expected variable to hold the assigned value");

    map<string|int> m1 = { one: sv };
    m1.two = iv;
    map<any> anyMap = m1;
    test:assertEquals(m1.one, sv, msg = "expected value to be the assigned value");
    test:assertEquals(m1.two, iv, msg = "expected value to be the assigned value");

    utils:assertPanic(function () { anyMap["three"] = 1.0f; },
                      "{ballerina}InherentTypeViolation",
                      "invalid error on inherent type violating map insertion");

    FLOAT_ONE_OR_TWO fot = 1.0;
    test:assertEquals(fot, 1.0, msg = "expected variable to hold the assigned value");

    fot = 2.0;
    test:assertEquals(fot, 2.0, msg = "expected variable to hold the assigned value");

    map<FLOAT_ONE_OR_TWO> m2 = { one: 1.0 };
    m2.two = 2.0;
    anyMap = m2;
    test:assertEquals(m2.one, 1.0, msg = "expected value to be the assigned value");
    test:assertEquals(m2.two, 2.0, msg = "expected value to be the assigned value");

    utils:assertPanic(function () { anyMap["three"] = 3.0f; },
                      "{ballerina}InherentTypeViolation",
                      "invalid error on inherent type violating map insertion");
}
