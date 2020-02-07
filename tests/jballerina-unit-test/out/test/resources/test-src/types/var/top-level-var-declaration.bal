
var intValue = 10;

var stringValue = "Ballerina";

var decimalValue = 100.0d;

var booleanValue = true;

var byteValue = <byte>2;

var floatValue = 2.0;

function testGetInt() returns int {
    return intValue;
}

function testGetString() returns string {
    return stringValue;
}

function testGetDecimal() returns decimal {
    return decimalValue;
}

function testGetBoolean() returns boolean {
    return booleanValue;
}

function testGetByte() returns byte {
    return byteValue;
}

function testGetFloat() returns float {
    return floatValue;
}

// ---------------------------------------------------------------------------------------------------------------------

var value = getValue();

function getValue() returns map<string> {
    map<string> m = { "k": "v" };
    return m;
}

function testFunctionInvocation() returns map<string> {
    return value;
}

// ---------------------------------------------------------------------------------------------------------------------

map<string> data = { "x": "y" };

var v = data;

function testVarAssign() returns map<string> {
    return v;
}
