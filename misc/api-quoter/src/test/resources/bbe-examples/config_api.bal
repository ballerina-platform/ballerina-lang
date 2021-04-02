import ballerina/config;
import ballerina/io;

public function main() {
    // Look up the configuration values. 
    string username = config:getAsString("app.auth.username");
    string password = config:getAsString("app.auth.password");

    io:println(string `Authenticating user '${username}'`);
}
