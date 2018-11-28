import ballerina/io;

public function main() {
    int|error result;
    io:println("Start dividing numbers");

    // An error is panicked (or thrown) when the `divideNumbers` function is executed.
    // `trap` will catch the error which occurred in the `divideNumbers` function and assgins the `result` variable.
    result = trap divideNumbers(1, 0);
    if (result is int) {
        io:println("int result: " + result);
    } else if (result is error) {
        io:println("Error occurred: ", result.reason());
    }
}

function divideNumbers(int a, int b) returns int|error {
    if (b == 0) {
        error err = error("Division by 0 is not defined");
        return err;
    }
    return a / b;
}
