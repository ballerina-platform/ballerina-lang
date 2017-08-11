function testInvalidFunctionCallWithNull() (any) {
    return foo(null);
}

function foo(string s) {
    return s;
}

