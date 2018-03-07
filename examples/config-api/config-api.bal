import ballerina.config;
import ballerina.io;

function main(string[] args) {
    // Ballerina config API provides a mechanism for developers to look-up values from config files, CLI parameters,
    // environment variables, etc. The precedence order for config lookup is as follows: <br>
    // * CLI parameters <br>
    // * Environment variables <br>
    // * Config files <br><br>
    // If a particular config defined in the file is also defined as an environment variable, the environment 
    // variable takes precedence. Similarly, if the same is set as a CLI parameter, it replaces the environment 
    // variable value. <br>
    // The configs are simply arbitrary key/value pairs with slight structure to it.
    string[] users = config:getAsString("username.instances").split(",");
    string user1Rights = config:getAsString(users[0] + ".access.rights");
    string user2Rights = config:getAsString(users[1] + ".access.rights");

    io:println(users[0] + " has " + user1Rights + " access");
    io:println(users[1] + " has " + user2Rights + " access");
    // A sample config file looks as follows: <br>
    // sum.limit=5 <br>
    // username.instances=john,peter <br>
    // [john] <br>
    // access.rights=RW <br>
    // [peter] <br>
    // access.rights=R <br><br>
    // The same configs can be set using CLI parameters as follows: <br>
    // -Busername.instances=john,peter -Bjohn.access.rights=RW -Bpeter.access.rights=R -Bsum.limit=5<br>

    io:println("Before changing sum.limit in code: " + sum());

    // Configurations can be set in code as well.
    config:setConfig("sum.limit", "10");

    io:println("After changing sum.limit: " + sum());
}

function sum () (int) {
    var to, err = <int>config:getAsString("sum.limit");

    if (err != null) {
        return -1; // Returning -1 to signal failure
    }

    int sumUpTo = 0;
    int i = 1;

    while (i <= to) {
        sumUpTo = sumUpTo + i;
        i = i + 1;
    }

    return sumUpTo;
}
