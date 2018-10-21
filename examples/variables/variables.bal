import ballerina/io;

// Declare a package-level variable that is private to the package.
int total = 99;

// Declare a public package-level variable.
public int count;

// Declare a public constant.
public const string OK = "ok";

public int status = 1;

public function main() {

    // Access a global variable.
    io:println(total);

    count++;
    io:println(count);

    // This is a local variable.
    boolean available = false;
    io:println(available);
}
