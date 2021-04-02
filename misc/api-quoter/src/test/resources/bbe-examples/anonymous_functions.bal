import ballerina/io;

public function main() {
    // Defines an anonymous function.
    function (string, string) returns string concat =
            function (string x, string y) returns string {
                return x + y;
            };
    // Defines an anonymous function with `var`.
    var add = function (int v1, int v2) returns int {
                  return v1 + v2;
              };
    io:println("Concat Result: ", concat("Hello ", "World!"));
    io:println("Add Result: ", add(5, 10));
}
