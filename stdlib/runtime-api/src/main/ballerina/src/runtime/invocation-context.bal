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

import ballerinax/java;

# Represents the InvocationContext.
#
# + id - Unique id generated when initiating the invocation context.
# + principal - User principal instance.
# + authenticationContext - Authentication context instance.
# + attributes - Context attributes.
public type InvocationContext record {|
    string id;
    Principal principal?;
    AuthenticationContext authenticationContext?;
    map<any> attributes;
|};

# Represents the AuthenticationContext, populated with authenticated information.
#
# + scheme - Authentication token type. e.g: JWT etc.
# + authToken - Relevant token for the schema.
public type AuthenticationContext record {
    string scheme?;
    string authToken?;
};

# Represents the Principal, populated with authenticated user information.
#
# + userId - User Id of the authenticated user.
# + username - Username of the authenticated user.
# + claims - Claims of the authenticated user.
# + scopes - Authenticated user scopes.
public type Principal record {
    string userId?;
    string username?;
    map<any> claims?;
    string[] scopes?;
};

# Creates a InvocationContext instance.
#
# + return - InvocationContext instance
public function getInvocationContext() returns InvocationContext = @java:Method {
    class: "org.ballerinalang.stdlib.runtime.nativeimpl.GetInvocationContext"
} external;
