import ballerina/io;

public function main() {
    // Calling the `divide()` function with `0` as the divisor results in a panic. A panic will cause the
    // runtime to exit. If needed, `trap` can be used to capture panics and treat them as errors thereafter.
    int|error result = trap divide(1, 0);
    if (result is int) {
        io:println("int result: ", result);
    } else {
        io:println("Error occurred: ", result.reason());
    }
}

function divide(int a, int b) returns int {
    return a / b;
}
