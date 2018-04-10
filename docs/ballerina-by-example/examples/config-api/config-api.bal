import ballerina/config;
import ballerina/io;

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
    string[] users;
    var usersConfig  = config:getAsString("username.instances");
    match usersConfig {
        string usersString => {
            users = usersString.split(",");
        }
        int | () => { return; }
    }

    string user1Rights;
    var user1RightsConfig = config:getAsString(users[0] + ".access.rights");
    match user1RightsConfig {
        string user1RightsString => {
            user1Rights = user1RightsString;
        }
        int | () => { return; }
    }

    string user2Rights;
    var user2RightsConfig = config:getAsString(users[1] + ".access.rights");
    match user2RightsConfig {
        string user2RightsString => {
            user2Rights = user2RightsString;
        }
        int | () => { return; }
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

    // Configurations can be set in code as well.
    config:setConfig("sum.limit", "10");

    io:println("After changing sum.limit: " + getLimit());
}

function getLimit() returns (string) {
    var limitConfig = config:getAsString("sum.limit");
    match limitConfig {
        string limit => {
            return limit;
        }
        float | () => {
            io:println("Returning default limit: 1000");
            return "1000";
        }
    }
}
