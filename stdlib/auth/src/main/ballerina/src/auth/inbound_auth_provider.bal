// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Represents the inbound Auth provider, which could be used to authenticate endpoints. The `auth:InboundAuthProvider` acts
# as the interface for all the inbound authentication providers. Any type of implementation such as JWT, OAuth2,
# LDAP, JDBC, file-based etc. should be object-wise similar.
public type InboundAuthProvider object {

    # Authenticates the user based on the user credentials (i.e., the username/password) or a token such as JWT or
    # OAuth2.
    #
    # + credential - The `string` credential value
    # + return - `true` if the authentication is successful, `false` otherwise, or else an `auth:Error` in case of an error
    public function authenticate(string credential) returns boolean|Error;
};
