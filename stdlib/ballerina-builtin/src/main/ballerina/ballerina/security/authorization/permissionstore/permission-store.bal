package ballerina.security.authorization.permissionstore;

import ballerina.config;

@Description {value:"Configuration key for groups in userstore"}
const string PERMISSIONSTORE_GROUPS_ENTRY = "groups";
@Description {value:"Configuration key for userids in userstore"}
const string PERMISSIONSTORE_USERIDS_ENTRY = "userids";

@Description {value:"Represents the permission store"}
public struct PermissionStore {
    // TODO
}

@Description {value:"Reads groups for the given scopes"}
@Param {value:"scopeName: name of the scope"}
@Return {value:"string: comma separated groups specified for the scopename"}
public function <PermissionStore permissionStore> readGroupsOfScope (string scopeName) (string) {
    // reads the groups for the provided scope
    return config:getInstanceValue(scopeName, PERMISSIONSTORE_GROUPS_ENTRY);
}

@Description {value:"Reads the groups for a user"}
@Param {value:"string: username"}
@Return {value:"string: comma separeted groups list, as specified in the userstore file"}
public function <PermissionStore permissionStore> readGroupsOfUser (string username) (string) {
    // first read the user id from user->id mapping
    string userid = readUserId(username);
    if (userid == null) {
        return null;
    }
    // reads the groups for the userid
    return config:getInstanceValue(userid, PERMISSIONSTORE_GROUPS_ENTRY);
}

@Description {value:"Reads the user id for the given username"}
@Param {value:"string: username"}
@Return {value:"string: user id read from the userstore, or null if not found"}
function readUserId (string username) (string) {
    return config:getInstanceValue(PERMISSIONSTORE_USERIDS_ENTRY, username);
}