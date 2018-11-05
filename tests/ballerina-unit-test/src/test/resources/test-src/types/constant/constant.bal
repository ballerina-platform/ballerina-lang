const nameWithoutType = "Ballerina";
const string nameWithType = "Ballerina";

function testConstWithTypeInReturn() returns string {
    return nameWithType;
}

// -----------------------------------------------------------

function testConstWithoutTypeInReturn() returns string {
    return nameWithType;
}

// -----------------------------------------------------------

function testConstWithTypeAsParam() returns string {
    return testParam(nameWithType);
}

function testConstWithoutTypeAsParam() returns string {
    return testParam(nameWithoutType);
}

function testParam(string s) returns string {
    return s;
}

// -----------------------------------------------------------

type Data record {
    string firstName;
};

function testConstInRecord() returns string {
    Data d = { firstName: "Ballerina" };
    return d.firstName;
}

// -----------------------------------------------------------

string sgvWithType = nameWithType;

function testConstWithTypeAssignmentToGlobalVariable() returns string {
    return sgvWithType;
}

function testConstWithTypeAssignmentToLocalVariable() returns string {
    string slv = nameWithType;
    return slv;
}

// -----------------------------------------------------------

string sgvWithoutType = nameWithoutType;

function testConstWithoutTypeAssignmentToGlobalVariable() returns string {
    return sgvWithoutType;
}

function testConstWithoutTypeAssignmentToLocalVariable() returns string {
    string slv = nameWithoutType;
    return slv;
}

// -----------------------------------------------------------

function testConstWithTypeConcat() returns string {
    return nameWithType + " rocks";
}

// -----------------------------------------------------------

function testConstWithoutTypeConcat() returns string {
    return nameWithoutType + " rocks";
}

// -----------------------------------------------------------

type ACTION "GET"|"POST";

const ACTION GET = "GET";
const ACTION POST = "POST";

function testTypeConstants() returns ACTION {
    return GET;
}

const ACTION constActionWithType = "GET";

function testConstWithTypeAssignmentToType() returns ACTION {
    ACTION action = constActionWithType;
    return action;
}

const constActionWithoutType = "GET";

function testConstWithoutTypeAssignmentToType() returns ACTION {
    ACTION action = constActionWithoutType;
    return action;
}

function testConstAndTypeComparison() returns boolean {
    return "GET" == GET;
}

function testTypeConstAsParam() returns boolean {
    return typeConstAsParam(GET);
}

function typeConstAsParam(ACTION a) returns boolean {
    return "GET" == a;
}

// -----------------------------------------------------------

function testEqualityWithConstWithType() returns boolean {
    return nameWithType == "Ballerina";
}

// -----------------------------------------------------------

const boolean conditionWithType = true;

function testConstWithTypeInCondition() returns boolean {
    if (conditionWithType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

const conditionWithoutType = true;

function testConstWithoutTypeInCondition() returns boolean {
    if (conditionWithoutType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

const boolean booleanWithType = false;

function testBooleanWithType() returns boolean {
    return booleanWithType;
}

const booleanWithoutType = true;

function testBooleanWithoutType() returns boolean {
    return booleanWithoutType;
}

// -----------------------------------------------------------

const int intWithType = 40;

function testIntWithType() returns int {
    return intWithType;
}

const intWithoutType = 20;

function testIntWithoutType() returns int {
    return intWithoutType;
}

// -----------------------------------------------------------

const byte byteWithType = 240;

function testByteWithType() returns byte {
    return byteWithType;
}

// -----------------------------------------------------------

const float floatWithType = 4.0;

function testFloatWithType() returns float {
    return floatWithType;
}

const floatWithoutType = 2.0;

function testFloatWithoutType() returns float {
    return floatWithoutType;
}

// -----------------------------------------------------------

const string stringWithType = "Ballerina is awesome";

function testStringWithType() returns string {
    return stringWithType;
}

const stringWithoutType = "Ballerina rocks";

function testStringWithoutType() returns string {
    return stringWithoutType;
}

// -----------------------------------------------------------

const string key = "key";
const string value = "value";

function testConstInMapKey() returns string {
    map<string> m = { key: "value" };
    return m.key;
}

function testConstInMapValue() returns string {
    map<string> m = { "key": value };
    return m.key;
}

function testConstInJsonKey() returns json {
    json j = { key: "value" };
    return j.key;
}

function testConstInJsonValue() returns json {
    json j = { "key": value };
    return j.key;
}

// -----------------------------------------------------------

function testBooleanConstInUnion() returns any {
    boolean|int v = booleanWithType;
    return v;
}

function testIntConstInUnion() returns any {
    int|boolean v = intWithType;
    return v;
}

function testByteConstInUnion() returns any {
    byte|boolean v = byteWithType;
    return v;
}

function testFloatConstInUnion() returns any {
    float|boolean v = floatWithType;
    return v;
}

function testStringConstInUnion() returns any {
    string|boolean v = stringWithType;
    return v;
}

// -----------------------------------------------------------

function testBooleanConstInTuple() returns boolean {
    (boolean, int) v = (booleanWithType, 1);
    return v[0];
}

function testIntConstInTuple() returns int {
    (int, boolean) v = (intWithType, true);
    return v[0];
}

function testByteConstInTuple() returns byte {
    (byte, boolean) v = (byteWithType, true);
    return v[0];
}

function testFloatConstInTuple() returns float {
    (float, boolean) v = (floatWithType, true);
    return v[0];
}

function testStringConstInTuple() returns string {
    (string, boolean) v = (stringWithType, true);
    return v[0];
}

// -----------------------------------------------------------

const D = "D";

const E = "E";

const F = "F";

type G D|E|F;

type H D|E;

const H h = "D";

function testProperSubset() returns G {
    G g = h;
    return g;
}

// -----------------------------------------------------------

// Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/11183.
//type M record { string f; }|Z;
//
//M m1 = { f: "foo" };
//
//M m2 = "V";
//
//M m3 = "W";
//
//M m4 = "X";

type Y X;

type Z "V"|W|X;

Y y = "X";

Z z1 = "V";

Z z2 = "W";

Z z3 = "X";


const string W = "W";

const string X = "X";
