// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/runtime_api.errors;
import ballerina/lang.test as test;

public function main() {

    error userError = errors:getError("UserError");
    test:assertTrue(userError is errors:GenericError);

    // check cast
    errors:GenericError error1 = <errors:GenericError>userError;
    errors:UserError error2 = <errors:UserError>userError;

    // Negative Tests
    error invalidError = trap errors:getError("UserError2");

    test:assertValueEqual(invalidError.message(), "No such error: UserError2");

    testTypeIds();
}

function testTypeIds() {
    error userError = error errors:UserError("Whoops!");
    string[] types = errors:getTypeIds(userError);
    test:assertValueEqual(types.length(), 2);
    test:assertValueEqual(types[0], "UserError");
    test:assertValueEqual(types[1], "GenericError");
}

