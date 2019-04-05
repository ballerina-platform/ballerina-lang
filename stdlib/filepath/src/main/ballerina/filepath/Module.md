## Module overview
The file path module implements the utility functions for manipulating the file path in a way that is compatible with 
the target operating system. 
This module uses either forward slashes or backslashes, depending on the operating system.

## Sample
The sample given below uses a few functions that are in the `ballerina/filepath` module.

```ballerina
import ballerina/filepath;
import ballerina/io;

public function main() {
    // Get the absolute representation of the path.
    string absValue = checkpanic filepath:absolute(untaint "test.txt");

    // Check whether the path is absolute.
    boolean isAbs = checkpanic filepath:isAbsolute("/A/B/C");
    io:println(isAbs); // On Unix : returns true
    
    // Get the base name of the path.
    string name = checkpanic filepath:filename("/A/B/C");
    io:println(name); // returns C

    // Get the enclosing parent directory.
    string parentPath = checkpanic filepath:parent("/A/B/C");
    io:println(parentPath); // returns B
    
    // Get the shortest path name equivalent to path purely via lexical processing.
    string normalizedPath = checkpanic filepath:normalize("foo/../bar");
    io:println(normalizedPath); // returns bar
    
    // Get the list of path elements joined by the OS-specific Path Separator.
    string[] parts = checkpanic filepath:split("/A/B/C");
    io:println(parts); // returns {"A", "B", "C"} 
    
    // Join any number of path elements into a single path.
    string path = checkpanic filepath:build("/", "foo", "bar");
    io:println(path); // On Unix : returns /foo/bar
    
    // Get the extension of the file path.
    string ext = checkpanic filepath:extension("path.bal");
    io:println(ext); // returns bal
    
    // Returns a relative path that is logically equivalent to the target path when joined to the base path.
    string relPath = checkpanic filepath:relative("a/b/c", "a/c/d");
    io:println(relPath); // On Unix : returns ../../c/d
    
}
```
