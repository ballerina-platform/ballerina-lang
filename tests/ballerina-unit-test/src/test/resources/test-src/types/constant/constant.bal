const name = "Ballerina";

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

const conditionWithoutType = true;

function testConstWithoutTypeInCondition() returns boolean {
    if (conditionWithoutType) {
        return true;
    }
    return false;
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

const booleanWithoutType = true;

function testBooleanWithoutType() returns boolean {
    return booleanWithoutType;
}

const boolean booleanWithType = false;

function testBooleanWithType() returns boolean {
    return booleanWithType;
}

// -----------------------------------------------------------

const intWithoutType = 20;

function testIntWithoutType() returns int {
    return intWithoutType;
}

const int intWithType = 40;

function testIntWithType() returns int {
    return intWithType;
}

// -----------------------------------------------------------

// Note - We need to explicitly define bytes with the type node. Otherwise it will be considered as an int. So the
// below example is invalid.
//
// const byteWithoutType = 120;
//
// function testByteWithoutType() returns byte {
//     return byteWithoutType;
// }

const byte byteWithType = 240;

function testByteWithType() returns byte {
    return byteWithType;
}

// -----------------------------------------------------------

const floatWithoutType = 2.0;

function testFloatWithoutType() returns float {
    return floatWithoutType;
}

const float floatWithType = 4.0;

function testFloatWithType() returns float {
    return floatWithType;
}

// -----------------------------------------------------------

const stringWithoutType = "Ballerina rocks";

function testStringWithoutType() returns string {
    return stringWithoutType;
}

const string stringWithType = "Ballerina is awesome";

function testStringWithType() returns string {
    return stringWithType;
}
