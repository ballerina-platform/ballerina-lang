import ballerina/io;
import ballerina/lang.'int as ints;

// The `test` function acts as a variable function pointer in the `main` function.
function test(string s, int... x) returns float {
    int | error y = ints:fromString(s);
    float f = 0.0;

    if (y is int) {
        foreach var item in x {
            f += item * 1.0 * y;
        }
    } else {
        // The type of `y` within the else block would be `error`.
        panic y;
    }
    return f;
}

// A function pointer as a parameter. A function pointer can be invoked similar to how a normal function is invoked.
function foo(int x, function (string, int...) returns float bar)
             returns float {
    return x * bar("2", 2, 3, 4, 5);
}

// A function pointer as a return type.
function getFunctionPointer() returns
                    (function (string, int...) returns float) {
    return test;
}

public function main() {
    // Value `test` will serve as a function pointer for the `foo` function.
    io:println("Answer: ", foo(10, test));
    io:println("Answer: ", foo(10, getFunctionPointer()));

    // A function pointer as a variable.
    function (string, int...) returns float f = getFunctionPointer();

    io:println("Answer: ", foo(10, f));
}
