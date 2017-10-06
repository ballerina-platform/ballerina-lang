function testCompareNullOfDifferentTypes () (boolean, xml, json) {
    xml x = null;
    json j = null;
    int a = 0;
    if (x == j) {
        a = 11;
    }

    return x, j, a;
}

function testNullForValueType () {
    int a = null;
}

function testNullForValueType () {
    string s = (string)null;
}

function testNullForValueType () {
    json j = (json)null;
}

function testInvalidFunctionCallWithNull () (any) {
    return foo(null);
}

function foo (string s) {
    return s;
}

function testLogicalOperationOnNull () (boolean) {
    xml x;
    return (null > x);
}

function testLogicalOperationOnNull () (any) {
    return (null + null);
}

function testCreateNullTypeVar () {
    null x;
}
