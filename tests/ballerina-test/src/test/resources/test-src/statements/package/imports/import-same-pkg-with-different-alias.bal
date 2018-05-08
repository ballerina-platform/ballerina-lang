import ballerina/http;
import ballerina/http as x;
import ballerina/http as y;

function testFunc() {
    endpoint http:Client clientEndpoint1 {
       url: "https://postman-echo.com"
    };
    endpoint x:Client clientEndpoint2 {
       url: "https://postman-echo.com"
    };
    endpoint y:Client clientEndpoint3 {
       url: "https://postman-echo.com"
    };
}
