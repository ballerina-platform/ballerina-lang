import ballerina/filepath;
import ballerina/io;

public function main() {
    // Get the absolute representation of the path.
    string absValue = checkpanic filepath:absolute("test.txt");

    // Check whether the path is absolute.
    boolean isAbs = checkpanic filepath:isAbsolute("/A/B/C");
    io:println("/A/B/C is absolute: ", isAbs);

    // Get the base name of the path.
    string name = checkpanic filepath:filename("/A/B/C");
    io:println("Filename of /A/B/C: ", name); // returns C

    // Get the enclosing parent directory.
    string parentPath = checkpanic filepath:parent("/A/B/C");
    io:println("Parent of /A/B/C: ", parentPath); // returns B

    // Get the shortest path name equivalent to path by purely lexical processing.
    string normalizedPath = checkpanic filepath:normalize("foo/../bar");
    io:println("Normalized path of foo/../bar: ", normalizedPath); // returns bar

    // Get the list of path elements joined by the OS-specific Path Separator.
    string[] parts = checkpanic filepath:split("/A/B/C");
    io:println(io:sprintf("Path elements of /A/B/C: %s", parts)); // returns {"A", "B", "C"}

    // Join any number of path elements into a single path.
    string path = checkpanic filepath:build("/", "foo", "bar");
    io:println("Built path of '/', 'foo', 'bar': ", path); // On Unix : returns /foo/bar

    // Get the extension of the file path.
    string ext = checkpanic filepath:extension("path.bal");
    io:println("Extension of path.bal: ", ext); // returns bal

    // Returns a relative path that is logically equivalent to the target path when joined to the base path.
    string relPath = checkpanic filepath:relative("a/b/c", "a/c/d");
    io:println("Relative path between 'a/b/c' and 'a/c/d': ", relPath); // On Unix : returns ../../c/d

}
