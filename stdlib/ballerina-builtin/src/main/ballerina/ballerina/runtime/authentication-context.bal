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

package ballerina.runtime;

@Description {value:"Represents the AuthenticationContext, populated with authenticated user information"}
public struct AuthenticationContext {
}

@Description {value:"Creates a AuthenticationContext instance"}
@Return {value:"AuthenticationContext instance"}
public function getAuthenticationContext () (AuthenticationContext) {
    AuthenticationContext authContext = {};
    return authContext;
}

@Description {value:"Set userId of the authenticated user in authentication context"}
@Param {value:"userId: UserId of the authenticated user"}
public native function <AuthenticationContext authenticationContext> setUserId (string userId);

@Description {value:"Get userId of the authenticated user from authentication context"}
@Return {value:"string: UserId of the authenticated user"}
public native function <AuthenticationContext authenticationContext> getUserId () (string);

@Description {value:"Set username of the authenticated user in authentication context"}
@Param {value:"username: Username of the authenticated user"}
public native function <AuthenticationContext authenticationContext> setUsername (string username);

@Description {value:"Get username of the authenticated user from authentication context"}
@Return {value:"string: Username of the authenticated user"}
public native function <AuthenticationContext authenticationContext> getUsername () (string);

@Description {value:"Set groups of the authenticated user in authentication context"}
@Param {value:"userGroups: Groups of the authenticated user"}
public native function <AuthenticationContext authenticationContext> setUserGroups (string[] userGroups);

@Description {value:"Get groups of the authenticated user from authentication context"}
@Return {value:"string[]: Groups of the authenticated user"}
public native function <AuthenticationContext authenticationContext> getUserGroups () (string[]);

@Description {value:"Set claims of the authenticated user in authentication context"}
@Param {value:"userClaims: Claims of the authenticated user"}
public native function <AuthenticationContext authenticationContext> setUserClaims (map userClaims);

@Description {value:"Get claims of the authenticated user from authentication context"}
@Return {value:"map: Claims of the authenticated user"}
public native function <AuthenticationContext authenticationContext> getUserClaims () (map);

@Description {value:"Set allowed scopes for the authenticated user as an array in authentication context"}
@Param {value:"allowedScopes: Allowed scopes for the authenticated user"}
public native function <AuthenticationContext authenticationContext> setAllowedScopes (string[] allowedScopes);

@Description {value:"Get allowed scopes for the authenticated user from authentication context"}
@Return {value:"string[]: Allowed scopes for the authenticated user"}
public native function <AuthenticationContext authenticationContext> getAllowedScopes () (string[]);

@Description {value:"Set authentication mechanism used to authenticate the user in authentication context"}
@Param {value:"authType: Authentication mechanism"}
public native function <AuthenticationContext authenticationContext> setAuthType (string authType);

@Description {value:"Get authentication mechanism used to authenticate the user in authentication context"}
@Return {value:"string: Authentication mechanism"}
public native function <AuthenticationContext authenticationContext> getAuthType () (string);

@Description {value:"Set security token used to authenticate the user in authentication context"}
@Param {value:"authToken: Security token used to authenticate"}
public native function <AuthenticationContext authenticationContext> setAuthToken (string authToken);

@Description {value:"Get security token used to authenticate the user in authentication context"}
@Return {value:"string: Security token used to authenticate"}
public native function <AuthenticationContext authenticationContext> getAuthToken () (string);
