import ballerina/io;

function main (string[] args) {
    // Define lambda function in-line to function pointer var.
    function (string, string) returns (string) lambda =
            (string x, string y) => (string) {
                string r = x + y;
                return r;
            };
    io:println("Output: " + lambda("Hello ", "world.!!!"));
}
