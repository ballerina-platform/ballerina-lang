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

import ballerina/config;
import ballerina/crypto;
import ballerina/internal;
import ballerina/log;
import ballerina/runtime;
import ballerina/system;
import ballerina/time;

# Represents LDAP authentication provider that supports generating JWT for client interactions
#
# + ldapJwtAuthProviderConfig - JWT configurations
# + ldapAuthProvider - LDAP auth store provider
public type LdapJwtAuthProvider object {

    public InferredJwtAuthProviderConfig ldapJwtAuthProviderConfig;
    public LdapAuthStoreProvider ldapAuthProvider;

    # Provides authentication based on the configured LDAP user store
    #
    # + ldapJwtAuthProviderConfig - Configuration for JWT token propagation
    # + ldapAuthProvider - LDAP auth store provider
    public new(ldapJwtAuthProviderConfig, ldapAuthProvider) {
    }

    # Authenticate with username and password using LDAP user store
    #
    # + username - user name
    # + password - password
    # + return - true if authentication is a success, else false
    public function authenticate(string username, string password) returns boolean {
        boolean isAuthenticated = self.ldapAuthProvider.authenticate(username, password);
        if (isAuthenticated){
            setAuthToken(username, self.ldapJwtAuthProviderConfig);
        }
        return isAuthenticated;
    }

    # Reads the scope(s) for the user with the given username from LDAP user store
    #
    # + username - user name
    # + return - array of groups for the user denoted by the username
    public function getScopes(string username) returns string[] {
        return self.ldapAuthProvider.getScopes(username);
    }
};
