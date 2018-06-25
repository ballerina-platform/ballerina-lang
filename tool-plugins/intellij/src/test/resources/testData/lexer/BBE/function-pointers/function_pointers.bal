import ballerina/io;

// The 'test' function acts as a variable function pointer in the 'main' function
function test(int x, string s) returns (float) {
    int y = check <int> s;
    float f = x * 1.0 * y;
    return f;
}

// Function pointer as a parameter.
function foo(int x, function (int, string) returns (float) bar) 
             returns (float) {
    return x * bar(10, "2");
}

// Function pointer as a return type.
function getIt() returns (function (int, string) returns (float)) {
    return test;
}

function main(string... args) {
    // Value 'test' will serve as a function pointer to the 'foo' function.
    io:println("Answer: " + foo(10, test));
    io:println("Answer: " + foo(10, getIt()));
    // Function pointer as a variable;
    function (int, string) returns (float) f = getIt();
    io:println("Answer: " + foo(10, f));
}
