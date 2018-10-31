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

const booleanWithoutType = true;

function testConstWithoutTypeInCondition() returns boolean {
    if (booleanWithoutType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

const boolean booleanWithType = true;

function testConstWithTypeInCondition() returns boolean {
    if (booleanWithType) {
        return true;
    }
    return false;
}
