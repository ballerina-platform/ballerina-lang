import ballerina/io;

function bar() returns int {
    return a + b + d + (s * 6);
}

function bar1() returns int {
    // This is a comment
    return a;
}

public function add() {
    int z = x + y;
}

function foo() {
    return a + b; //This is a comment
    int a = 5 + 10;
}

public function multiply() {
    int k = a * (b - c) + d / (e + (f + g));
}
