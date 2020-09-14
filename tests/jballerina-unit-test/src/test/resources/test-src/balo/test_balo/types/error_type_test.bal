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

import testorg/errors as er;

function getApplicationError() returns error {
    er:ApplicationError e = er:ApplicationError(er:APPLICATION_ERROR_REASON, message = "Client has been stopped");
    return e;
}

function getApplicationErrorIndirectCtor() returns error {
    er:ApplicationError e = er:ApplicationError(er:APPLICATION_ERROR_REASON, message = "Client has been stopped");
    error e1 = er:ApplicationError(er:APPLICATION_ERROR_REASON, message = "Client has been stopped");
    return e;
}

function getDistinctError() returns error {
    er:OrderCreationError2 k = er:OrderCreationError2("OrderCreationError2-msg", message = "Client has been stopped");
    er:OrderCreationError f = k;
    return f;
}

type OurError distinct er:OrderCreationError;

function testDistinctTypeFromAnotherPackageInATypeDef() returns error {
    OurError e = OurError("Our error message", message = "Client has been stopped");
    er:OrderCreationError f = e;
    return f;
}

type OurProccessingError distinct er:OrderCreationError2;

function testDistinctTypeFromAnotherPackageInATypeDefWithACast() returns error {
    OurProccessingError e = OurProccessingError("Our error message", message = "Client has been stopped");
    error f = e;

    er:OrderCreationError k = <er:OrderCreationError> f; // casting to distinct super type
    return k;
}

function testDistinctTypeFromAnotherPackageInATypeDefWithACastNegative() returns error {
    OurProccessingError e = OurProccessingError("Our error message", message = "Client has been stopped");
    error f = e;
    er:OrderProcessingError k = <er:OrderProcessingError> f; // casting to unrelated, yet same shaped type.
    return k;
}

function performInvalidCastWithDistinctErrorType() returns error? {
    error? e = trap testDistinctTypeFromAnotherPackageInATypeDefWithACastNegative();
    return e;
}

function testErrorDetailDefinedAfterErrorDef() {
    er:NewPostDefinedError e = er:NewPostDefinedError("New error", code = "ABCD");
    er:PostDefinedError k = e;
    assertEquality("New error", k.message());
    assertEquality("ABCD", k.detail()["code"]);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError("Assertion Error",
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

type AssertionError error;
