import ballerina/io;

function main(string... args) {
    string|() x = null;

    string matchExprOutput;

    // If you need to get the string value of x, and if the value of x is `null`, you may want
    // to assign a known value. This is how you can do it via Match
    // Expression.
    matchExprOutput = x but {
        string s => s,
        () => "value is null"
    };
    io:println("The output from match expression: ", matchExprOutput);

    // This shows how to achieve the same via the Elvis operator.
    string elvisOutput = x ?: "value is null";
    io:println("The output from elvis operator: ", elvisOutput);
}
