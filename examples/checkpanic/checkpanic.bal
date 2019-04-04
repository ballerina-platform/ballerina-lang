import ballerina/io;

// `parse()` takes a `string` argument and attempts to convert it to an `int`.
function parse(string num) returns int|error {
    return int.convert(num);
}

function scale(string num) returns int {
    // The `checkpanic` unary operator can be used to lift errors.
    // In this instance, `checkpanic` is used to lift the (potential) error
    // returned by the `parse()` function. If the actual value returned
    // by the function is an `error`, the function immediately `panics`
    // with the error.
    int x = checkpanic parse(num);
    return x * 10;
}

public function main() {
    // Passing a valid integer as a `string` will return an `int`.
    int y = scale("12");
    io:println(y);

    // Passing a random `string` will result in a `panic`.
    int z = scale("Invalid");
    io:println(z);
}
