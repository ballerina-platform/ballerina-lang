import ballerina/io;

int moduleA = 5;

// A basic example in which an anonymous function with an `if` block accesses its outer scope
// variables.
function basicClosure() returns (function (int) returns int) {
    int a = 3;
    var foo = function (int b) returns int {
        int c = 34;
        if (b == 3) {
            c = c + b + a + moduleA;
        }
        return c + a;
    };
    return foo;
}

// An example function with multiple levels of anonymous functions in which the
// innermost anonymous function has access to all of its outer scope variables.
function multilevelClosure() returns (function (int) returns int) {
    int a = 2;
    var func1 = function (int x) returns int {
        int b = 23;
        // The variable `a` defined in the outer scope is modified.
        // The original value of `a` will be changed to `10`.
        a = a + 8;
        var func2 = function (int y) returns int {
            int c = 7;
            var func3 = function (int z) returns int {
                // The variable `b` defined in the `func1` function is modified.
                // The original value of `b` will be changed to `24`.
                b = b + 1;
                return x + y + z + a + b + c;
            };
            return func3(8) + y + x;
        };
        return func2(4) + x;
    };
    return func1;
}

// An Example to represent how function pointers are passed with closures
// so that the inner scope anonymous function can access the outer scope variables.
function functionPointers(int a) returns
                    (function (int) returns (function (int) returns int)) {
    return function (int b) returns (function (int) returns int) {
        return function (int c) returns int {
            return a + b + c;
        };
    };
}


public function main() {
    // Invokes the function that shows basic closure support.
    var foo = basicClosure();
    int result1 = foo(3);
    io:println("Answer: ", result1);

    // The function invocation that represents multiple levels of anonymous functions
    // with closure support.
    var bar = multilevelClosure();
    int result2 = bar(5);
    io:println("Answer: ", result2);

    // This function invocation shows how function pointers with closures
    // are passed around.
    var baz1 = functionPointers(7);
    var baz2 = baz1(5);
    int result3 = baz2(3);
    io:println("Answer: ", result3);
}
