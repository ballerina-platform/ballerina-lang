function testCompareNullOfDifferentTypes () returns (int, xml?, json) {
    xml? x;
    json j = null;
    int a = 0;
    if (x == j) {
        a = 11;
    }

    return (a, x, j);
}

function testInvalidFunctionCallWithNull() returns (any) {
    string? s;
    return foo(s);
}

function foo(string? s) returns (string?){
    return s;
}

function testLogicalOperationOnNull1() returns (boolean) {
    xml x;
    return (() > x);
}

function testNullForValueType1() {
    int a = ();
}

function testLogicalOperationOnNull2() returns (any) {
    return (null + null);
}

function testNullForValueType2() {
    string s = ();
}

function testNullForValueType3() {
    json j = null;
}