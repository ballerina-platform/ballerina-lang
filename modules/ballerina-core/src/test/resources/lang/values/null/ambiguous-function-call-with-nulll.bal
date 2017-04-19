function testAmbiguousFunctionCallWithNull() (any) {
    return foo(null);
}

function foo(string s) {
    return s;
}

function foo(xml x) {
    return x;
}

function foo(json j) {
    json j;
}
