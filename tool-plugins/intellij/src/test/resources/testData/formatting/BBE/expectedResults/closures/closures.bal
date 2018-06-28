import ballerina/io;

int globalA = 5;

// Basic example where a lambda with an 'if' block accesses its outer scope
// variables.
function basicClosure() returns (function (int) returns int) {
    int a = 3;
    var foo = (int b) => int {
        int c = 34;
        if (b == 3) {
            c = c + b + a + globalA;
        }
        return c + a;
    };
    return foo;
}

// Example function with multiple levels of lambda functions in which the
// innermost lambda has access to all of its outer scope variables.
function multilevelClosure() returns (function (int) returns int) {
    int a = 2;
    var func1 = (int x) => int {
        int b = 23;
        var func2 = (int y) => int {
            int c = 7;
            var func3 = (int z) => int {
                return x + y + z + a + b + c;
            };
            return func3(8) + y + x;
        };
        return func2(4) + x;
    };
    return func1;
}

// Example showing how function pointers are passed around with closures
// and inner scope lambdas access the outer scope variables.
function functionPointers(int a) returns
                                         (function (int) returns (function (int) returns int)) {
    return (int b) => (function (int) returns int) {
        return (int c) => int {
            return a + b + c;
        };
    };
}


function main(string... args) {
    // Invoke the function that shows basic closure support.
    var foo = basicClosure();
    int result1 = foo(3);
    io:println("Answer: " + result1);

    // This function invocation shows multiple levels of lambda functions
    // with closure support.
    var bar = multilevelClosure();
    int result2 = bar(5);
    io:println("Answer: " + result2);

    // This function invocation shows how function pointers with closures
    // are passed around.
    var baz1 = functionPointers(7);
    var baz2 = baz1(5);
    int result3 = baz2(3);
    io:println("Answer: " + result3);

}
