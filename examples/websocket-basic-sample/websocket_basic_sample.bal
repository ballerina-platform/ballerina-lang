import ballerina/http;
import ballerina/io;
import ballerina/log;

string ping = "ping";
byte[] pingData = ping.toBytes();

@http:WebSocketServiceConfig {
    path: "/basic/ws",
    subProtocols: ["xml", "json"],
    idleTimeoutInSeconds: 120
}
service basic on new http:Listener(9090) {

    // This `resource` is triggered after a successful client connection.
    resource function onOpen(http:WebSocketCaller caller) {
        io:println("\nNew client connected");
        io:println("Connection ID: " + caller.getConnectionId());
        io:println("Negotiated Sub protocol: " + caller.getNegotiatedSubProtocol().toString());
        io:println("Is connection open: " + caller.isOpen().toString());
        io:println("Is connection secured: " + caller.isSecure().toString());
    }

    // This `resource` is triggered when a new text frame is received from a client.
    resource function onText(http:WebSocketCaller caller, string text,
                                boolean finalFrame) {
        io:println("\ntext message: " + text + " & final fragment: "
                                                        + finalFrame.toString());
        if (text == "ping") {
            io:println("Pinging...");
            var err = caller->ping(pingData);
            if (err is http:WebSocketError) {
                log:printError("Error sending ping", err);
            }
        } else if (text == "closeMe") {
            error? result = caller->close(statusCode = 1001,
                            reason = "You asked me to close the connection",
                            timeoutInSeconds = 0);
            if (result is http:WebSocketError) {
                log:printError("Error occurred when closing connection", result);
            }
        } else {
            var err = caller->pushText("You said: " + text);
            if (err is http:WebSocketError) {
                log:printError("Error occurred when sending text", err);
            }
        }
    }

    // This `resource` is triggered when a new binary frame is received from a client.
    resource function onBinary(http:WebSocketCaller caller, byte[] b) {
        io:println("\nNew binary message received");
        io:print("UTF-8 decoded binary message: ");
        io:println(b);
        var err = caller->pushBinary(b);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when sending binary", err);
        }
    }

    // This `resource` is triggered when a ping message is received from the client. If this resource is not implemented,
    // a pong message is automatically sent to the connected `http:WebSocketCaller` when a ping is received.
    resource function onPing(http:WebSocketCaller caller, byte[] data) {
        var err = caller->pong(data);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", err);
        }
    }

    // This resource is triggered when a pong message is received.
    resource function onPong(http:WebSocketCaller caller, byte[] data) {
        io:println("Pong received");
    }

    // This resource is triggered when a particular client reaches the idle timeout that is defined in the
    // `http:WebSocketServiceConfig` annotation.
    resource function onIdleTimeout(http:WebSocketCaller caller) {
        io:println("\nReached idle timeout");
        io:println("Closing connection " + caller.getConnectionId());
        var err = caller->close(statusCode = 1001, reason =
                                    "Connection timeout");
        if (err is http:WebSocketError) {
            log:printError("Error occurred when closing the connection", err);
        }
    }

    // This resource is triggered when an error occurred in the connection or the transport.
    // This resource is always followed by a connection closure with an appropriate WebSocket close frame
    // and this is used only to indicate the error to the user and take post decisions if needed.
    resource function onError(http:WebSocketCaller caller, error err) {
        log:printError("Error occurred ", err);
    }

    // This resource is triggered when a client connection is closed from the client side.
    resource function onClose(http:WebSocketCaller caller, int statusCode,
                                string reason) {
        io:println(string `Client left with ${statusCode} because ${reason}`);
    }
}
