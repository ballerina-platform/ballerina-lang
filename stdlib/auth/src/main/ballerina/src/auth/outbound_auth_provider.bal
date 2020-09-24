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

# Represents the outbound Auth provider, which could be used to authenticate external endpoints. Any type of
# implementation such as JWT, OAuth2, LDAP, JDBC, file-based etc. should be object-wise similar to the `auth:OutboundAuthProvider` object.
public type OutboundAuthProvider object {

    # Generates an authentication token for the outbound request with outbound Auth providers such as JWT and OAuth2.
    #
    # + return - The token as a `string` or else an `auth:Error` occurred when generating the token
    public function generateToken() returns string|Error;

    # Inspects the incoming data and generates the authentication token as needed. For example, if the incoming data
    # indicates that it needs to regenerate the token because the previously-generated token is invalid, this method
    # will generate it.
    #
    # + data - Map of data, which is extracted from the HTTP response
    # + return - The token as a `string`, an `auth:Error` occurred when generating the token,
    #            or else `()` if nothing is to be returned
    public function inspect(map<anydata> data) returns string|Error?;
};
