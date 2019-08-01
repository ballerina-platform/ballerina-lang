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

import ballerina/encoding;
import ballerina/internal;
import ballerina/log;

# Constant for empty string.
const string EMPTY_STRING = "";

# Default charset to be used with password hashing.
public const string DEFAULT_CHARSET = "UTF-8";

# Prefix used to denote special configuration values.
public const string CONFIG_PREFIX = "@";

# Prefix used to denote that the config value is a SHA-256 hash.
public const string CONFIG_PREFIX_SHA256 = "@sha256:";

# Prefix used to denote that the config value is a SHA-384 hash.
public const string CONFIG_PREFIX_SHA384 = "@sha384:";

# Prefix used to denote that the config value is a SHA-512 hash.
public const string CONFIG_PREFIX_SHA512 = "@sha512:";

# Basic Authentication scheme.
public const string AUTH_SCHEME_BASIC = "Basic ";

# Bearer Authentication scheme.
public const string AUTH_SCHEME_BEARER = "Bearer ";

# The table name of the config user section of the TOML file.
const string CONFIG_USER_SECTION = "b7a.users";

# Extracts the username and password from the credential values.
#
# + credential - The credential values.
# + return - A `string` tuple with the extracted username and password or `Error` occurred while extracting credentials
public function extractUsernameAndPassword(string credential) returns [string, string]|Error {
    string decodedHeaderValue = encoding:byteArrayToString(check encoding:decodeBase64(credential));
    string[] decodedCredentials = internal:split(decodedHeaderValue, ":");
    if (decodedCredentials.length() != 2) {
        return prepareError("Incorrect credential format. Format should be username:password");
    } else {
        return [decodedCredentials[0], decodedCredentials[1]];
    }
}

# Log and prepare `error` as a `Error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `Error` instance
public function prepareError(string message, error? err = ()) returns Error {
    log:printError(message, err);
    Error authError;
    if (err is error) {
        authError = error(AUTH_ERROR, message = message, cause = err);
    } else {
        authError = error(AUTH_ERROR, message = message);
    }
    return authError;
}

# Set the authentication context token and scheme.
#
# + scheme - Auth scheme (JWT, LDAP, OAuth2, Basic etc.)
# + authToken - Auth token (credential)
public function setAuthenticationContext(string scheme, string authToken) {
    runtime:AuthenticationContext? authenticationContext = runtime:getInvocationContext()?.authenticationContext;
    if (authenticationContext is runtime:AuthenticationContext) {
        authenticationContext.scheme = scheme;
        authenticationContext.authToken = authToken;
    }
}
