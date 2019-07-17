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

// Both kinds of type descriptor are covariant in the types of their members.
@test:Config {}
function testTupleCovariance() {
    (string, boolean) sbTuple = ("string one", true);
    (string|int, float|boolean) unionTuple = sbTuple;

    test:assertEquals(sbTuple[0], "string one", msg = "expected the original value");
    test:assertEquals(sbTuple[1], true, msg = "expected the original value");

    utils:assertPanic(function() { unionTuple[0] = 1; },
                      INHERENT_TYPE_VIOLATION_REASON,
                      INVALID_REASON_ON_INHERENT_TYPE_VIOLATIONG_TUPLE_UPDATE_FAILURE_MESSAGE);
}
