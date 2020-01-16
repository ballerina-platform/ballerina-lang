xmlns "http://sample.com/wso2/a1" as ns0;

function testCompoundAssignmentAddition() returns (int){
    int x = 5;
    x += 10;
    return x;
}

function testCompoundAssignmentSubtraction()  returns (int){
    int x = 5;
    x -= 10;
    return x;
}

function testCompoundAssignmentMultiplication() returns (int){
    int x = 5;
    x *= 10;
    return x;
}

function testCompoundAssignmentDivision() returns (int){
    int x = 100;
    x /= 10;
    return x;
}

function testCompoundAssignmentBitwiseAND() returns (int){
    int x = 15;
    x &= 5;
    return x;
}

function testCompoundAssignmentBitwiseOR() returns (int){
    int x = 15;
    x |= 5;
    return x;
}

function testCompoundAssignmentBitwiseXOR() returns (int){
    int x = 15;
    x ^= 5;
    return x;
}

function testCompoundAssignmentLeftShift() returns (int){
    int x = 8;
    x <<= 1;
    return x;
}

function testCompoundAssignmentRightShift() returns (int){
    int x = 8;
    x >>= 1;
    return x;
}

function testCompoundAssignmentLogicalShift() returns (int){
    int x = 8;
    x >>>= 1;
    return x;
}

function testCompoundAssignmentAdditionArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0] += 10;
    return x[0];
}

function testCompoundAssignmentSubtractionArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0] -= 10;
    return x[0];
}

function testCompoundAssignmentMultiplicationArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0] *= 10;
    return x[0];
}

function testCompoundAssignmentDivisionArrayElement() returns (int){
     int[] x = [];
     x[0] = 100;
     x[0] /= 10;
     return x[0];
}

type Company record {
   int count = 0;
   int count2 = 0;
};

function testCompoundAssignmentAdditionStructElement() returns (int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count += 10;
    return ibm.count;
}

function testCompoundAssignmentSubtractionStructElement() returns (int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count -= 10;
    return ibm.count;
}

function testCompoundAssignmentMultiplicationStructElement() returns (int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count *= 10;
    return ibm.count;
}

function testCompoundAssignmentDivisionStructElement() returns (int){
    Company ibm = {};
    ibm.count = 100;
    ibm.count /= 10;
    return ibm.count;
}

function testIncrementOperatorArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0] += 1;
    return x[0];
}

function testDecrementOperatorArrayElement() returns (int){
    int[] x = [];
    x[0] = 100;
    x[0] -= 1;
    return x[0];
}

function testIncrementOperatorStructElement() returns (int){
    Company ibm = {};
    ibm.count = 888;
    ibm.count += 1;
    return ibm.count;
}

function testDecrementOperatorStructElement() returns (int){
    Company ibm = {};
    ibm.count = 888;
    ibm.count -= 1;
    return ibm.count;
}

function testStringIntCompoundAssignmentAddition() returns (string){
    int x = 5;
    string a = "test";
    a += x.toString();
    return a;
}

function testIncrementOperatorFloat() returns (float){
    float x = 100.0;
    x += 1;
    return x;
}

function testDecrementOperatorFloat() returns (float){
    float x = 100.0;
    x -= 1;
    return x;
}

function testIntFloatCompoundAssignmentAddition() returns (float){
    int x = 5;
    float d = 2.5;
    d += x;
    return d;
}

function testXMLAttributeWithCompoundAssignment() returns (string){
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    x1@[ns0:foo1] = "bar1";
    var result = x1@[ns0:foo1];

    if (result is string) {
        result += "bar2";
        return result;
    }

    return "";
}

function testCompoundAssignmentAdditionRecursive() returns (int){
    int x = 5;
    x += x;
    return x;
}

function testCompoundAssignmentAdditionStructElementRecursive() returns int? {
    Company ibm = {};
    ibm["count"] = 100;
    ibm.count += ibm.count;
    return ibm["count"];
}

function testCompoundAssignmentAdditionStructElements() returns int? {
    Company ibm = {};
    ibm["count"] = 100;
    ibm["count2"] = 400;
    ibm.count += ibm.count2;
    return ibm["count"];
}

function testCompoundAssignmentAdditionWithExpression() returns (int){
    int x = 5;
    x += (2+3+4+5);
    return x;
}

function testCompoundAssignmentAdditionMultiple() returns (int){
    int x = 5;
    x += 5;
    x += 5;
    x += 5;
    return x;
}

function testCompoundAssignmentAdditionMultipleWithIncrement() returns (int){
    int x = 5;
    x += 5;
    x += 1;
    x += 5;
    x += 1;
    x += 5;
    x -= 1;
    x -= 1;
    x -= 1;
    return x;
}

