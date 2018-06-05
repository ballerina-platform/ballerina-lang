import ballerina/io;
import ballerina/http;


@http:WebSocketServiceConfig {
    path: "/error/ws"
}
service<http:WebSocketService> errorService bind {port: 9090} {
    onOpen(endpoint ep) {
        io:println("connection open");
    }

    onText(endpoint ep, string text) {
        io:println(string `text received: {{text}}`);
        ep->pushText(text) but {
            error => io:println("error sending message")
        };
    }

    onError(endpoint ep, error err) {
        io:println(string `error occurred: {{err.message}}`);
    }

    onClose(endpoint ep, int statusCode, string reason) {
        io:println(string `Connection closed with {{statusCode}}, {{reason}}`);
    }
}