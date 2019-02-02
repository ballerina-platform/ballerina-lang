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

// Both kinds of type descriptor are covariant in the types of their members.
@test:Config {}
function testMapCovariance() {
    string st1 = "test string 1";
    string st2 = "test string 2";
    string st3 = "test string 3";
    map<string> stringMap = { one: st1, two: st2 };
    map<string|int> stringOrIntMap = stringMap;
    stringOrIntMap.three = st3;

    test:assertEquals(stringOrIntMap.one, st1, msg = EXPECTED_THE_ORIGINAL_VALUE_FAILURE_MESSAGE);
    test:assertEquals(stringOrIntMap.two, st2, msg = EXPECTED_THE_ORIGINAL_VALUE_FAILURE_MESSAGE);
    test:assertEquals(stringOrIntMap.three, st3, msg = EXPECTED_THE_ORIGINAL_VALUE_FAILURE_MESSAGE);
    utils:assertPanic(function () { stringOrIntMap.four = 1; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATING_ARRAY_INSERTION_FAILURE_MESSAGE);
}
