import ballerina/io;

public function main() {
    // Defines an anonymous function.
    function (string, string) returns string anonFunction =
            function (string x, string y) returns string {
                return x + y;
            };
    io:println("Output: ", anonFunction("Hello ", "World.!!!"));

    // Defines an anonymous function with `var`.
    var anonFunction2 = function (string x, string y) returns string {
                            return x + y;
                        };
    io:println("Output: ", anonFunction2("Hello ", "World.!!!"));

    // Defines an anonymous function with rest parameter.
    var anonFunction3 = function (string... a) returns int {
                            return  a.length();
                        };
    io:println("Output: ", anonFunction3("Hello ", "Ballerina!!"));

    // If an anonymous function contains only the return statement in the body,
    // you can use the `arrow function expression` instead of the anonymous function.
    // The types of the input parameters are inferred from the left-hand side.
    // The return of the arrow function expression is determined by the
    // evaluation of the expression on the right-hand side of the `=>` symbol.
    function (string, string) returns string arrowExpr = (x, y) => x + y;
    io:println("Output: ", arrowExpr("Hello ", "World.!!!"));
}
