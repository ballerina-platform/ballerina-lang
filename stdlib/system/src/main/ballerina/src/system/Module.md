## Module overview

This module provides functions to retrieve information about the system and the current users of the system.

## Sample

The sample given below uses the functions in the module to get the system-level information.

```ballerina
import ballerina/io;
import ballerina/system;

public function main() {
    // Get environment variables.
    io:println("Envirionment variable: " + system:getEnv("HTTP_PORT")); // E.g. “80”

    // Get the user account name.
    io:println("Username: " + system:getUsername());  // E.g. “john”

    // Get the user home path.
    io:println("User home: " + system:getUserHome());  // E.g. “/home/john”

    // Execute an OS command as a subprocess.
    system:Process|system:Error proc = checkpanic system:exec("ls", {}, "/", "-la");
}
```
