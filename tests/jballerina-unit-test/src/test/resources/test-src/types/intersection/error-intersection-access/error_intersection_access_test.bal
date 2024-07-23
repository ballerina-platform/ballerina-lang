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

const MESSAGE = "Message";

public function testErrorIntersectionFromImportedModule() {
    err_lib:RemoteServerError remoteServerErr = error err_lib:RemoteServerError(MESSAGE);
    assertError(remoteServerErr);

    err_lib:ApplicationResponseError applResponeErr = error err_lib:ApplicationResponseError(MESSAGE);
    assertError(applResponeErr);

    err_lib:ClientRequestErrorWithStatusCode clientReqErrWithStatusCode = error (MESSAGE, code = 404);
    assertError(clientReqErrWithStatusCode, 404);

    err_lib:ApplicationResponseErrorWithStatusCode appRespErrWithStatusCode = error (MESSAGE, code = 401);
    assertError(appRespErrWithStatusCode, 401);

    err_lib:BadRequestError badReqErr = error err_lib:BadRequestError(MESSAGE, code = 400);
    assertError(badReqErr, 400);

    err_lib:Error err = error err_lib:Error(MESSAGE);
    assertError(err);
}

function assertError(any|error actual, int? code = ()) {
    if actual !is error {
        panic error(string `expected an error, found '${actual.toString()}'`);
    }

    if MESSAGE != actual.message() {
        panic error(string `expected message: '${MESSAGE}', found: '${actual.message()}'`);
    }

    if code != () {
        var detail = <record {|int code;|} & readonly> actual.detail();
        if code != detail.code {
            panic error(string `expected code: '${code}', found: '${detail.code}'`);
        }
    }
}
