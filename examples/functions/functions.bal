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

function printAndReturnValue(string s) returns string {
    string t = "Hello ".concat(s);
    io:println(t);
    return t;
}

// A public function named `main` is considered a default entry point of a
// Ballerina program.
public function main() {
    // Call the `printValue()` function that prints the value provided.
    printValue("This is a sample text");

    // Call the `add()` function to retrieve the result of adding two given values.
    int result = add(5, 6);
    // Print the result.
    io:println(result);

    // Ballerina does not allow ignoring the return value of a function
    // invocation implicitly, unless the return type is `()`. A return value can be ignored
    // using `_` if the value may never be an `error` (i.e., the return type of the
    // function does not contain `error`).
    _ = printAndReturnValue("World");
}
