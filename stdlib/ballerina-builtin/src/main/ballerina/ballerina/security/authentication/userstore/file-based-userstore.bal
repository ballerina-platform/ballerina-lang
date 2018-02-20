package ballerina.security.authentication.userstore;

import ballerina.config;

@Description {value:"Configuration key for userids in userstore"}
const string USERSTORE_USERIDS_ENTRY = "userids";

@Description {value:"Represents the file-based user store"}
public struct FilebasedUserstore {
    // TODO
}

@Description {value:"Reads the password hash for a user"}
@Param {value:"string: username"}
@Return {value:"string: password hash read from userstore, or null if not found"}
public function <FilebasedUserstore userstore> readPasswordHash (string username) (string) {
    // first read the user id from user->id mapping
    string userid = readUserId(username);
    if (userid == null) {
        return null;
    }
    // read the hashed password from the userstore file, using the user id
    return config:getInstanceValue(userid, "password");
}

@Description {value:"Reads the user id for the given username"}
@Param {value:"string: username"}
@Return {value:"string: user id read from the userstore, or null if not found"}
function readUserId (string username) (string) {
    return config:getInstanceValue(USERSTORE_USERIDS_ENTRY, username);
}
