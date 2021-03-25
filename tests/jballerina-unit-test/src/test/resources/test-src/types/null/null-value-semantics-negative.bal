function testInvalidFunctionCallWithNull() returns (any) {
    string? s = ();
    return foo(s);
}

function foo(string? s) returns (string?){
    return s;
}

function testLogicalOperationOnNull1() returns (boolean) {
    xml? x = ();
    return (() > x);
}

function testNullForValueType1() {
    int a = ();
}

function testArithmaticOperationOnNull() returns (any) {
    return (null + null);
}

function testNullForValueType2() {
    string s = ();
}

function testNullForValueType3() {
    json j = null;
}

function testArithmaticOperationOnNull2() returns (any) {
    return (() + ());
}

type A A[]|int;
type Person record {| string name; |};

function testNullValueNegativeScenarios() {
    string a = null;
    string|int b = null;
    map<string> c = null;
    A d = null;
    int[] e = [null];
    [string, int] f = [null, 2];
    Person g = null;
}
