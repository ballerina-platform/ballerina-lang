// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import testorg/foo;

public function testObjectEqualityBetweenNonClientAndClientObject() {
    foo:NonClientObject obj1 = new foo:NonClientObject("NonClientObject");
    foo:ClientObjectWithoutRemoteMethod o2 = new foo:ClientObjectWithoutRemoteMethod("ClientObjectWithoutRemoteMethod");

    foo:NonClientObject obj3 = o2;
    foo:ClientObjectWithoutRemoteMethod obj4 = obj1;

    assertEquality("NonClientObject", obj4.name);
    assertEquality("ClientObjectWithoutRemoteMethod", obj3.name);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if (expected is anydata && actual is anydata && expected == actual) {
        return;
    }

    if (expected === actual) {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                 message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
