import ballerina/http as x;
import ballerina/sql as x;

function testFunc() {
    endpoint x:Client clientEndpoint1 {
       url: "https://postman-echo.com"
    };
}
