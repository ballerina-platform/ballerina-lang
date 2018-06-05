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
    onOpen(endpoint caller) {
    }

    onText(endpoint caller, string text) {
    }

    onBinary(endpoint caller, blob text, boolean final) {
    }

    onClose(endpoint caller, int val, string text) {
    }

    onIdleTimeout(endpoint caller) {
    }

    onPing(endpoint caller, blob so) {
    }

    onPong(endpoint caller, blob yes) {
    }

    onError(endpoint caller, error err) {
    }
}
