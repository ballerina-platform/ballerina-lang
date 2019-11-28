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

function testIntFloatDivision() returns (int){
    int x = 5;
    float d = 2.5;
    x /= d;
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

type Company record {|
   int count?;
   int...;
|};

function testCompoundAssignmentAdditionStructElementRecursive() returns int? {
    Company ibm = {};
    ibm["count"] = 100;
    ibm["count"] += ibm["count"];
    return ibm["count"];
}

function testCompoundAssignmentAdditionStructElements() returns int? {
    Company ibm = {};
    ibm["count"] = 100;
    ibm["count2"] = 400;
    ibm["count"] += ibm["count2"];
    return ibm["count"];
}

function testCompoundAssignmentAdditionWithStructAccess() returns int {
    Company ibm = {};
    ibm["count"] = 100;
    int[] arr = [];
    arr[0] = 200;
    int x = 5;
    x += (ibm["count"] + arr[0]);
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
