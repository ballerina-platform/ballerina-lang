import ballerina/http;
import ballerina/log;

// The annotation which is used to configure a WebSocket service.
@http:WebSocketServiceConfig {
    path: "/retry/ws"
}

// Define the backend service with port 9095, which is called by the client.
service server on new http:Listener(9095) {

    // This resource gets invoked when a new client connects.
    // Since messages to the server are not read by the service
    // until the execution of the `onOpen` resource finishes,
    // operations which should happen before reading messages should be
    // done in the `onOpen` resource.
    resource function onOpen(http:WebSocketCaller caller) {
        log:printInfo("WebSocket client connected wih the server. " +
        "The Connection ID: " + caller.getConnectionId());
    }

    // This resource gets invoked when a server is receiving
    // a text message from the client.
    resource function onText(http:WebSocketCaller caller, string text,
                                boolean finalFrame) {
        var err = caller->pushText(text, finalFrame);
        if (err is http:WebSocketError) {
            log:printError("Error occurred when sending text message", err);
        }
    }
}
