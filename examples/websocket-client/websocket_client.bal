import ballerina/http;
import ballerina/io;

public function main() {
    // Creates a new WebSocket client with the backend URL and assigns a callback service.
    http:WebSocketClient wsClientEp = new ("ws://echo.websocket.org",
                            config = {callbackService: ClientService});
    // Pushes a text message to the server.
    var err = wsClientEp->pushText("Hello World!");
    if (err is error) {
        // Prints the error.
        io:println(err);
    }
}
// The client callback service, which handles backend responses.
service ClientService = @http:WebSocketServiceConfig {} service {

    // This resource is triggered when a new text frame is received from the remote backend.
    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) {
        io:println(text);
    }
    // This is triggered if an error occurs.
    resource function onError(http:WebSocketClient conn, error err) {
        io:println(err);
    }
};
