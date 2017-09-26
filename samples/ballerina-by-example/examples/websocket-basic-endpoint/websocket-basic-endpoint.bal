import ballerina.lang.system;
import ballerina.net.ws;
import ballerina.doc;

@doc:Description {value:"This example gives you the basic idea of WebSocket endpoint"}
@ws:configuration {
    basePath:"/basic/ws",
    port:9090,
    idleTimeoutInSeconds:10
}
service<ws> basicEndpoint {

    @doc:Description {value:"This resource is responsible for handling user logic on handshake time. Note that the connection is not yet established while this code is running."}
    resource onHandshake(ws:HandshakeConnection conn) {
        system:println("New client is going to connect");
    }

    @doc:Description {value:"This resource is triggered after a successful client connection."}
    resource onOpen(ws:Connection conn) {
        system:println("New client connected");
    }

    @doc:Description {value:"This resource is triggered when a new text frame is received from a client"}
    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
        system:println("New text message received: " + frame.text);
    }

    @doc:Description {value:"This resource is triggered when a new binary frame is received from a client"}
    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        blob b = frame.data;
        system:println("New binary message received");
    }

    @doc:Description {value:"This resource is triggered when a particular client reaches it's idle timeout defined in the ws:configuration annotation"}
    resource onIdleTimeout(ws:Connection conn) {
        system:println("Connection " + ws:getID(conn) + " achieved it's idle timeout");
    }

    @doc:Description {value:"This resource is triggered when a client connection is closed from the client side"}
    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
        system:println("Connection " + ws:getID(conn) + " is closed");
        system:println("Status code: " + frame.statusCode);
        system:println("Close reason: " + frame.reason);
    }
}