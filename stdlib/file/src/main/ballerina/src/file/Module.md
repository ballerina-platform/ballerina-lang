## Module overview

This module contains functions to perform file system based operations such as create, delete, rename the
file/directory and retrieve metadata of the file.

## Samples

The sample given below uses the functions in the module to get the system-level information.

```ballerina
import ballerina/file;
import ballerina/io;

public function main() {

    // Get the current directory path.
    io:println("Current directory: " + file:getCurrentDirectory());

    // Check whether file or directory of the provided path exists.
    boolean result = file:exists("foo/bar.txt");

    // Create a new directory.
    string | error results = file:createDir("foo/bar");

    // Create a directory with any non-existent parents.
    string | error results = file:createDir("foo/bar", true);

    // Remove the file or directory in the specified file path.
    error? results = file:remove("foo/bar.txt");

    // Remove the directory in the specified file path with all its children.
    error? results = file:remove("foo/bar", true);

    // Rename(Move) the file or directory to the new path.
    error? results = file:rename("/A/B/C", "/A/B/D");

    // Get default directory use for temporary files.
    string results = file:tempDir();

    // Create a file in given file path.
    string | error results = file:createFile("bar.txt");

    // Get metadata information of the file.
    file:FileInfo | error result = file:getFileInfo("foo/bar.txt");

    // Get the list of files in the directory.
    file:FileInfo[] | error results = file:readDir("foo/bar");

    // Copy the file or directory to the new path.
    error? results = file:copy("/A/B/C", "/A/B/D", true);
}
```
