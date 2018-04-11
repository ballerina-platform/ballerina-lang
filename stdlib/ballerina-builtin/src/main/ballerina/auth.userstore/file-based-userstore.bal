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

package ballerina.auth.userstore;

import ballerina/config;
import ballerina/security.crypto;

@Description {value:"Represents the file-based user store"}
public type FilebasedUserstore object {
    
    @Description {value:"Attempts to authenticate with username and password"}
    @Param {value:"username: user name"}
    @Param {value:"password: password"}
    @Return {value:"boolean: true if authentication is a success, else false"}
    public function authenticate (string user, string password) returns (boolean) {
        string passwordHash = readPasswordHash(user);
        return passwordHash == crypto:getHash(password, crypto:SHA256);
    }

    @Description {value:"Reads the groups for a user"}
    @Param {value:"string: username"}
    @Return {value:"string[]: array of groups for the user denoted by the username"}
    public function readGroupsOfUser (string username) returns (string[]) {
        // first read the user id from user->id mapping
        // reads the groups for the userid
        return getGroupsArray(getUserstoreConfigValue(readUserId(username), "groups"));
    }
};


@Description {value:"Reads the password hash for a user"}
@Param {value:"string: username"}
@Return {value:"string: password hash read from userstore, or nil if not found"}
function readPasswordHash (string username) returns (string) {
    // first read the user id from user->id mapping
    // read the hashed password from the userstore file, using the user id
    return getUserstoreConfigValue(readUserId(username), "password");
}

@Description {value:"Reads the user id for the given username"}
@Param {value:"string: username"}
@Return {value:"string: user id read from the userstore, or nil if not found"}
public function readUserId (string username) returns (string) {
    return getUserstoreConfigValue(username, "userid");
}

function getUserstoreConfigValue (string instanceId, string property) returns (string) {
    return config:getAsString(instanceId + "." + property, default = "");
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
