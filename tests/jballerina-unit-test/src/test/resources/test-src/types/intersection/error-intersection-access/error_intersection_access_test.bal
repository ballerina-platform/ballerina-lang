// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import error_intersection_access.error_intersection_mod as err_lib;

public function testErrorIntersectionAccessTest() {
    err_lib:RemoteServerError remoteServerErr = error err_lib:RemoteServerError("remote server error");
    assertEquality("remote server error", remoteServerErr.message());

    err_lib:ApplicationResponseError applResponeErr = error err_lib:ApplicationResponseError("application response server error");
    assertEquality("application response server error", applResponeErr.message());

    err_lib:ClientRequestErrorWithStatusCode clientReqErrWithStatusCode =
        error err_lib:ClientRequestErrorWithStatusCode("client request error", code = 404);
    assertEquality("client request error", clientReqErrWithStatusCode.message());
    assertEquality(404, clientReqErrWithStatusCode.detail().code);

    err_lib:ApplicationResponseErrorWithStatusCode applResponseErrWithStatusCode =
        error err_lib:ApplicationResponseErrorWithStatusCode("application response error", code = 401);
    assertEquality("application response error", applResponseErrWithStatusCode.message());
    assertEquality(401, applResponseErrWithStatusCode.detail().code);

    err_lib:BadRequestError badReqErr = error err_lib:BadRequestError("bad request error", code = 400);
    assertEquality("bad request error", badReqErr);
    assertEquality(400, badReqErr.detail().code);

    err_lib:Error err = error err_lib:Error("response error");
    assertEquality("error", err.message());
}

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
