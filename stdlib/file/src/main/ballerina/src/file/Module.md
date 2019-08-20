## Module overview

This module contains functions to perform file system based operations.

* It provides functions to perform file system based operations such as creating, deleting, renaming the
file/directory and retrieving metadata of the file.

## Samples

The sample given below uses the functions in the module to get the system-level information.

```ballerina
import ballerina/io;
import ballerina/file;

public function main() {
 
    // Get the current directory path.
    io:println("Current directory: " + file:getCurrentDirectory());  // E.g. “/home/john/work”
    
    // Check whether file or directory of the provided path exists.
    boolean result = file:exists(“foo/bar.txt”);
    
    // Create a new directory.
    string|error results = file:createDir(“foo/bar”);
    
    // Create a directory with any non existence parents.
    string|error results = file:createDir(“foo/bar”, parentDirs = true);
    
    // Remove file or directory in specified file path.
    error? results = file:remove(“foo/bar.txt”);

    // Remove directory in specified file path with all children.
    error? results = file:remove(“foo/bar”, recursive = true);
    
    // Rename(Move) file or directory to new path.
    error? results = file:rename(“/A/B/C”, “/A/B/D”);
    
    // Get default directory use for temporary files.
    string results = file:tempDir();
    
    // Create a file in given file path.
    string|error results = file:createFile(“bar.txt”);
    
    // Get metadata information of the file.
    FileInfo|error result = file:getFileInfo(“foo/bar.txt”);
    
    // Get list of files in the directory.
    FileInfo[]|error results = file:readDir(“foo/bar”);
    
    // Copy file ot directory to new path.
    error? results = file:copy(“/A/B/C”, “/A/B/D”, replaceExisting = true);
}
```
