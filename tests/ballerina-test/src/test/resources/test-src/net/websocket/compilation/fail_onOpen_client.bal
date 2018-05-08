import ballerina/http;
import ballerina/io;

endpoint http:WebSocketClient wsClientEp {
    url:"wss://echo.websocket.org",
    callbackService:echo
};

service<http:WebSocketClientService> echo {
    onOpen(endpoint conn) {
    }

    onIdleTimeout(endpoint conn) {

    }
}