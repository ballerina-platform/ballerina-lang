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
    er:ApplicationError e = error er:ApplicationError(er:APPLICATION_ERROR_REASON, message = "Client has been stopped");
    return e;
}

function getApplicationErrorIndirectCtor() returns error {
    er:ApplicationError e = error er:ApplicationError(er:APPLICATION_ERROR_REASON, message = "Client has been stopped");
    error e1 = error er:ApplicationError(er:APPLICATION_ERROR_REASON, message = "Client has been stopped");
    return e;
}

function getDistinctError() returns error {
    er:OrderCreationError2 k = error er:OrderCreationError2("OrderCreationError2-msg", message = "Client has been stopped");
    er:OrderCreationError f = k;
    return f;
}

type OurError distinct er:OrderCreationError;

function testDistinctTypeFromAnotherPackageInATypeDef() returns error {
    OurError e = error OurError("Our error message", message = "Client has been stopped");
    er:OrderCreationError f = e;
    return f;
}

type OurProccessingError distinct er:OrderCreationError2;

function testDistinctTypeFromAnotherPackageInATypeDefWithACast() returns error {
    OurProccessingError e = error OurProccessingError("Our error message", message = "Client has been stopped");
    error f = e;

    er:OrderCreationError k = <er:OrderCreationError> f; // casting to distinct super type
    return k;
}

function testDistinctTypeFromAnotherPackageInATypeDefWithACastNegative() returns error {
    OurProccessingError e = error OurProccessingError("Our error message", message = "Client has been stopped");
    error f = e;
    er:OrderProcessingError k = <er:OrderProcessingError> f; // casting to unrelated, yet same shaped type.
    return k;
}

function performInvalidCastWithDistinctErrorType() returns error? {
    error? e = trap testDistinctTypeFromAnotherPackageInATypeDefWithACastNegative();
    return e;
}

function testErrorDetailDefinedAfterErrorDef() {
    er:NewPostDefinedError e = error er:NewPostDefinedError("New error", code = "ABCD");
    er:PostDefinedError k = e;
    assertEquality("New error", k.message());
    assertEquality("ABCD", k.detail()["code"]);
}

type ErrorIntersection3 er:ErrorIntersection1 & error<er:Data1>;
type ErrorIntersection4 er:ErrorIntersection1 & er:ErrorIntersection2;

function testErrorIntersection() {
    ErrorIntersection3 e3 = error ErrorIntersection3("Intersection error", num = 2);
    assertEquality("Intersection error", e3.message());
    assertEquality(2, e3.detail()["num"]);

    ErrorIntersection4 e4 = error ErrorIntersection4("Intersection error", num = 3);
    assertEquality("Intersection error", e4.message());
    assertEquality(3, e4.detail()["num"]);
}

type MyDefaultStatusCodeError1 distinct er:DefaultStatusCodeError;
type MyDefaultStatusCodeError2 distinct er:DefaultStatusCodeError & error<record {| int code; boolean fatal; |}>;
type MyDefaultStatusCodeError3 distinct er:DefaultStatusCodeError & distinct error<record {| int code; boolean fatal; |}>;

function testDistinctErrorIntersection() {
    MyDefaultStatusCodeError1 e1 = error MyDefaultStatusCodeError1("Default status code error", code = 404);
    assertEquality("Default status code error", e1.message());
    assertEquality(404, e1.detail()["code"]);

    MyDefaultStatusCodeError2 e2 = error MyDefaultStatusCodeError2("Default status code error", code = 404,
        fatal = true);
    assertEquality("Default status code error", e2.message());
    assertEquality(404, e2.detail()["code"]);

    er:DefaultStatusCodeError e3  = e2;
    assertEquality("Default status code error", e3.message());
    assertEquality(404, e3.detail()["code"]);

    MyDefaultStatusCodeError3 e4 = error MyDefaultStatusCodeError3("Default status code error", code = 404,
        fatal = true);
    er:DefaultStatusCodeError e5 = e4;
    assertEquality("Default status code error", e5.message());
    assertEquality(404, e5.detail()["code"]);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("Assertion Error",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
