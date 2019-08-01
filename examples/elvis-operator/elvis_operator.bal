import ballerina/io;

public function main() {
    string? x = ();

    // If you need to get the value of `x` if it is a `string`, or assign some
    // other `string` value if `x` is `()`, you can do it using the type-guard
    // as follows.
    string output = x is string ? "value is string: " + x : "value is nil";
    io:println("The output from the type-guard: " + output);

    // You can achieve the same using the elvis operator as follows.
    string elvisOutput = x ?: "value is nil";
    io:println("The output from the elvis operator: " + elvisOutput);
}
