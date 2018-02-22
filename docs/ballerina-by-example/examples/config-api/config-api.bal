import ballerina.config;
import ballerina.io;

function main(string[] args) {
    // Ballerina config API provides a mechanism for developers to look-up values from config files, CLI parameters,
    // environment variables etc. The precedence order for config lookup is as follows: <br>
    // * CLI parameters <br>
    // * Environment variables <br>
    // * Config file <br><br>
    // If a particular config defined in the file is also defined as an environment variable, the env variable takes
    // precedence. Similarly, if the same is set as a CLI param, it replaces the env variable value. <br>
    // The configs are simply arbitrary key/value pairs with slight structure to it.
    string[] users = config:getGlobalValue("username.instances").split(",");
    string user1Rights = config:getInstanceValue(users[0], "access.rights");
    string user2Rights = config:getInstanceValue(users[1], "access.rights");

    io:println(users[0] + " has " + user1Rights + " access");
    io:println(users[1] + " has " + user2Rights + " access");
    // A sample config file looks as follows: <br>
    // username.instances=john,peter <br>
    // [john] <br>
    // access.rights=RW <br>
    // [peter] <br>
    // access.rights=R <br><br>
    // The same configs can be set using CLI params as follows: <br>
    // -Busername.instances=john,peter -B[john].access.rights=RW -B[peter].access.rights=R <br>
}
