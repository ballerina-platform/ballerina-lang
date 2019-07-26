import ballerina/io;

// This function accepts three `int` values, performs a calculation,
// and returns the result as an integer.
// All three parameters are required parameters.
function calculate(int a, int b, int c) returns int {
    return a + 2 * b + 3 * c;
}

public function main() {
    // Call the `calculate()` function to retrieve the integer result.
    // Arguments for required parameters can be passed as positional arguments.
    // Positional arguments need to be passed in the expected order.
    int result = calculate(5, 6, 7);
    // Print the result.
    io:println(result);

    // Arguments for required parameters can also be passed as named arguments.
    // All arguments after the first named argument need to be passed by name.
    // Named arguments do not have to be specified in order.
    result = calculate(5, c = 7, b = 6);
    // Print the result.
    io:println(result);
}
