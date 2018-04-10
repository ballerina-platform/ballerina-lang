import ballerina/io;

function main (string[] args) {
    // Define the lambda function in-line to the function pointer var.
    function (string, string) returns (string) lambda =
            (string x, string y) => (string) {
                string r = x + y;
                return r;
            };
    io:println("Output: " + lambda("Hello ", "world.!!!"));
}
