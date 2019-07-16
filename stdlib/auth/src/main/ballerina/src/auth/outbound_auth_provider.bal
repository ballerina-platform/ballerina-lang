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

# Represents the outbound Auth provider. Any type of implementation such as JWT and OAuth2 should be object-wise similar
# to the `OutboundAuthProvider` object.
public type OutboundAuthProvider abstract object {

    # Generates a token for the outbound request.
    #
    # + return - The String token or the `Error` occurred when generating the token.
    public function generateToken() returns string|Error;

    # Inspects the incoming data and generates the token as needed.
    #
    # + data - Map of the data, which is extracted from the HTTP response.
    # + return - The String token, the `Error` occurred when generating the token, or `()` if nothing is to be returned.
    public function inspect(map<anydata> data) returns string|Error?;
};