function testCompoundAssignmentAdditionWithStructAccess() returns (int){
    Company ibm = {};
    ibm["count"] = 100;
    int[] arr = [];
    arr[0] = 200;
    int x = 5;
    x += (ibm.count + arr[0]);
    return x;
}

function testCompoundAssignmentAdditionWithFunctionInvocation() returns (int){
    int x = 5;
    x += getIncrement();
    return x;
}


function getIncrement() returns (int) {
   return 200;
}

function xmlCompoundAssignment() returns (xml){
    xml x = xml `hello`;
    xml y = xml `<hello>hi</hello>`;
    xml z = x + y;
    string blah = "blah";
    z += "hah";
    z += blah;
    return z;
}

function testCompoundAssignmentAdditionRecordElementRecursive() returns int {
    Company ibm = {};
    ibm["count"] = 100;
    ibm["count"] += ibm["count"];
    return ibm["count"];
}

function testCompoundAssignmentAdditionRecordElements() returns int {
    Company ibm = { count: 100 };
    ibm["count2"] = 400;
    ibm["count"] += ibm["count2"];
    return ibm["count"];
}

function testCompoundAssignmentAdditionWithRecordAccess() returns int {
    Company ibm = {};
    ibm["count"] = 100;
    int[] arr = [];
    arr[0] = 200;
    int x = 5;
    x += (ibm["count"] + arr[0]);
    return x;
}

function testCompoundAssignmentAdditionArrayElementFunctionInvocation() returns [int, int, int] {
    var x = 5;
    Girl girl = {};
    Boy boy = {};
    int[][][] b = [[[1, 2, 3], [3, 2, 1], [3, 2, 1]], [[10, 20, 30], [30, 20, 10], [30, 20, 10]],
    [[5, 6, 7], [7, 6, 5], [7, 6, 5]]];

    b[incrementAgeOfGirl(1, girl)][2][incrementAgeOfBoy(0, boy)] += x;
    return [b[2][2][1], girl.age, boy.age];
}

function testCompoundAssignmentSubtractionArrayElementFunctionInvocation() returns [int, int, int] {
    var x = 5;
    Girl girl = {};
    Boy boy = {};
    int[][][] b = [[[1, 2, 3], [3, 2, 1], [3, 2, 1]], [[10, 20, 30], [30, 20, 10], [30, 20, 10]],
    [[5, 6, 7], [7, 6, 5], [7, 6, 5]]];

    b[incrementAgeOfGirl(1, girl)][2][incrementAgeOfBoy(0, boy)] -= x;
    return [b[2][2][1], girl.age, boy.age];
}

function testCompoundAssignmentDivisionArrayElementFunctionInvocation() returns [int, int, int] {
    var x = 5;
    Girl girl = {};
    Boy boy = {};
    int[][][] b = [[[1, 2, 3], [3, 2, 1], [3, 2, 1]], [[10, 20, 30], [30, 20, 10], [30, 20, 10]],
    [[5, 6, 7], [7, 6, 5], [7, 6, 5]]];

    b[incrementAgeOfGirl(1, girl)][2][incrementAgeOfBoy(0, boy)] /= x;
    return [b[2][2][1], girl.age, boy.age];
}

function testCompoundAssignmentMultiplicationArrayElementFunctionInvocation() returns [int, int, int] {
    var x = 5;
    Girl girl = {};
    Boy boy = {};
    int[][][] b = [[[1, 2, 3], [3, 2, 1], [3, 2, 1]], [[10, 20, 30], [30, 20, 10], [30, 20, 10]],
    [[5, 6, 7], [7, 6, 5], [7, 6, 5]]];

    b[incrementAgeOfGirl(1, girl)][2][incrementAgeOfBoy(0, boy)] *= x;
    return [b[2][2][1], girl.age, boy.age];
}

function testCompoundAssignmentArrayElementFunctionInvocationOrder() returns [int, int, int] {
    var x = 3;
    Girl girl = {};
    Boy boy = {};
    int[][][] b = [[[1, 2, 3], [3, 2, 1], [3, 2, 1]], [[10, 20, 30], [30, 20, 10], [30, 20, 10]],
    [[5, 6, 7], [7, 6, 5], [7, 6, 5]]];

    b[incrementIndexOfGirl(girl)][2][incrementIndexOfBoy(boy)] *= x;
    return [b[1][2][2], girl.age, boy.age];
}

function incrementAgeOfGirl(int i, Girl g) returns (int) {
    g.age += 1;
    return i + 1;
}

function incrementAgeOfBoy(int i, Boy b) returns (int) {
    b.age += 1;
    return i + 1;
}

int index = 0;

function incrementIndexOfGirl(Girl g) returns (int) {
    g.age += 1;
    index = index + 1;
    return index;
}

function incrementIndexOfBoy(Boy b) returns (int) {
    b.age += 1;
    index = index + 1;
    return index;
}

type Girl record  {
    int age = 0;
};

type Boy record  {
    int age = 0;
};
