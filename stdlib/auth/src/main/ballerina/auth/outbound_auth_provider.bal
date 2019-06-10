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

# Represents the outbound auth provider. Any type of implementation, such as JWT, OAuth2 should be object-wise similar
# to `OutboundAuthProvider` object.
public type OutboundAuthProvider abstract object {

    # Generates token for the outbound request.
    #
    # + return - String token, or `error` occurred when generating token
    public function generateToken() returns string|error;

    # Inspect the incoming data and generate the token as needed.
    #
    # + data - Map of data which is extracted from the HTTP response
    # + return - String token, or `error` occurred when generating token or `()` if nothing to be returned
    public function inspect(map<anydata> data) returns string|error?;
};
