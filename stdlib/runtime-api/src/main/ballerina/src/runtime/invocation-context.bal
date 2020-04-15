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

import ballerina/java;

# Represents the `runtime:InvocationContext`.
#
# + id - Unique ID generated when initiating the invocation context
# + principal - User principal instance
# + authenticationContext - Authentication context instance
# + attributes - Map of context attributes
public type InvocationContext record {|
    string id;
    Principal principal?;
    AuthenticationContext authenticationContext?;
    map<any> attributes;
|};

# Represents the `runtime:AuthenticationContext` populated with the authenticated information.
#
# + scheme - Authentication scheme
# + authToken - Token for the provided `scheme`
public type AuthenticationContext record {
    string scheme?;
    string authToken?;
};

# Represents the `runtime:Principal` populated with the authenticated user information.
#
# + userId - User ID of the authenticated user
# + username - Username of the authenticated user
# + claims - Claims of the authenticated user
# + scopes - Scopes of the authenticated user
public type Principal record {
    string userId?;
    string username?;
    map<any> claims?;
    string[] scopes?;
};

# Returns the `runtime:InvocationContext` instance.
# ```ballerina
# runtime:InvocationContext invocationContext = runtime:getInvocationContext();
# ```
#
# + return - The `runtime:InvocationContext` instance
public function getInvocationContext() returns InvocationContext = @java:Method {
    class: "org.ballerinalang.stdlib.runtime.nativeimpl.GetInvocationContext"
} external;
