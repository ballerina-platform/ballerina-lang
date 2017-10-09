function testCompareNullOfDifferentTypes () (int, xml, json) {
    xml x = null;
    json j = null;
    int a = 0;
    if (x == j) {
        a = 11;
    }

    return a, x, j;
}

function testInvalidFunctionCallWithNull() (any) {
    return foo(null);
}

function foo(string s) (string){
    return s;
}

function testLogicalOperationOnNull1() (boolean) {
    xml x;
    return (null > x);
}

function testNullForValueType1() {
    int a = null;
}

function testLogicalOperationOnNull2() (any) {
    return (null + null);
}

function testNullForValueType2() {
    //string s = (string) null;
}

function testNullForValueType3() {
    //json j = (json) null;
}