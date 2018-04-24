import ballerina/io;

function main(string... args) {
    string|() x = null;

    string matchExprOutput;

    // Suppose you need to get the string value of x. If x is null, you want to assign some
    // known value. This is how you can do it using match expression.
    matchExprOutput = x but {
        string s => s,
        () => "value is null"
    };
    io:println("The output from match expression: " + matchExprOutput);

    // You can achive the same using elvis operator.
    string elvisOutput = x ?: "value is null";
    io:println("The output from elvis operator: " + elvisOutput);
}
