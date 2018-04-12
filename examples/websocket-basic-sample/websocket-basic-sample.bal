import ballerina/io;
import ballerina/log;
import ballerina/http;

@Description {value:"This example gives you the basic idea of WebSocket endpoint."}
endpoint http:Listener ep {
    host:"0.0.0.0",
    port:9090
};

@http:WebSocketServiceConfig {
    path:"/basic/ws",
    subProtocols:["xml", "json"],
    idleTimeoutInSeconds:120
}
service<http:WebSocketService> SimpleSecureServer bind ep {

    string ping = "ping";
    blob pingData = ping.toBlob("UTF-8");

    @Description {value:"This resource is triggered after a successful client connection."}
    onOpen (endpoint conn) {
        io:println("\nNew client connected");
        io:println("Connection ID: " + conn.id);
        io:println("Negotiated Sub protocol: " + conn.negotiatedSubProtocol);
        io:println("Is connection open: " + conn.isOpen);
        io:println("Is connection secured: " + conn.isSecure);
        io:println("Upgrade headers -> ");
    }

    @Description {value:"This resource is triggered when a new text frame is received from a client."}
    onText (endpoint conn, string text, boolean more) {
        io:println("\ntext message: " + text + " & more fragments: " + more);

        if (text == "ping") {
            io:println("Pinging...");
            conn -> ping(pingData) but {error e => log:printErrorCause("Error sending ping", e)};
        } else if (text == "closeMe") {
            conn -> close(1001, "You asked me to close the connection")
                         but {error e => log:printErrorCause("Error occurred when closing the connection", e)};
        } else {
            conn -> pushText("You said: " + text) but {error e => log:printErrorCause("Error occurred when sending
            text", e)};
        }
    }

    @Description {value:"This resource is triggered when a new binary frame is received from a client."}
    onBinary (endpoint conn, blob b) {
        io:println("\nNew binary message received");
        io:println("UTF-8 decoded binary message: " + b.toString("UTF-8"));
        conn -> pushBinary(b) but {error e => log:printErrorCause("Error occurred when sending binary", e)};
    }

    @Description {value:"This resource is triggered when a ping message is received from the client. If this resource is
     not implemented, a pong message is automatically sent to the connected endpoint when a ping is received."}
    onPing (endpoint conn, blob data) {
        conn -> pong(data) but {error e => log:printErrorCause("Error occurred when closing the connection", e)};
    }

    @Description {value:"This resource is triggered when a pong message is received"}
    onPong (endpoint conn, blob data) {
        io:println("Pong received");
    }

    @Description {value:"This resource is triggered when a particular client reaches the idle timeout that is defined in
     the `http:WebSocketServiceConfig` annotation."}
    onIdleTimeout (endpoint conn) {
        // This resource will be triggered after 180 seconds if there is no activity in a given channel.
        io:println("\nReached idle timeout");
        io:println("Closing connection " + conn.id);
        conn -> close(1001, "Connection timeout") but {error e => log:printErrorCause("Error occured when closing the connection", e)};
    }

    @Description {value:"This resource is triggered when a client connection is closed from the client side."}
    onClose (endpoint conn, int statusCode, string reason) {
        io:println("\nClient left with status code " + statusCode + " because " + reason);
    }
}
