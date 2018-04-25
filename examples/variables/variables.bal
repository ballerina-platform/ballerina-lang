import ballerina/io;

// Here we declare a package-level variable which is private to the package.
int total = 99;

// Here we declare a public package-level variable.
public int count;

// Here we declare a public constant.
@final
public string OK = "ok";

// Here we declare a public variable which behaves as a constant for other packages;
// the code in the current package can update the value.
@readonly
public int status = 1;

function main(string... args) {

    // Accessing a global variable.
    io:println(total);

    count++;
    io:println(count);

    // This is a local variable.
    boolean available = false;
    io:println(available);
}
