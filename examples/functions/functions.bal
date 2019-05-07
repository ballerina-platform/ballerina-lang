import ballerina/io;

// This function takes a `string` argument, but does not return a value.
function printValue(string value) {
    io:println(value);
}

// This function takes in two `int` values as arguments and returns their
// sum as an integer.
function add(int a, int b) returns int {
    return a + b;
}

// A public function named `main` is considered a default entry point to a
// Ballerina program.
public function main() {
    // Call a function that prints the given value to the console.
    printValue("This is a sample text");

    // Directly print the value that was returned from the function to the
    // console.
    int result = add(5, 6);
    io:println(result);
}
