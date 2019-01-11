import ballerina/io;

public function main() {
    string|() x = ();

    // If you need to get the `string` value of x, and if the value of x is `nil`, you may want
    // to assign a known value. This is how you can do it via type-guard (also known as `is` check)
    // expression.
    string output = x is string ? "value is string: " + x : "value is nil";

    io:println("The output from is check expression: " + output);

    // This shows how to achieve the same via the `elvis` operator.
    string elvisOutput = x ?: "value is nil";
    io:println("The output from elvis operator: " + elvisOutput);
}
