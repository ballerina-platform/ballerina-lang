## Module overview

This module contains functions to retrieve information about the system and the current users of the system. It 
provides information such as environment variables, username, user home directory path, and the current working directory.

## Samples

The sample given below uses the functions in the module to get the system-level information.

```ballerina
import ballerina/io;
import ballerina/system;

public function main(string... args) {
    //Get environment variables.
    io:println("Envirionment variable: " + system:getEnv("HTTP_PORT")); // Eg. “80”

    // Get the user account name.
    io:println("Username: " + system:getUsername());  //Eg. “john”

    // Get the user home path.
    io:println("User home: " + system:getUserHome());  //Eg. “/home/john”

    // Get the current directory path.
    io:println("Current directory: " + system:getCurrentDirectory());  //Eg. “/home/john/work”
}
```
