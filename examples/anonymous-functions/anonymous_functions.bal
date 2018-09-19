import ballerina/io;

public function main() {
    // Define the anonymous function.
    function (string, string) returns (string) anonFunction =
            function (string x, string y) returns (string) {
                return x + y;
            };
    io:println("Output: " + anonFunction("Hello ", "World.!!!"));

    // If the anonymous function contains only the return statement in the body,
    // the arrow function expression can be used instead of the anonymous function.
    // The types of the input parameters will be inferred from the lhs.
    function (string, string) returns (string) arrowExpression =
                                                (x, y) => x + y;
    io:println("Output: " + arrowExpression("Hello ", "World.!!!"));
}
