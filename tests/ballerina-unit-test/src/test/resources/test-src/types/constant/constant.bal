const string name = "Ballerina";

function testConstInReturn() returns string {
    return name;
}

// -----------------------------------------------------------

const int age = 10;

function testConstWithTypeInReturn() returns int {
    return age;
}

// -----------------------------------------------------------

function testConstAsParam() returns string {
    return testParam(name);
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

string sgv = name;

function testConstAssignmentToGlobalVariable() returns string {
    return sgv;
}

function testConstAssignmentToLocalVariable() returns string {
    string slv = name;
    return slv;
}

// -----------------------------------------------------------

int igv = age;

function testConstWithTypeAssignmentToGlobalVariable() returns int {
    return igv;
}

function testConstWithTypeAssignmentToLocalVariable() returns int {
    int ilv = age;
    return ilv;
}

// -----------------------------------------------------------

function testConstConcat() returns string {
    return name + " rocks";
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

function testEqualityWithConstWithoutType() returns boolean {
    return name == "Ballerina";
}

// -----------------------------------------------------------

function testEqualityWithConstWithType() returns boolean {
    return age == 10;
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

const boolean booleanWithType = false;

function testBooleanWithType() returns boolean {
    return booleanWithType;
}

// -----------------------------------------------------------

const int intWithType = 40;

function testIntWithType() returns int {
    return intWithType;
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

// -----------------------------------------------------------

const string stringWithType = "Ballerina is awesome";

function testStringWithType() returns string {
    return stringWithType;
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
