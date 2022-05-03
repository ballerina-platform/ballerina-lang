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

    errors:IOError|error res = trap errors:getDistinctErrorNegative("UserError");
    test:assertTrue(res is error);
    error e = <error> res;
    test:assertValueEqual(e.message(), "'class java.lang.String' is not from a valid java runtime class. " +
        "It should be a subclass of one of the following: java.lang.Number, java.lang.Boolean or " +
        "from the package 'io.ballerina.runtime.api.values'");

    error err = trap errors:getErrorNegative1("error message");
    test:assertValueEqual(err.message(), "'class java.lang.String' is not from a valid java runtime class. " +
        "It should be a subclass of one of the following: java.lang.Number, java.lang.Boolean or " +
        "from the package 'io.ballerina.runtime.api.values'");

    err = trap errors:getErrorWithTypeNegative("error message");
    test:assertValueEqual(err.message(), "'class java.lang.String' is not from a valid java runtime class. " +
        "It should be a subclass of one of the following: java.lang.Number, java.lang.Boolean or " +
        "from the package 'io.ballerina.runtime.api.values'");

    err = trap errors:getErrorNegative2("error message");
    test:assertValueEqual(err.message(), "'class java.lang.String' is not from a valid java runtime class. " +
            "It should be a subclass of one of the following: java.lang.Number, java.lang.Boolean or " +
            "from the package 'io.ballerina.runtime.api.values'");

    err = trap errors:getDistinctErrorWithNullDetailNegative("error message");
    test:assertValueEqual(err.message(), "No such error: error message");

    err = trap errors:getErrorWithEmptyDetailNegative("error message");
    test:assertValueEqual(err.message(), "error message");

    err = trap errors:getErrorWithNullDetailNegative("error message");
    test:assertValueEqual(err.message(), "error message");

    err = trap errors:getErrorWithEmptyDetailNegative2("error message");
    test:assertValueEqual(err.message(), "error message");

    err = trap errors:getErrorWithNullDetailNegative2("error message");
    test:assertValueEqual(err.message(), "error message");

    err = trap errors:getDistinctErrorWithEmptyDetailNegative2("error message");
    test:assertValueEqual(err.message(), "error message");

    err = trap errors:getDistinctErrorWithNullDetailNegative2("error message");
    test:assertValueEqual(err.message(), "error message");
}

function testTypeIds() {
    error userError = error errors:UserError("Whoops!");
    string[] types = errors:getTypeIds(userError);
    test:assertValueEqual(types.length(), 2);
    test:assertValueEqual(types[0], "UserError");
    test:assertValueEqual(types[1], "GenericError");
}
