import ballerina/http;

int globalField1 = 12;
int globalField2 = 12;

function toEmployee() returns http:Client => http:
