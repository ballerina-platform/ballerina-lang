function testCompareNullOfDifferentTypes () returns (int, xml|null, json) {
    xml | null x = null;
    json | null j = null;
    int a = 0;
    if (x == j) {
        a = 11;
    }

    return (a, x, j);
}

function testInvalidFunctionCallWithNull() returns (any) {
    return foo(null);
}

function foo(string s) returns (string){
    return s;
}

function testLogicalOperationOnNull1() returns (boolean) {
    xml x;
    return (null > x);
}

function testNullForValueType1() {
    int | null a = null;
}

function testLogicalOperationOnNull2() returns (any) {
    return (null + null);
}

function testNullForValueType2() {
    string s = (string) null;
}

function testNullForValueType3() {
    json | null j = null;
}