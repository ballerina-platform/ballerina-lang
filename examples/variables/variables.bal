import ballerina/io;

// Declare a package-level variable that is private to the package.
int total = 99;

// Declare a public package-level variable.
public int count;

// Declare a public constant.
@final
public string OK = "ok";

// Declare a public variable that behaves as a constant for other packages.
// The code in the current package can update the value.
@readonly
public int status = 1;

function main(string... args) {

    // Access a global variable.
    io:println(total);

    count++;
    io:println(count);

    // This is a local variable.
    boolean available = false;
    io:println(available);
}
