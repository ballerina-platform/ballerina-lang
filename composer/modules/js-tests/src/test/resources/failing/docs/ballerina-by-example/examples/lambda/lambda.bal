import ballerina.io;

function main (string[] args) {
    // Define lambda function in-line to function pointer var.
    function (string, string) returns (string) lambda =
            function (string x, string y) returns (string r) {
                r = x + y;
                return;
            };
    io:println("Output: " + lambda("Hello ", "world.!!!"));
}
