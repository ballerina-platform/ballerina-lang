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
const string PERMISSIONSTORE_GROUPS_ENTRY = "groups";

const string EMPTY_STRING = "";

@Description {value:"Represents the permission store"}
public struct FileBasedPermissionStore {
}

@Description {value:"Checks if the the user has sufficient permission to access a resource with the specified scope"}
@Param {value:"username: user name"}
@Param {value:"scopeName: name of the scope"}
@Return {value:"boolean: true if authorized, else false"}
public function <FileBasedPermissionStore permissionStore> isAuthorized (string username, string scopeName) returns (boolean) {
    string[] groupsForScope = getGroupsArray(permissionStore.readGroupsOfScope(scopeName));
    if (lengthof groupsForScope == 0) {
        // no groups for scope, no need to authorize
        return true;
        
    }
    string[] groupsForUser = getGroupsArray(permissionStore.readGroupsOfUser(username));
    if (lengthof groupsForUser == 0) {
        // no groups for user
        return false;
    }
    return matchGroups(groupsForScope, groupsForUser);
}

@Description {value:"Reads groups for the given scopes"}
@Param {value:"scopeName: name of the scope"}
@Return {value:"string: comma separated groups specified for the scopename"}
public function <FileBasedPermissionStore permissionStore> readGroupsOfScope (string scopeName) returns (string) {
    return getPermissionStoreConfigValue(scopeName, PERMISSIONSTORE_GROUPS_ENTRY);
}

@Description {value:"Matches the groups passed"}
@Param {value:"requiredGroupsForScope: array of groups for the scope"}
@Param {value:"groupsReadFromPermissionstore: array of groups for the user"}
@Return {value:"boolean: true if two arrays are equal in content, else false"}
function matchGroups (string[] requiredGroupsForScope, string[] groupsReadFromPermissionstore) returns (boolean) {
    int groupCountRequiredForResource = lengthof requiredGroupsForScope;
    int matchingRoleCount = 0;
    foreach groupReadFromPermissiontore in groupsReadFromPermissionstore {
        foreach groupRequiredForResource in requiredGroupsForScope {
            if (groupRequiredForResource == groupReadFromPermissiontore) {
                matchingRoleCount = matchingRoleCount + 1;
            }
        }
    }
    return matchingRoleCount == groupCountRequiredForResource;
}

@Description {value:"Construct an array of groups from the comma separed group string passed"}
@Param {value:"groupString: comma separated string of groups"}
@Return {value:"string[]: array of groups, null if the groups string is empty/null"}
function getGroupsArray (string groupString) returns (string[]) {
    string[] groupsArr = [];
    if (lengthof groupString == 0) {
        return groupsArr;
    }
    return groupString.split(",");
}

@Description {value:"Reads the groups for a user"}
@Param {value:"string: username"}
@Return {value:"string: comma separeted groups list, as specified in the userstore file"}
public function <FileBasedPermissionStore permissionStore> readGroupsOfUser (string username) returns (string) {
    string userId = getPermissionStoreConfigValue(username, "userid");
    if (userId == EMPTY_STRING) {
        return EMPTY_STRING;
    }
    return getPermissionStoreConfigValue(userId, PERMISSIONSTORE_GROUPS_ENTRY);
}

@Description {value:"Reads the user id for the given username"}
@Param {value:"string: username"}
@Return {value:"string: user id read from the userstore, or null if not found"}
function readUserId (string username) returns (string|null) {
    return config:getAsString(username + ".userid");
}

function getPermissionStoreConfigValue (string instanceId, string property) returns (string) {
    match config:getAsString(instanceId + "." + property) {
        string value => {
            return value == null ? "" : value;
        }
        any|null => return "";
    }
}
