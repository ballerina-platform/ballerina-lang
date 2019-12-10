import ballerina/io;
import ballerina/lang.'int as ints;

// The `test` function acts as a variable function pointer in the `main` function.
function test(int x, string s) returns float {
    int | error y = ints:fromString(s);
    float f = 0.0;

    if (y is int) {
        f = x * 1.0 * y;
    } else {
        // The type of `y` within the else block would be `error`.
        panic y;
    }
    return f;
}

// A function pointer as a parameter. A function pointer can be invoked just as invoking a normal function.
function foo(int x, function (int, string) returns float bar)
             returns float {
    return x * bar(10, "2");
}

// A function pointer as a return type.
function getFunctionPointer() returns (function (int, string) returns float) {
    return test;
}

function functionWithRestValues(int... r) returns int {
    int total = 0;

    foreach var item in r {
        total = total + item;
    }

    return total;
}

public function main() {
    // Value `test` will serve as a function pointer for the `foo` function.
    io:println("Answer: ", foo(10, test));
    io:println("Answer: ", foo(10, getFunctionPointer()));

    // A function pointer as a variable.
    function (int, string) returns float f = getFunctionPointer();

    // A function pointer with rest params as a variable
    function (int...) returns int total= functionWithRestValues;

    io:println("Answer: ", foo(10, f));

    io:println("Total of 1, 2, 3: ", total(1, 2, 3));
}
