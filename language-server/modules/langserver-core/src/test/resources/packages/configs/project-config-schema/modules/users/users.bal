public type UserInfo record {|
    readonly string username;
    int age;
|};

public type UserTable table<UserInfo> key(username);

configurable UserInfo admin = ?;
configurable UserTable users = ?;

public function getAdminUser() returns UserInfo {
    return admin;
}

public function getUsers() returns UserTable {
    return users;
}
