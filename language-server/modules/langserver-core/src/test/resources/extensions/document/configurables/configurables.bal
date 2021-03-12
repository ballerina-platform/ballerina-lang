import ballerina/io;

type UserInfo record {|
    readonly string username;
    string password;
|};

configurable string hostName = ?;
configurable int port = ?;
configurable boolean enableRemote = true;
configurable float maxPayload = 1.0;
configurable string protocol = "http";
configurable UserInfo admin = ?;

public function main() {
    io:println("host: ", hostName);
}
