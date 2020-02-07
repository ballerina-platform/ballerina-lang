// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/auth;
import ballerina/log;

# Log and prepare `error` as a `Error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `Error` instance
function prepareError(string message, error? err = ()) returns Error {
    log:printError(message, err);
    Error oauth2Error;
    if (err is error) {
        oauth2Error = error(OAUTH2_ERROR, message = message, cause = err);
    } else {
        oauth2Error = error(OAUTH2_ERROR, message = message);
    }
    return oauth2Error;
}

# Log and prepare `error` as a `auth:Error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `auth:Error` instance
function prepareAuthError(string message, error? err = ()) returns auth:Error {
    log:printError(message, err);
    auth:Error authError;
    if (err is error) {
        authError = error(auth:AUTH_ERROR, message = message, cause = err);
    } else {
        authError = error(auth:AUTH_ERROR, message = message);
    }
    return authError;
}
