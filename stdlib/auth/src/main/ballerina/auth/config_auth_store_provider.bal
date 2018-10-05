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
import ballerina/runtime;

@final string CONFIG_USER_SECTION = "b7a.users";

# Represents Ballerina configuration file based auth store provider
public type ConfigAuthStoreProvider object {

    # Attempts to authenticate with username and password
    #
    # + user - user name
    # + password - password
    # + return - true if authentication is a success, else false
    public function authenticate(string user, string password) returns boolean {
        boolean isAuthenticated = password == readPassword(user);
            if(isAuthenticated){
                runtime:UserPrincipal userPrincipal = runtime:getInvocationContext().userPrincipal;
                userPrincipal.userId = user;
                // By default set userId as username.
                userPrincipal.username = user;
            }
            return isAuthenticated;
        }

    # Reads the scope(s) for the user with the given username
    #
    # + username - username
    # + return - array of groups for the user denoted by the username
    public function getScopes(string username) returns string[] {
        // first read the user id from user->id mapping
        // reads the groups for the userid
        return getArray(getConfigAuthValue(CONFIG_USER_SECTION + "." + username, "scopes"));
    }

    # Reads the password hash for a user
    #
    # + username - username
    # + return - password hash read from userstore, or nil if not found
    public function readPassword(string username) returns string {
        // first read the user id from user->id mapping
        // read the hashed password from the userstore file, using the user id
        return getConfigAuthValue(CONFIG_USER_SECTION + "." + username, "password");
    }

    public function getConfigAuthValue(string instanceId, string property) returns string {
        return config:getAsString(instanceId + "." + property, default = "");
    }

    # Construct an array of groups from the comma separed group string passed
    #
    # + groupString - comma separated string of groups
    # + return - array of groups, nil if the groups string is empty/nil
    public function getArray(string groupString) returns (string[]) {
        string[] groupsArr = [];
        if (lengthof groupString == 0) {
            return groupsArr;
        }
        return groupString.split(",");
    }
};
