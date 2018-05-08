## Package overview

This package contains functions to retrieve information about the system and the current users of the system. It provides information such as environment variables, username, user home directory path, and the current working directory.

## Samples

The sample given below uses the functions in the package to get the system-level information.

```ballerina
//Get environment variables.
string balHome = system:getEnv("HTTP_PORT"); // Eg. “80”

// Get the user account name.
string username = user:getName();  //Eg. “john”

// Get the user home path.
string userHome = user:getUserHome();  //Eg. “/home/john”

// Get the current directory path.
string currentDir = user:getCurrentDirectory();  //Eg. “/home/john/work”
```
