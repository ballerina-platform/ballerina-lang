import ballerina/io;

public function main() {
    // Define the lambda function.
    function (string, string) returns (string) lambda =
            (string x, string y) => (string) {
                string r = x + y;
                return r;
            };
    io:println("Output: " + lambda("Hello ", "world.!!!"));
}
