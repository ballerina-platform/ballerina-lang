import ballerina/io;

function main(string... args) {
    // Define the lambda function.
    function (string, string) returns (string) lambda =
            function (string x, string y) returns (string) {
                string r = x + y;
                return r;
            };
    io:println("Output: " + lambda("Hello ", "world.!!!"));
}
