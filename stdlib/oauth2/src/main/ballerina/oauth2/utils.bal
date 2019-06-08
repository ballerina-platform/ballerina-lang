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

import ballerina/log;

const string EMPTY_STRING = "";
const string WHITE_SPACE = " ";

# Specifies the type of the OAuth2 grant type
public type OAuth2GrantType CLIENT_CREDENTIALS_GRANT|PASSWORD_GRANT|DIRECT_TOKEN;

# Indicates OAuth2 client credentials grant type
public const CLIENT_CREDENTIALS_GRANT = "CLIENT_CREDENTIALS_GRANT";

# Indicates OAuth2 password grant type
public const PASSWORD_GRANT = "PASSWORD_GRANT";

# Indicates `direct token` as a grant type, where this is considered as a custom way of providing access tokens by the user
public const DIRECT_TOKEN = "DIRECT_TOKEN";

# Constant for the auth error code.
public const OAUTH2_ERROR_CODE = "{ballerina/oauth2}OAuth2Error";

# Log, prepare and return the `error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `error` instance
function prepareError(string message, error? err = ()) returns error {
    log:printError(message, err = err);
    error preparedError = error(OAUTH2_ERROR_CODE, { message: message, reason: err.reason() });
    return preparedError;
}
