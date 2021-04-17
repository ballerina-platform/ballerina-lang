import ballerina/filepath;
import ballerina/io;

public function main() returns error? {
    // Get the absolute representation of the path.
    string absValue = check filepath:absolute("test.txt");

    // Check whether the path is absolute.
    boolean isAbs = check filepath:isAbsolute("/A/B/C");
    io:println("/A/B/C is absolute: ", isAbs);

    // Get the base name of the path.
    string name = check filepath:filename("/A/B/C");
    io:println("Filename of /A/B/C: ", name);

    // Get the enclosing parent directory.
    string parentPath = check filepath:parent("/A/B/C");
    io:println("Parent of /A/B/C: ", parentPath);

    // Get the shortest path name equivalent to path by purely lexical processing.
    string normalizedPath = check filepath:normalize("foo/../bar");
    io:println("Normalized path of foo/../bar: ", normalizedPath);

    // Get the list of path elements joined by the OS-specific path separator.
    string[] parts = check filepath:split("/A/B/C");
    io:println(io:sprintf("Path elements of /A/B/C: %s", parts));

    // Join any number of path elements into a single path.
    string path = check filepath:build("/", "foo", "bar");
    io:println("Built path of '/', 'foo', 'bar': ", path);

    // Get the extension of the file path.
    string ext = check filepath:extension("path.bal");
    io:println("Extension of path.bal: ", ext);

    // Returns a relative path that is logically equivalent to the target path when joined to the base path.
    string relPath = check filepath:relative("a/b/c", "a/c/d");
    io:println("Relative path between 'a/b/c' and 'a/c/d': ", relPath);
}
