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

package ballerina.auth.authz.permissionstore;

import ballerina/config;
import ballerina/log;

@Description {value:"Configuration key for groups in userstore"}
@final string PERMISSIONSTORE_GROUPS_ENTRY = "groups";

@final string EMPTY_STRING = "";

@Description {value:"Represents the permission store"}
public type FileBasedPermissionStore object {
    new () {}
    public function isAuthorized (string username, string[] scopes) returns (boolean);
    public function isAuthorizedByGroups (string[] groups, string[] scopes) returns (boolean);
    public function readGroupsOfScope (string scopeName) returns (string[]);
    public function readGroupsOfUser (string username) returns (string[]);
};

@Description {value:"Checks if the the user has sufficient permission to access a resource with the specified scope"}
@Param {value:"username: user name"}
@Param {value:"scopes: array of scope names"}
@Return {value:"boolean: true if authorized, else false"}
public function FileBasedPermissionStore::isAuthorized (string username, string[] scopes) returns (boolean) {
    string[] groupsForUser = self.readGroupsOfUser(username);
    if (lengthof groupsForUser == 0) {
        // no groups for user
        return false;
    }
    return self.isAuthorizedByGroups(groupsForUser, scopes);
}

@Description {value:"Checks whether the groups provided can access a resource with the specified scopes"}
@Param {value:"groups: array of group names"}
@Param {value:"scopes: array of scope names"}
@Return {value:"boolean: true if authorized, else false"}
public function FileBasedPermissionStore::isAuthorizedByGroups (string[] groups, string[] scopes) returns (boolean) {
    if (lengthof groups == 0) {
        // no groups for user
        return false;
    }

    string[] groupsForScope = [];
    foreach scopeName in scopes  {
        // read groups for each scope and see if there is a match between user's groups
        // and the groups of the scope. There is no need to read groups for all scopes
        // and do the comparison.
        groupsForScope = self.readGroupsOfScope(scopeName);
        if (lengthof groupsForScope > 0) {
            // check if there is a match
            if (matchGroups(groupsForScope, groups)) {
                return true;
            }

        }
    }
    return false;
}

@Description {value:"Reads groups for the given scopes"}
@Param {value:"scopeName: name of the scope"}
@Return {value:"string[]: array of groups corresponding to the given scope name"}
public function FileBasedPermissionStore::readGroupsOfScope (string scopeName) returns (string[]) {
    return getGroupsArray(getPermissionStoreConfigValue(scopeName, PERMISSIONSTORE_GROUPS_ENTRY));
}

@Description {value:"Matches the groups passed"}
@Param {value:"requiredGroupsForScope: array of groups for the scope"}
@Param {value:"groupsOfUser: array of groups belonging to the user"}
@Return {value:"boolean: true if two arrays are equal in content, else false"}
function matchGroups (string[] groupsOfScope, string[] groupsOfUser) returns (boolean) {
    foreach groupOfUser in groupsOfUser {
        foreach groupOfScope in groupsOfScope {
            if (groupOfUser == groupOfScope) {
                // if user is in one group that is equal to a group of a scope, authorization passes
                return true;
            }
        }
    }
    return false;
}

@Description {value:"Construct an array of groups from the comma separed group string passed"}
@Param {value:"groupString: comma separated string of groups"}
@Return {value:"string[]: array of groups, nil if the groups string is empty/nil"}
function getGroupsArray (string groupString) returns (string[]) {
    string[] groupsArr = [];
    if (lengthof groupString == 0) {
        return groupsArr;
    }
    return groupString.split(",");
}

@Description {value:"Reads the groups for a user"}
@Param {value:"string: username"}
@Return {value:"string[]: array of groups, for the user denoted by the username"}
public function FileBasedPermissionStore::readGroupsOfUser (string username) returns (string[]) {
    string userId = getPermissionStoreConfigValue(username, "userid");
    string[] groupsArr = [];
    if (userId == EMPTY_STRING) {
        return groupsArr;
    }
    return getGroupsArray(getPermissionStoreConfigValue(userId, PERMISSIONSTORE_GROUPS_ENTRY));
}

function getPermissionStoreConfigValue (string instanceId, string property) returns (string) {
    match config:getAsString(instanceId + "." + property) {
        string value => {
            return value == () ? "" : value;
        }
        () => return "";
    }
}
