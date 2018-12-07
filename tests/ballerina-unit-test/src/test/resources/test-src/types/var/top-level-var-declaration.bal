
var intValue = 10;

var stringValue = "Ballerina";

var decimalValue = <decimal>100;

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

function testGetByte() returns byte|error {
    return check byteValue;
}

function testGetFloat() returns float {
    return floatValue;
}
