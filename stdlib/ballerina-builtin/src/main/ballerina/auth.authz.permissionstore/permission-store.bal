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

@Description {value:"Represents the permission store. Any implementation of the Permission store should be
struct-wise similar"}
public struct PermissionStore {
}

@Description {value:"Checks if the the user has sufficient permission to access a resource with the specified scope"}
@Param {value:"username: user name"}
@Param {value:"scopeName: name of the scope"}
@Return {value:"boolean: true if authorized, else false"}
public function <PermissionStore permissionStore> isAuthorized (string username, string scopeName) returns (boolean) {
    error e = {message:"Not implemented"};
    throw e;
    return false;
}

@Description {value:"Reads groups for the given scopes"}
@Param {value:"scopeName: name of the scope"}
@Return {value:"string: comma separated groups specified for the scopename"}
public function <PermissionStore permissionStore> readGroupsOfScope (string scopeName) returns (string) {
    error e = {message:"Not implemented"};
    throw e;
    return "";
}

@Description {value:"Reads the groups for a user"}
@Param {value:"string: username"}
@Return {value:"string: comma separeted groups list, as specified in the userstore file"}
public function <PermissionStore permissionStore> readGroupsOfUser (string username) returns (string) {
    error e = {message:"Not implemented"};
    throw e;
    return "";
}

