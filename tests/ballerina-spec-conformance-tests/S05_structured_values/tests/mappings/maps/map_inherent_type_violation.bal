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

// The inherent type of a mapping value must be a mapping-type-descriptor. The
// inherent type of a mapping value determines a type Tf
// for the value of the field with name f.
// The runtime system will enforce a constraint that a value written to field f will belong to type
// Tf. Note that the constraint is not merely that the value looks like Tf.
@test:Config {}
function testMapInherentTypeViolation() {
    map<string> m1 = { one: "test string 1", two: "test string 2" };
    map<any> anyMap = m1;
    utils:assertPanic(function() { anyMap["three"] = 3; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      "invalid reason on inherent type violating map insertion");

    map<map<string>> m2 = { one: { strOne: "test string 1", strTwo: "test string 2" } };
    anyMap = m2;
    // `m3` looks like `map<string>`
    map<string|int> m3 = { one: "test string 1", two: "test string 2" };
    utils:assertPanic(function() { anyMap["two"] = m3; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      "invalid reason on inherent type violating map insertion");
}
