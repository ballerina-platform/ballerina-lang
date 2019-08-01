## Module overview

This module contains functions to retrieve information about the system, the current users of the system and
to perform file system based operations.
* It provides information such as environment variables, username, user home directory path, and the current working
directory.
* It provides functions to perform file system based operations such as creating, deleting, renaming the
file/directory and retrieving metadata of the file.

## Samples

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

    // Get the current directory path.
    io:println("Current directory: " + system:getCurrentDirectory());  // E.g. “/home/john/work”
    
    // Check whether file or directory of the provided path exists.
    boolean result = system:exists(“foo/bar.txt”);
    
    // Create a new directory.
    string|error results = system:createDir(“foo/bar”);
    
    // Create a directory with any non existence parents.
    string|error results = system:createDir(“foo/bar”, parentDirs = true);
    
    // Remove file or directory in specified file path.
    error? results = system:remove(“foo/bar.txt”);

    // Remove directory in specified file path with all children.
    error? results = system:remove(“foo/bar”, recursive = true);
    
    // Rename(Move) file or directory to new path.
    error? results = system:rename(“/A/B/C”, “/A/B/D”);
    
    // Get default directory use for temporary files.
    string results = system:tempDir();
    
    // Create a file in given file path.
    string|error results = system:createFile(“bar.txt”);
    
    // Get metadata information of the file.
    FileInfo|error result = system:getFileInfo(“foo/bar.txt”);
    
    // Get list of files in the directory.
    FileInfo[]|error results = system:readDir(“foo/bar”);
    
    // Copy file ot directory to new path.
    error? results = system:copy(“/A/B/C”, “/A/B/D”, replaceExisting = true);
}
```
