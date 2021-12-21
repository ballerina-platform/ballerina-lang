import ballerina/lang.'xml as xmllib;
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
    x += 1.0;
    return x;
}

function testDecrementOperatorFloat() returns (float){
    float x = 100.0;
    x -= 1.0;
    return x;
}

function testIntFloatCompoundAssignmentAddition() returns (float){
    int x = 5;
    float d = 2.5;
    d += <float>x;
    return d;
}

function testXMLAttributeWithCompoundAssignment() returns (string){
    xmllib:Element x1 = <xmllib:Element> xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    map<string> m = x1.getAttributes();
    m[ns0:foo1] = "bar1";
    var result = x1.ns0:foo1;

    if (result is string) {
        result += "bar2";
        return checkpanic result;
    } else {
        return result.toString();
    }
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

const int FOUR = 4;
const int FIVE = 5;

type FourOrFive FOUR|FIVE;

type FiveOrSix 5|6;

const float A = 25.0;
const float B = 10.5;

type AB A|B;

type C 10.5|30.5;

const decimal D = 4.5;
const decimal E = 10.5;

type DE D|E;

type F 10d|30d;

const M = "M";
const N = "N";

type O M|N;

type P "Cat"|"Dog";

function testCompoundAssignmentAdditionWithTypes() {
    1 one = 1;
    int two = 2;
    int|int:Signed32 three = 3;
    FourOrFive four = 4;
    FiveOrSix five = 5;
    byte six = 6;
    int[] arr1 = [10, 20, 30];
    AB a1 = 10.5;
    float a2 = 40.5;
    C a3 = 30.5;
    float[] a4 = [10.5, 20.5];
    record {| float count = 10.0; |} a5 = {};
    DE a6 = 4.5;
    F a7 = 10d;
    decimal a8 = 10;

    two += one;
    assertEqual(two, 3);

    three += one;
    assertEqual(three, 4);

    three += four;
    assertEqual(three, 8);

    two += four;
    assertEqual(two, 7);

    two += five;
    assertEqual(two, 12);

    three += five;
    assertEqual(three, 13);

    two += three;
    assertEqual(two, 25);

    three += two;
    assertEqual(three, 38);

    two += six;
    assertEqual(two, 31);

    three += six;
    assertEqual(three, 44);

    arr1[2] += getNumberFive();
    assertEqual(arr1[2], 35);

    arr1[0] += one;
    assertEqual(arr1[0], 11);

    arr1[0] += four;
    assertEqual(arr1[0], 15);

    arr1[0] += five;
    assertEqual(arr1[0], 20);

    arr1[0] += three;
    assertEqual(arr1[0], 64);

    Girl g = {};
    g.age += one;
    assertEqual(g.age, 1);

    g.age += three;
    assertEqual(g.age, 45);

    g.age += four;
    assertEqual(g.age, 49);

    g.age += five;
    assertEqual(g.age, 54);

    Company com = {};
    com["count"] += four;
    assertEqual(com["count"], 4);

    com["count"] += five;
    assertEqual(com["count"], 9);

    com["count"] += one;
    assertEqual(com["count"], 10);

    a2 += a1;
    assertEqual(a2, 51.0);

    a2 += a3;
    assertEqual(a2, 81.5);

    a4[1] += a1;
    assertEqual(a4[1], 31.0);

    a4[1] += a3;
    assertEqual(a4[1], 61.5);

    a5.count += a1;
    assertEqual(a5.count, 20.5);

    a5.count += a3;
    assertEqual(a5.count, 51.0);

    a8 += a6;
    assertEqual(a8, 14.5d);

    a8 += a7;
    assertEqual(a8, 24.5d);

    string x1 = "abc";
    O x2 = "M";
    string|string:Char x3 = "EFG";
    P x4 = "Cat";
    xml x5 = xml `abc`;
    xml:Text|xml x7 = xml `abdef`;

    x1 += x2;
    assertEqual(x1, "abcM");

    x1 += x3;
    assertEqual(x1, "abcMEFG");

    x1 += x4;
    assertEqual(x1, "abcMEFGCat");

    x3 += x2;
    assertEqual(x3, "EFGM");

    x3 += x4;
    assertEqual(x3, "EFGMCat");

    x5 += x2;
    assertEqual(x5, xml `abcM`);

    x5 += x3;
    assertEqual(x5, xml `abcMEFGMCat`);

    x5 += x4;
    assertEqual(x5, xml `abcMEFGMCatCat`);

    x5 += x7;
    assertEqual(x5, xml `abcMEFGMCatCatabdef`);
}

function getNumberFive() returns FourOrFive {
    return 5;
}

function testCompoundAssignmentSubtractionWithTypes() {
    1 one = 1;
    int two = 2;
    int|int:Signed32 three = 3;
    FourOrFive four = 4;
    FiveOrSix five = 5;
    byte six = 6;
    int[] arr1 = [10, 20, 30];
    AB a1 = 10.5;
    float a2 = 40.5;
    C a3 = 30.5;
    float[] a4 = [10.5, 20.5];
    record {| float count = 10.0; |} a5 = {};
    DE a6 = 4.5;
    F a7 = 10d;
    decimal a8 = 10;

    two -= one;
    assertEqual(two, 1);

    three -= one;
    assertEqual(three, 2);

    three -= four;
    assertEqual(three, -2);

    two -= four;
    assertEqual(two, -3);

    two -= five;
    assertEqual(two, -8);

    three -= five;
    assertEqual(three, -7);

    two -= three;
    assertEqual(two, -1);

    three -= two;
    assertEqual(three, -6);

    two -= six;
    assertEqual(two, -7);

    three -= six;
    assertEqual(three, -12);

    arr1[2] -= getNumberFive();
    assertEqual(arr1[2], 25);

    arr1[0] -= one;
    assertEqual(arr1[0], 9);

    arr1[0] -= four;
    assertEqual(arr1[0], 5);

    arr1[0] -= five;
    assertEqual(arr1[0], 0);

    arr1[0] -= three;
    assertEqual(arr1[0], 12);

    Girl g = {};
    g.age -= one;
    assertEqual(g.age, -1);

    g.age -= three;
    assertEqual(g.age, 11);

    g.age -= four;
    assertEqual(g.age, 7);

    g.age -= five;
    assertEqual(g.age, 2);

    Company com = {};
    com["count"] -= four;
    assertEqual(com["count"], -4);

    com["count"] -= five;
    assertEqual(com["count"], -9);

    com["count"] -= one;
    assertEqual(com["count"], -10);

    a2 -= a1;
    assertEqual(a2, 30.0);

    a2 -= a3;
    assertEqual(a2, -0.5);

    a4[1] -= a1;
    assertEqual(a4[1], 10.0);

    a4[1] -= a3;
    assertEqual(a4[1], -20.5);

    a5.count -= a1;
    assertEqual(a5.count, -0.5);

    a5.count -= a3;
    assertEqual(a5.count, -31.0);

    a8 -= a6;
    assertEqual(a8, 5.5d);

    a8 -= a7;
    assertEqual(a8, -4.5d);
}

function testCompoundAssignmentMultiplicationWithTypes() {
    1 one = 1;
    int two = 2;
    int|int:Signed32 three = 3;
    FourOrFive four = 4;
    FiveOrSix five = 5;
    byte six = 6;
    int[] arr1 = [10, 20, 30];
    AB a1 = 10.5;
    float a2 = 40.5;
    C a3 = 30.5;
    float[] a4 = [10.5, 5.5];
    record {| float count = 10.0; |} a5 = {};
    DE a6 = 4.5;
    F a7 = 10d;
    decimal a8 = 10;

    two *= one;
    assertEqual(two, 2);

    three *= one;
    assertEqual(three, 3);

    three *= four;
    assertEqual(three, 12);

    two *= four;
    assertEqual(two, 8);

    two *= five;
    assertEqual(two, 40);

    three *= five;
    assertEqual(three, 60);

    two *= three;
    assertEqual(two, 2400);

    three *= two;
    assertEqual(three, 144000);

    two *= six;
    assertEqual(two, 14400);

    three *= six;
    assertEqual(three, 864000);

    arr1[2] *= getNumberFive();
    assertEqual(arr1[2], 150);

    arr1[0] *= one;
    assertEqual(arr1[0], 10);

    arr1[0] *= four;
    assertEqual(arr1[0], 40);

    arr1[0] *= five;
    assertEqual(arr1[0], 200);

    arr1[0] *= three;
    assertEqual(arr1[0], 172800000);

    Girl g = {age: 30};
    g.age *= one;
    assertEqual(g.age, 30);

    g.age *= three;
    assertEqual(g.age, 25920000);

    g.age *= four;
    assertEqual(g.age, 103680000);

    g.age *= five;
    assertEqual(g.age, 518400000);

    Company com = {count: 100};
    com["count"] *= four;
    assertEqual(com["count"], 400);

    com["count"] *= five;
    assertEqual(com["count"], 2000);

    com["count"] *= one;
    assertEqual(com["count"], 2000);

    //AB a1 = 10.5;
    //float a2 = 40.5;
    //C a3 = 30.5;
    //float[] a4 = [10.5, 5.5];
    //record {| float count = 10.0; |} a5 = {};
    //DE a6 = 4.5;
    //F a7 = 10d;
    //decimal a8 = 10;

    a2 *= a1;
    assertEqual(a2, 425.25);

    a2 *= a3;
    assertEqual(a2, 12970.125);

    a4[1] *= a1;
    assertEqual(a4[1], 57.75);

    a4[1] *= a3;
    assertEqual(a4[1], 1761.375);

    a5.count *= a1;
    assertEqual(a5.count, 105.0);

    a5.count *= a3;
    assertEqual(a5.count, 3202.5);

    a8 *= a6;
    assertEqual(a8, 45d);

    a8 *= a7;
    assertEqual(a8, 450d);
}

function testCompoundAssignmentDivisionWithTypes() {
    1 one = 1;
    int two = 300;
    int|int:Signed32 three = 300;
    FourOrFive four = 4;
    FiveOrSix five = 5;
    byte six = 6;
    int[] arr1 = [300, 20, 30];
    AB a1 = 25.0;
    float a2 = 1235.25;
    C a3 = 30.5;
    float[] a4 = [300.5, 1235.25];
    record {| float count = 3050.0; |} a5 = {};
    DE a6 = 10.5;
    F a7 = 10d;
    decimal a8 = 10.5;

    two /= one;
    assertEqual(two, 300);

    three /= one;
    assertEqual(three, 300);

    three /= four;
    assertEqual(three, 75);

    two /= four;
    assertEqual(two, 75);

    two /= five;
    assertEqual(two, 15);

    three /= five;
    assertEqual(three, 15);

    two /= three;
    assertEqual(two, 1);

    three /= two;
    assertEqual(three, 15);

    two /= six;
    assertEqual(two, 0);

    three /= six;
    assertEqual(three, 2);

    arr1[2] /= getNumberFive();
    assertEqual(arr1[2], 6);

    arr1[0] /= one;
    assertEqual(arr1[0], 300);

    arr1[0] /= four;
    assertEqual(arr1[0], 75);

    arr1[0] /= five;
    assertEqual(arr1[0], 15);

    arr1[0] /= three;
    assertEqual(arr1[0], 7);

    Girl g = {age: 300};
    g.age /= one;
    assertEqual(g.age, 300);

    g.age /= three;
    assertEqual(g.age, 150);

    g.age /= four;
    assertEqual(g.age, 37);

    g.age /= five;
    assertEqual(g.age, 7);

    Company com = {count: 300};
    com["count"] /= four;
    assertEqual(com["count"], 75);

    com["count"] /= five;
    assertEqual(com["count"], 15);

    com["count"] /= one;
    assertEqual(com["count"], 15);

    a2 /= a1;
    assertEqual(a2, 49.41);

    a2 /= a3;
    assertEqual(a2, 1.6199999999999999);

    a4[1] /= a1;
    assertEqual(a4[1], 49.41);

    a4[1] /= a3;
    assertEqual(a4[1], 1.6199999999999999);

    a5.count /= a1;
    assertEqual(a5.count, 122.0);

    a5.count /= a3;
    assertEqual(a5.count, 4.0);

    a8 /= a6;
    assertEqual(a8, 1d);

    a8 /= a7;
    assertEqual(a8, 0.1d);
}

public const SIXTY_TWO = 62;
public const SIXTY_THREE = 63;
public const SIXTY_FOUR = 64;
public const SIXTY_FIVE = 65;

const int CAI = 60 + 2;
const int CAF = 60 + 3;
const int CAD = 60 + 4;

type SixtiesCode SIXTY_TWO|SIXTY_THREE|SIXTY_FOUR|SIXTY_FIVE;

type ThisOrThat 63|65;

type SixtiesConst CAI|CAF|CAD;

public const byte ONE = 1;
public const byte TWO = 2;
public const byte THREE = 3;

type ThreeNumbers ONE|TWO|THREE;

function testCompoundAssignmentBitwiseLeftShift() {
    SixtiesCode a1 = 62;
    int x1 = 1;
    x1 <<= a1;
    assertEqual(x1, 0x4000000000000000);

    int x2 = 1;
    x2 <<= SIXTY_TWO;
    assertEqual(x2, 0x4000000000000000);

    SIXTY_TWO|SIXTY_FOUR a2 = 64;
    int x3 = 1;
    x3 <<= a2;
    assertEqual(x3, 0x1);

    ThisOrThat a3 = 65;
    int x4 = 1;
    x4 <<= a3;
    assertEqual(x4, 0x2);

    SixtiesConst a4 = 62;
    int x5 = 1;
    x5 <<= a4;
    assertEqual(x5, 0x4000000000000000);

    SixtiesConst a5 = 63;
    int x6 = 1;
    x6 <<= a5;
    assertEqual(x6, -0x8000000000000000);

    CAI|CAD a7 = 64;
    int x7 = 1;
    x7 <<= a7;
    assertEqual(x7, 0x1);

    ThreeNumbers|int a8 = 3;
    a8 <<= SIXTY_TWO;
    assertEqual(a8, -0x4000000000000000);

    int|int:Unsigned32 a9 = 1;
    int a10 = 64;
    a9 <<= a10;
    assertEqual(a9, 0x1);
}

function testCompoundAssignmentBitwiseRightShift() {
    SixtiesCode a1 = 62;
    int x1 = 1;
    x1 >>= a1;
    assertEqual(x1, 0x0);

    int x2 = 1;
    x2 >>= SIXTY_TWO;
    assertEqual(x2, 0x0);

    SIXTY_TWO|SIXTY_FOUR a2 = 64;
    int x3 = 1;
    x3 >>= a2;
    assertEqual(x3, 0x1);

    ThisOrThat a3 = 65;
    int x4 = 1;
    x4 >>= a3;
    assertEqual(x4, 0x0);

    SixtiesConst a4 = 62;
    int x5 = 1;
    x5 >>= a4;
    assertEqual(x5, 0x0);

    CAI|CAD a5 = 64;
    int x6 = 1;
    x6 >>= a5;
    assertEqual(x6, 0x1);

    ThreeNumbers|int a6 = 3;
    a6 >>= SIXTY_TWO;
    assertEqual(a6, 0x0);

    int|int:Unsigned16 a7 = 1;
    int:Unsigned32 a8 = 64;
    a7 >>= a8;
    assertEqual(a7, 0x1);
}

function testCompoundAssignmentBitwiseUnsignedRightShift() {
    int|int:Signed32 a1 = -32;
    int x1 = 2;
    a1 >>>= x1;
    assertEqual(a1, 0x3ffffffffffffff8);

    byte a2 = 2;
    int x2 = -32;
    x2 >>>= a2;
    assertEqual(x2, 0x3ffffffffffffff8);

    2 a3 = 2;
    int x3 = 15;
    x3 >>>= a3;
    assertEqual(x3, 0x3);

    2|4 a4 = 2;
    int x4 = -32;
    x4 >>>= a4;
    assertEqual(x4, 0x3ffffffffffffff8);

    ThreeNumbers a5 = 2;
    int x5 = -32;
    x5 >>>= a5;
    assertEqual(x5, 0x3ffffffffffffff8);

    int|int:Unsigned32 a6 = 15;
    2 x6 = 2;
    a6 >>>= x6;
    assertEqual(a6, 0x3);
}

const PANIC_ARITHMETIC_OVERFLOW = 1;
const PANIC_TYPE_CAST = 3;
const PANIC_INDEX_OUT_OF_BOUNDS = 5;

type PanicIndex PANIC_ARITHMETIC_OVERFLOW|PANIC_TYPE_CAST|PANIC_INDEX_OUT_OF_BOUNDS;

type Numbers 12|25;

const NEG_THIRTY_TWO = -32;
const INT_VAL = 25;

function testCompoundAssignmentBitwiseANDOperation() {
    PanicIndex panicIndex = 5;
    int a1 = 3;
    a1 &= panicIndex;
    assertEqual(a1, 1);

    byte a2 = 0x5;
    a2 &= panicIndex;
    assertEqual(a2, 0x5);

    byte a3 = 12;
    a3 &= panicIndex;
    assertEqual(a3, 0x4);

    int:Signed32|int a4 = 12;
    a4 &= panicIndex;
    assertEqual(a4, 0x4);

    ThreeNumbers c = 3;
    int:Signed32|int a5 = 12;
    a5 &= c;
    assertEqual(a5, 0);

    int a6 = 12;
    a6 &= NEG_THIRTY_TWO;
    assertEqual(a6, 0);

    byte a7 = 12;
    a7 &= 5;
    assertEqual(a7, 4);

    int a8 = 5;
    a8 &= 12;
    assertEqual(a8, 4);

    int a9 = 32;
    a9 &= NEG_THIRTY_TWO;
    assertEqual(a9, 32);

    int:Unsigned16 a10 = 12;
    a10 &= 5;
    assertEqual(a10, 4);

    int:Unsigned32 d = 5;
    int:Unsigned16 a11 = 12;
    a11 &= d;
    assertEqual(a11, 4);

    int:Unsigned32 a12 = 12;
    a12 &= panicIndex;
    assertEqual(a12, 4);

    int:Unsigned16 a13 = 12;
    int e = 5;
    a13 &= e;
    assertEqual(a13, 4);

    Numbers f = 12;
    int a14 = 25;
    a14 &= f;
    assertEqual(a14, 8);
}

function testCompoundAssignmentBitwiseOROperation() {
    PanicIndex panicIndex = 5;
    int a1 = 3;
    a1 |= panicIndex;
    assertEqual(a1, 7);

    a1 |= INT_VAL;
    assertEqual(a1, 31);

    byte|int a2 = 0x5;
    a2 |= panicIndex;
    assertEqual(a2, 0x5);

    int:Signed32|int a3 = 12;
    a3 |= panicIndex;
    assertEqual(a3, 13);

    ThreeNumbers a = 3;
    int a4 = 12;
    a4 |= a;
    assertEqual(a4, 15);

    int a5 = 12;
    a5 |= NEG_THIRTY_TWO;
    assertEqual(a5, -20);

    int a6 = 5;
    a6 |= 12;
    assertEqual(a6, 13);

    int:Unsigned16 b = 12;
    int a7 = 5;
    a7 |= b;
    assertEqual(a7, 13);

    int:Unsigned16 a8 = 5;
    int:Unsigned32 a9 = 12;
    a9 |= a8;
    assertEqual(a9, 13);
}

function testCompoundAssignmentBitwiseXOROperation() {
    PanicIndex panicIndex = 5;
    int a1 = 3;
    a1 ^= panicIndex;
    assertEqual(a1, 6);

    int a2 = 0x5;
    a2 ^= panicIndex;
    assertEqual(a2, 0);

    byte a = 12;
    int a3 = 5;
    a3 ^= a;
    assertEqual(a3, 9);

    int:Signed32|int:Unsigned8|int a4 = 12;
    a4 ^= panicIndex;
    assertEqual(a4, 9);

    ThreeNumbers b = 3;
    int a5 = 12;
    a5 ^= b;
    assertEqual(a5, 15);

    int a6 = 12;
    a6 ^= NEG_THIRTY_TWO;
    assertEqual(a6, -20);

    int a7 = 5;
    int c = 12;
    a7 ^= c;
    assertEqual(a7, 9);

    int a8 = 12;
    a8 ^= NEG_THIRTY_TWO;
    assertEqual(a8, -20);

    int:Unsigned16 d = 12;
    int a9 = 5;
    a9 ^= d;
    assertEqual(a9, 9);

    int:Unsigned16 e = 5;
    int:Unsigned32 a10 = 12;
    a10 ^= e;
    assertEqual(a10, 9);

    int:Unsigned32|int a11 = 12;
    a11 ^= panicIndex;
    assertEqual(a11, 9);
}

function testCompoundAssignmentMemberAccess() {
       map<int>? m = {x:2};
       m["x"] += 1;
       assertEqual(m["x"], 3);
}

function assertEqual(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }

    if actual === expected {
        return;
    }

    string actualValAsString = actual.toString();
    string expectedValAsString = expected.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
