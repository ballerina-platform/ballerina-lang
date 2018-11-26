import ballerina/http;
import ballerina/http as x;
import ballerina/http as y;

function testFunc() {
    http:Client clientEndpoint1 = new({
       url: "https://postman-echo.com"
    });
    x:Client clientEndpoint2 = new({
       url: "https://postman-echo.com"
    });
    y:Client clientEndpoint3 = new({
       url: "https://postman-echo.com"
    });
}
