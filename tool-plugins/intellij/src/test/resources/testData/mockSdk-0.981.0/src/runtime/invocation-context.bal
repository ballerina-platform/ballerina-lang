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

documentation {
    Represents the InvocationContext.
}
public type InvocationContext record {
    string id;
    UserPrincipal userPrincipal;
    AuthContext authContext;
    map attributes;
};

documentation {
    Represents the AuthenticationContext, populated with authenticated information.
}
public type AuthContext record {
    string scheme;
    string authToken;
};

documentation {
    Represents the UserPrincipal, populated with authenticated user information.
}
public type UserPrincipal record {
    string userId;
    string username;
    map claims;
    string[] scopes;
};

documentation {
    Creates a InvocationContext instance.

    R{{}} InvocationContext instance
}
public extern function getInvocationContext() returns (InvocationContext);
