import ballerina.net.http;
import ballerina.net.http as x;
import ballerina.net.http as y;

function testFunc() {
    http:ClientConnector httpConnector1 = create http:ClientConnector("https://postman-echo.com", {});
    x:ClientConnector httpConnector2 = create x:ClientConnector("https://postman-echo.com", {});
    y:ClientConnector httpConnector3 = create y:ClientConnector("https://postman-echo.com", {});
}
