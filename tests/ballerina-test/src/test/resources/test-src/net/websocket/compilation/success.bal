import ballerina/http;
import ballerina/io;

endpoint http:WebSocketListener echoEP {
    host:"0.0.0.0",
    port:9090
};

@http:WebSocketServiceConfig {
    path:"/echo"
}
service<http:WebSocketService> echo bind echoEP {
    onOpen(endpoint conn) {
    }

    onText(endpoint conn, string text) {
    }

    onBinary(endpoint conn, blob text, boolean more) {

    }

    onClose(endpoint conn, int val, string text) {

    }

    onIdleTimeout(endpoint conn) {

    }
    onPing(endpoint conn, blob so) {

    }
    onPong(endpoint conn, blob yes) {

    }
}