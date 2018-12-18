import ballerina/io;

public function main() {
    // Define the anonymous function.
    function (string, string) returns (string) anonFunction =
            function (string x, string y) returns (string) {
                return x + y;
            };
    io:println("Output: " + anonFunction.call("Hello ", "World.!!!"));

    // If an anonymous function contains only the return statement in the body,
    // you can use the arrow function expression instead of the anonymous function.
    // You can infer the type of input parameters from the left hand side.
    function (string, string) returns (string) arrowExpression =
                                                (x, y) => x + y;
    io:println("Output: " + arrowExpression.call("Hello ", "World.!!!"));
}
