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

# Represents the outbound Basic Auth authenticator.
#
# + credential - The credential configurations.
public type OutboundBasicAuthProvider object {

    *OutboundAuthProvider;

    public Credential credential;

    # Provides authentication based on the provided Basic Auth configuration.
    #
    # + credential - The credential configurations.
    public function __init(Credential credential) {
        self.credential = credential;
    }

    # Generates a token for Basic authentication.
    #
    # + return - The generated token or the `error` if an error occurred during validation.
    public function generateToken() returns string|error {
        return getAuthTokenForBasicAuth(self.credential);
    }

    # Inspects the incoming data and generates the token for Basic authentication.
    #
    # + data - Map of the data, which is extracted from the HTTP response.
    # + return - The String token, the `error` occurred when generating the token, or `()` if nothing is to be returned.
    public function inspect(map<anydata> data) returns string|error? {
        return ();
    }
};

# The `Credential` record can be used to configure Basic authentication, which is used by the HTTP endpoint.
#
# + username - The username for Basic authentication.
# + password - The password for Basic authentication.
public type Credential record {|
    string username;
    string password;
|};

# Processes the auth token for Basic Auth.
#
# + credential - The credential configurations.
# + return - The auth token or the `error` if an error occurred during validation.
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
