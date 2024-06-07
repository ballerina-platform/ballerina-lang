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

function testErrorIntersectionAccessTest(int code) returns error? {
    string reason = "response error";

    if code > 500 {
        return error err_lib:RemoteServerError(reason);
    }

    if code == 500 {
        return error err_lib:ApplicationResponseError(reason);
    }

    if code > 400 {
        if code == 404 {
            return error err_lib:ClientRequestErrorWithStatusCode(reason, code = code);
        }

        return error err_lib:ApplicationResponseErrorWithStatusCode(reason, code = code);
    }

    if code == 400 {
        return error err_lib:BadRequestError(reason, code = 0);
    }

    return error err_lib:Error(reason);
}
