import ballerina/http;

function testFunction1() {
    http:Response res = new;
    string testString = "Hello Ballerina!";
    string val = res.entity.
}

function getResponseObject() returns http:Response {
    http:Response res = new;
    return res;
}