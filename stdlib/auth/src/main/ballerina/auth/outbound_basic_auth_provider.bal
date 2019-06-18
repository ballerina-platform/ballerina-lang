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
import ballerina/log;

# Represents outbound Basic auth authenticator.
#
# + credential - Credential configurations
public type OutboundBasicAuthProvider object {

    *OutboundAuthProvider;

    public Credential credential;

    # Provides authentication based on the provided basic auth configuration.
    #
    # + credential - Credential configurations
    public function __init(Credential credential) {
        self.credential = credential;
    }

    # Generate token for Basic authentication.
    #
    # + return - Generated token or `error` if an error occurred during validation
    public function generateToken() returns string|error {
        return getAuthTokenForBasicAuth(self.credential);
    }

    # Inspect the incoming data and generate the token for Basic authentication.
    #
    # + data - Map of data which is extracted from the HTTP response
    # + return - String token, or `error` occurred when generating token or `()` if nothing to be returned
    public function inspect(map<anydata> data) returns string|error? {
        return ();
    }
};

# The `Credential` record can be used to configure Basic authentication used by the HTTP endpoint.
#
# + username - Username for Basic authentication
# + password - Password for Basic authentication
public type Credential record {|
    string username;
    string password;
|};

# Process the auth token for basic auth.
#
# + credential - Credential configurations
# + return - Auth token or `error` if an error occurred during validation
function getAuthTokenForBasicAuth(Credential credential) returns string|error {
    string username = credential.username;
    string password = credential.password;
    if (username == EMPTY_STRING || password == EMPTY_STRING) {
        return prepareError("Username or password cannot be empty.");
    }
    string str = username + ":" + password;
    string token = encoding:encodeBase64(str.toByteArray("UTF-8"));
    log:printDebug(function () returns string {
        return "Authorization header is generated for basic auth scheme.";
    });
    return token;
}
