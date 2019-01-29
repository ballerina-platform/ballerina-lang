import ballerina/io;

// `parse()` takes a `string` argument and attempts to convert it to an `int`.
function parse(string num) returns int|error {
    return int.convert(num);
}

function scale(string num) returns int|error {
    // The `check` unary operator can be used to lift errors.
    // In this instance, `check` is used to lift the (potential) error
    // returned by the `parse()` function. If the actual value returned
    // by the function is an `error`, the function immediately returns
    // with the error. If `check` is used within a function, the return type
    // of the function must include `error` in its return signature.
    int x = check parse(num);
    return x * 10;
}

public function main() {
    // Passing a valid integer as a `string` will return an `int`.
    int|error w = parse("12");
    io:println(w);

    // Passing a random `string` will return an `error`.
    int|error x = parse("invalid");
    io:println(x);

    int|error y = scale("12");
    io:println(y);

    int|error z = scale("Invalid");
    io:println(z);
}
