import ballerina/io;

// This function takes a `string` argument. However, it does not return a value.
function printValue(string value) {
    io:println(value);
}

// This function takes in two `int` values as arguments and returns their
// sum as an integer.
function add(int a, int b) returns int {
    return a + b;
}

// A public function named `main` is considered a default entry point of a
// Ballerina program.
public function main() {
    // Calls a function that prints the value provided via the console.
    printValue("This is a sample text");

    // Directly, prints the value that was returned from the function in the
    // console.
    int result = add(5, 6);
    io:println(result);
}
