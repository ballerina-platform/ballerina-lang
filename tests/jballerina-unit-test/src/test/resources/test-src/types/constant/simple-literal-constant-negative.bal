const boolean someBoolean = 10; // Invalid RHS value.
const int someInt = "ABC"; // Invalid RHS value.
const byte someByte = 500; // Invalid RHS value.
const float someFloat = true; // Invalid RHS value.
const decimal someDeciaml = true; // Invalid RHS value.
const string someString = 120; // Invalid RHS value.

// Invalid identifier
const string _ = "PUT";
const _ = 11;

const invalidType someValue = 1;

// Assigning var ref.
string s = "Ballerina";
public const string name2 = "";

// Assigning var ref.
int a = 10;

// Updating const.
const x = 10;
const int y = 20;

function testAssignment() {
    x = 1; // Updating constant.
    y = 2; // Updating constant.
}

// Updating constant in a worker.
function testWorkerInteractions() {
    worker w1 {
        x = <- w2; // Updating constant.
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

const xyz = "XYZ";

function testInvalidTypes() returns ACTION {
    ACTION action = xyz; // Incompatibel types.
    return action;
}

// -----------------------------------------------------------

const byteWithoutType = 120;

function testByteWithoutType() returns byte {
    return byteWithoutType; // Invalid return.
}

// -----------------------------------------------------------

const D = "D";

const E = "E";

const F = "F";

type G E|F;

type H D|E;

H h = "D";

function testImproperSubset() returns G {
    G g = h; // Test improper subset assignment.
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

type STU S|T|U; // T, U are undefined.

const S = "S";

// -----------------------------------------------------------

function testInvalidConstUsage() {
    string wxy = name2; // name2 has an invalid RHS.
}

// -----------------------------------------------------------

const boolean booleanWithType = false;

type BooleanTypeWithType booleanWithType;

function testBooleanTypeWithType() returns BooleanTypeWithType {
    BooleanTypeWithType t = true; // Invalid value assignment.
    return t;
}

const booleanWithoutType = true;

type BooleanTypeWithoutType booleanWithoutType;

function testBooleanTypeWithoutType() returns BooleanTypeWithoutType {
    BooleanTypeWithoutType t = false; // Invalid value assignment.
    return t;
}

// -----------------------------------------------------------

const int intWithType = 40;

type IntTypeWithType intWithType;

function testIntTypeWithType() returns IntTypeWithType {
    IntTypeWithType t = 100; // Invalid value assignment.
    return t;
}

const intWithoutType = 20;

type IntTypeWithoutType intWithoutType;

function testIntTypeWithoutType() returns IntTypeWithoutType {
    IntTypeWithoutType t = 100; // Invalid value assignment.
    return t;
}

// -----------------------------------------------------------

const byte byteWithType = 240;

type ByteTypeWithType byteWithType;

function testByteTypeWithType() returns ByteTypeWithType {
    ByteTypeWithType t = 120; // Invalid value assignment.
    return t;
}

// -----------------------------------------------------------

const float floatWithType = 4.0;

type FloatTypeWithType floatWithType;

function testFloatTypeWithType() returns FloatTypeWithType {
    FloatTypeWithType t = 10.0; // Invalid value assignment.
    return t;
}

const floatWithoutType = 2.0;

type FloatTypeWithoutType floatWithoutType;

function testFloatTypeWithoutType() returns FloatTypeWithoutType {
    FloatTypeWithoutType t = 10.0; // Invalid value assignment.
    return t;
}

// -----------------------------------------------------------

const decimal decimalWithType = 4.0;

type DecimalTypeWithType decimalWithType;

function testDecimalTypeWithType() returns DecimalTypeWithType {
    DecimalTypeWithType t = 10.0; // Invalid value assignment.
    return t;
}

// -----------------------------------------------------------

const string stringWithType = "Ballerina is awesome";

type StringTypeWithType stringWithType;

function testStringTypeWithType() returns StringTypeWithType {
    StringTypeWithType t = "random text"; // Invalid value assignment.
    return t;
}

const stringWithoutType = "Ballerina rocks";

type StringTypeWithoutType stringWithoutType;

function testStringTypeWithoutType() returns StringTypeWithoutType {
    StringTypeWithoutType t = "random text"; // Invalid value assignment.
    return t;
}

// -----------------------------------------------------------

const int invalidNil = ();

// -----------------------------------------------------------

const map<string> m1 = { "key": getValue() };
const map<string> m2 = { "key": sValue };
string sValue = "ballerina";

function getValue() returns string {
    return "ballerina";
}

// -----------------------------------------------------------

const map<string> m3 = { "m3k": "m3v" };

function updateConstantMapValue() {
    m3.m3k = "m3nv";
}

// -----------------------------------------------------------

type Foo record {
    string s;
    int i;
};

const Foo f = { s: "const string", i: 1 };

const json j = 1;
