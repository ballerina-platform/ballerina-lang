import ballerina/http as x;
import ballerina/sql as x;

function testFunc() {
    x:Client clientEndpoint1 = new({
       url: "https://postman-echo.com"
    });
}
