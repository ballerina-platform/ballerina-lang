import ballerina/io;
import ballerina/log;
import userPortal.users;

configurable string url = ?;
configurable int[] ports = [9090];

public function main() {
    io:println("Page URL : ", url);
    io:println("Available ports : ", ports);
    io:println("Admin : ", users:getAdminUser());

    table<users:UserInfo> key(username) userTable = users:getUsers();
    io:println("Other users : ", userTable);

    if (userTable.length() > 2) {
        log:printError("Maximum number of users is 2");
    }
}
