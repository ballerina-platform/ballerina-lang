import ballerina.net.http;
import ballerina.net.http as x;
import ballerina.net.http as y;

function testFunc() {
    http:ClientConnector httpConnector1;
    x:ClientConnector httpConnector2;
    y:ClientConnector httpConnector3;
}
