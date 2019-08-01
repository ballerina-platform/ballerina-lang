import ballerina/http;

http:Client httpClient = new ("http://localhost:8080");

function testSignatureHelp () {
    httpClient->get()
}