import ballerina/http;

int globalField1 = 12;
int globalField2 = 12;

function testFunction() returns http:Client => globalField1.toHexString().
