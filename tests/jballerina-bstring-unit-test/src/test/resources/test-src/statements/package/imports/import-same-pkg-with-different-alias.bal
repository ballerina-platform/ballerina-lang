import ballerina/http;
import ballerina/http as x;
import ballerina/http as y;

function testFunc() {
    http:Client clientEndpoint1 = new("https://postman-echo.com");
    x:Client clientEndpoint2 = new("https://postman-echo.com");
    y:Client clientEndpoint3 = new("https://postman-echo.com");
}
