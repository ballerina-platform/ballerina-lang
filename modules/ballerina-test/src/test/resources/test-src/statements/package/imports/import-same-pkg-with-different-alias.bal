import ballerina.net.http;
import ballerina.net.http as x;
import ballerina.net.http as y;

function testFunc() {
    http:HttpClient httpConnector1 = create http:HttpClient("https://postman-echo.com", {});
    x:HttpClient httpConnector2 = create x:HttpClient("https://postman-echo.com", {});
    y:HttpClient httpConnector3 = create y:HttpClient("https://postman-echo.com", {});
}
