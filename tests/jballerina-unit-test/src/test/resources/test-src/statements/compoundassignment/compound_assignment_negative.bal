import ballerina/lang.'int as ints;

function testMapElementIncrement()  returns int|error {
    map<any> namesMap = { fname: 1 };
    namesMap["fname"] += 1;
    int x;
    x = check trap <int> namesMap["fname"];
    return x;
}

function testMapElementDecrement() returns int|error {
    map<any> namesMap = {fname:1};
    namesMap["fname"] -= 1;
    int x;
    x = check trap <int> namesMap["fname"];
    return x;
}

function testInvalidExpressionIncrement() returns  (int) {
    getInt() += 1;
    return getInt();
}

function testInvalidExpressionDecrement() returns  (int) {
    getInt() -= 1;
    return getInt();
}

function getInt() returns (int){
    return 3;
}

function testStringVarRefIncrement() returns (string){
    string x = "compound";
    x += 1;
    return x;
}

function testStringVarRefDecrement() returns (string){
    string x = "compound";
    x -= 1;
    return x;
}

function testMultiReturnWithCompound() returns (int){
    int x = 4;
    x += ints:fromString("NotAInteger");
    return x;
}

function testInvalidRefExpressionWithCompound() returns (int){
    int x = 5;
    getInt() += x;
    return x;
}

function testInvalidTypeJsonStringCompound() returns (json){
    json j = {"test":"test"};
    j += "sdasdasd";
    return j;
}

function testInvalidTypeIntStringCompound() returns (int){
    int x = 5;
    x += "sdasd";
    return x;
}

function testIntFloatDivision() returns (float){
    int x = 5;
    float d = 2.5;
    x /= <int>d;
    return x;
}

function testCompoundAssignmentAdditionWithFunctionInvocation() returns (int){
    int x = 5;
    x += getMultiIncrement();
    return x;
}


function getMultiIncrement() returns [int, int] {
   return [200, 100];
}


function testCompoundAssignmentBitwiseAND() returns (int){
    int x = 15;
    x &= "Ballerina";
    return x;
}

function testCompoundAssignmentBitwiseOR() returns (int){
    int x = 15;
    x |= "Ballerina";
    return x;
}

function testCompoundAssignmentBitwiseXOR() returns (int){
    int x = 15;
    x ^= "Ballerina";
    return x;
}

function testCompoundAssignmentLeftShift() returns (int){
    int x = 8;
    x <<= "Ballerina";
    return x;
}

function testCompoundAssignmentRightShift() returns (int){
    int x = 8;
    x >>= "Ballerina";
    return x;
}

function testCompoundAssignmentLogicalShift() returns (int){
    int x = 8;
    x >>>= "Ballerina";
    return x;
}

function testFunctionInvocation() returns (int) {
    Bar bar = {};
    foo(bar).bar += 10;
}

function foo(Bar b) returns Bar {
    b.bar += 1;
    return b;
}

type Bar record {
    int bar = 0;
};

function invalidUsageOfCompoundAssignmentAsExpr() {
    int a = 5;
    int f = a <<= 3;
    int g = a >>= 3;
}

type SomeType int|string;
type SomeType2 12|"A";

function incompatibleTypesInBinaryBitwiseOpInCompoundAssignment() {
    int|string a = 5;
    int b = 12;
    a &= b;
    a |= b;
    a ^= b;

    SomeType c = 12;
    a &= c;
    a |= c;
    a ^= c;

    SomeType2 d = 12;
    a &= d;
    a |= d;
    a ^= d;
}

function testCompoundAssignmentNotAllowedWithNullableOperands() {
    map<int>? m = {x: 2};
    m["x"] += 1;

    int? a = ();
    a += 2;

    record {|int name?; int? age;|} b = {age: ()};
    b.name += 4;
    b.age += 4;

    ()|int c = 5;
    c += 4;
}
