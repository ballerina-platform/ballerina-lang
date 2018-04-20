import ballerina/config;
import ballerina/io;

function main(string... args) {
    // Using the Ballerina config API, you can look up values from config files, CLI parameters,
    // environment variables, etc. The precedence order for config lookup is as follows: <br>
    // * CLI parameters <br>
    // * Environment variables <br>
    // * Config files <br><br>
    // If a particular config defined in the file is also defined as an environment variable, the environment 
    // variable takes precedence. Similarly, if the same is set as a CLI parameter, it replaces the environment 
    // variable value. <br>
    // The configs are simply arbitrary key/value pairs with slight structure to it.
    string[] users;
    string usersString  = config:getAsString("username.instances");
    if (usersString != ""){
        users = usersString.split(",");
    } else {
        return;
    }

    string user1Rights = config:getAsString(users[0] + ".access.rights");
    if (user1Rights == ""){
        return;
    }

    var user2Rights = config:getAsString(users[1] + ".access.rights");
    if (user2Rights == ""){
        return;
    }

    io:println(users[0] + " has " + user1Rights + " access");
    io:println(users[1] + " has " + user2Rights + " access");
    // A sample config file looks as follows: <br>
    // sum.limit=5 <br>
    // username.instances="john,peter" <br>
    // [john] <br>
    // access.rights="RW" <br>
    // [peter] <br>
    // access.rights="R" <br><br>
    // The same configs can be set using CLI parameters as follows: <br>
    // -e sum.limit=5 -e username.instances=john,peter -e john.access.rights=RW -e peter.access.rights=R<br>

    io:println("Before changing sum.limit in code: " + getLimit());

    // You can set configs using the code as well.
    config:setConfig("sum.limit", "10");

    io:println("After changing sum.limit: " + getLimit());
}

function getLimit() returns (string) {
    string sumLimit = config:getAsString("sum.limit");
    if (sumLimit != ""){
        return sumLimit;
    }

    io:println("Returning default limit: 1000");
    return "1000";
}
