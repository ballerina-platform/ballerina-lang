public type Error distinct error;

function bar1() {
    int i = check foo1();
    return foo2();
}

function bar2() {
    int i = check foo2();
    return foo1();
}

function bar3() {
    int i = check foo2();
    return foo3();
}

function foo1() returns int|error {
    return 1;
}

function foo2() returns int|Error {
    return 1;
}

function foo3() returns int|string|Error {
    return 1;
}
