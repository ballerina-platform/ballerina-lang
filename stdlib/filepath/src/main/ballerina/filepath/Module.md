## Module overview
File path module implements utility functions for manipulating file path in a way compatible with the target operating 
system. 
This module uses either forward slashes or backslashes, depending on the operating system.

## Sample
The sample given below uses a few functions that are in the ballerina/filepath module.

```ballerina
import ballerina/filepath;
import ballerina/io;

public function main() {
    // Get Absolute representation of the path.
    string absValue = filepath:absolute(untaint "test.txt");

    // Check whether the path is absolute.
    boolean isAbs = filepath:isAbsolute("/A/B/C");
    io:println(isAbs); // On Unix : returns true
    
    // Get base name of the path.
    string name = filepath:filename("/A/B/C");
    io:println(name); // returns C

    // Get the enclosing parent directory.
    string parentPath = filepath:parent("/A/B/C");
    io:println(parentPath); // returns B
    
    // Get the shortest path name equivalent to path by purely lexical processing.
    string normalizedPath = filepath:normalize("foo/../bar");
    io:println(normalizedPath); // returns bar
    
    // Get list of path elements joined by the OS-specific Path Separator.
    string[] parts = filepath:split("/A/B/C");
    io:println(parts); // returns {"A", "B", "C"} 
    
    // Join any number of path elements into a single path
    string path = filepath:build("/", "foo", "bar");
    io:println(path); // On Unix : returns /foo/bar
    
    // Get the extension of the file path.
    string ext = filepath:extension("path.bal");
    io:println(ext); // returns bal
    
    // Returns a relative path that is logically equivalent to target path when joined to base path.
    string relPath = filepath:relative("a/b/c", "a/c/d");
    io:println(relPath); // On Unix : returns ../../c/d
```