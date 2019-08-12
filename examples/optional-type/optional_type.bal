import ballerina/io;

// This function (optionally) returns a `string` value. In this example, `string?` is equivalent to `string|()`.
function getValue(string key) returns string? {
    if (key == "string") {
        return "hello world";
    }

    // In Ballerina, the `nil` type that is provided as `()` contains a single value named "nil". This is used
    // to represent the absence of any other value.
    // The "nil" value is written as `()`.
    // `null` is syntactic sugar for "nil" that is to be used with JSON values because JSON uses `null`.
    // "return ();" here is the same as "return;". Not having a return statement at the end is also the same as
    // explicitly returning `()`.
    return ();
}

public function main() {
    // It is optional for `getValue()` to return a value of type `string`. Thus, the value could be either
    // of type `string` or of type `()` and needs to be handled explicitly.
    // The statement `string s = getValue("string");` produces a compilation error.
    string? s = getValue("string");

    // The type test can then be used to check if the value is in fact a `string` and operate on it.
    if (s is string) {
        io:println("Length of the string: ", s.length());
    } else {
        io:println("s is ()");
    }

    // A value of type `string` or `()` can be assigned to `s`.
    s = ();
    if (s is string) {
        io:println("Length of the string: ", s.length());
    } else {
        io:println("s is ()");
    }
}
