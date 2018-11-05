const boolean someBoolean = 10;
const int someInt = "ABC";
const byte someByte = 500;
const float someFloat = true;
const string someString = 120;

// Assigning var ref.
string s = "Ballerina";
const string name = s;
public const string name2 = s;

// Assigning var ref.
int a = 10;
const age = a;
public const age2 = a;

// Updating const.
const x = 10;
const int y = 20;

function testAssignment() {
    x = 1;
    y = 2;
}

// Updating const in worker.
function testWorkerInteractions() {
    worker w1 {
        x <- w2;
    }
    worker w2 {
        30 -> w1;
    }
}

const string sVar = 10;

const string m = { name: "Ballerina" };

// Redeclared constant.
const abc = "abc";

const abc = "Ballerina";

// Redeclared variable.
const def = "def";

function test() {
    string def = "def";
}

// Incompatible types.
type ACTION "GET";

type XYZ "XYZ";

const XYZ xyz = "XYZ";

function testInvalidTypes() returns ACTION {
    ACTION action = xyz;
    return action;
}

// Built-in function invocation.
function testInvalidInvocation() {
    string lowercase = xyz.toLower();
}

const aBoolean = true;

function testBooleanConcat() returns string {
    return aBoolean + " rocks";
}

const aInt = 24;

function testIntConcat() returns string {
    return aInt + " rocks";
}

const aByte = 12;

function testByteConcat() returns string {
    return aByte + " rocks";
}

const aFloat = 25.5;

function testFloatConcat() returns string {
    return aFloat + " rocks";
}

// -----------------------------------------------------------

const byteWithoutType = 120;

function testByteWithoutType() returns byte {
    return byteWithoutType;
}

// -----------------------------------------------------------

const string SHA1 = "SHA1";

function testFunctionInvocation(){
    boolean b = SHA1.equalsIgnoreCase("ABC");
}

// -----------------------------------------------------------

const D = "D";

const E = "E";

const F = "F";

type G E|F;

type H D|E;

const H h = "D";

function testImproperSubset() returns G {
    G g = h;
    return g;
}

// -----------------------------------------------------------

// Cyclic dependency.
type UVW UVW;

// -----------------------------------------------------------

// Cyclic dependency and unknown type.
type IJK IJK|"R"|SSS|"S";

// -----------------------------------------------------------

// Cyclic dependency and multiple unknown types.
type LMN OPQ|"QRS"|RST|STU;

type OPQ LMN|"STU"|RST;

// -----------------------------------------------------------

// Complex cyclic dependency.
type ACE BDF;

type BDF "AAA"|CEG|"TTT"|DFH;

type CEG ACE|"UUU"|EGI;

type DFH "DFH";

type EGI BDF|ACE;

// -----------------------------------------------------------

// Type node's type undefined.
type MNO PQ;

// -----------------------------------------------------------

// Type node's one of member's type not available.
type JKL J|STU;

const J = "J";

type STU S|T|U;

const S = "S";
