import ballerina/io;

public function main() {
    string|() x = ();

    // If you need to get the `string` value of x, and if the value of x is `nil`, you may want
    // to assign a value. This is how you can do it via the type-guard.
    string output = x is string ? "value is string: " + x : "value is nil";
    io:println("The output from the type-guard: " + output);

    // This shows how to achieve the same via the `elvis` operator.
    string elvisOutput = x ?: "value is nil";
    io:println("The output from the elvis operator: " + elvisOutput);
}
