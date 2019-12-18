import ballerina/io;
import ballerina/lang.'int as ints;

// The `parse()` function takes a `string` argument and attempts to convert it to an `int`.
function parse(string num) returns int|error {
    return ints:fromString(num);
}

function scale(string num) returns int|error {
    // The `check` unary operator can be used to return early on error.
    // Here, `check` is used to return the error if the `parse()`
    // function evaluates to `error`. If the actual value returned
    // by the `parse()` function is an error, this function immediately returns
    // the error, else the `int` value returned by `parse()` is set to `x` and
    // execution continues. If `check` is used within a function, the return type
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
