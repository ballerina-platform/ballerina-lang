// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# Represents the `auth:InvocationContext`.
#
# + scheme - Authentication scheme
# + token - Token for the provided `scheme`
# + userId - User ID of the authenticated user
# + claims - Claims of the authenticated user
# + scopes - Scopes of the authenticated user
public type InvocationContext record {|
    string scheme?;
    string token?;
    string userId?;
    map<any> claims?;
    string[] scopes?;
|};

# Returns the `auth:InvocationContext` instance.
# ```ballerina
# auth:InvocationContext invocationContext = auth:getInvocationContext();
# ```
#
# + return - The `auth:InvocationContext` instance
public function getInvocationContext() returns InvocationContext = @java:Method {
    'class: "org.ballerinalang.stdlib.auth.nativeimpl.GetInvocationContext"
} external;

# Sets the authentication-related values to the invocation context.
# ```ballerina
# auth:setInvocationContext("jwt", "<credential>", "<userID>", <scopes>, <claims>);
# ```
#
# + scheme - Auth scheme (`JWT`, `LDAP`, `OAuth2`, `Basic`, etc.)
# + token - Auth token (credential)
# + userId - User ID of the authenticated user
# + scopes - Authenticated user scopes
# + claims - Claims of the authenticated user
public function setInvocationContext(string? scheme = (), string? token = (),
                    string? userId = (), string[]? scopes = (), map<any>? claims = ()) {
    InvocationContext invocationContext = getInvocationContext();
    if (!(scheme is ())) {
        invocationContext.scheme = scheme;
    }
    if (!(token is ())) {
        invocationContext.token = token;
    }
    if (!(userId is ()) && userId != "") {
        invocationContext.userId = userId;
    }
    if (!(scopes is ())) {
        invocationContext.scopes = scopes;
    }
    if (!(claims is ())) {
        invocationContext.claims = claims;
    }
}
