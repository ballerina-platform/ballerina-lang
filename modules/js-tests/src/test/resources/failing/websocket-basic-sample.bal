import ballerina.lang.system;
import ballerina.doc;
import ballerina.lang.maps;
import ballerina.lang.blobs;
import ballerina.net.ws;


@doc:Description {value:"This example gives you the basic idea of WebSocket endpoint"}
@ws:configuration {
    basePath: "/basic/ws",
    subProtocols: ["xml", "json"],
    idleTimeoutInSeconds: 120,
    host: "0.0.0.0",
    port: 9090
}
service<ws> SimpleSecureServer {

    @doc:Description {value:"This resource is responsible for handling user logic on handshake time. Note that the connection is not yet established while this code is running."}
    resource onHandshake(ws:HandshakeConnection conn) {
        system:println("\nNew client is going to connect");
        system:println("Connection ID: " + conn.connectionID);
        system:println("Is connection secure: " + conn.isSecure);

        system:println("pre upgrade headers -> ");
        printHeaders(conn.upgradeHeaders);
    }

    @doc:Description {value:"This resource is triggered after a successful client connection."}
    resource onOpen(ws:Connection conn) {
        system:println("\nNew client connected");
        system:println("Connection ID: " + ws:getID(conn));
        system:println("Negotiated Sub protocol: " + ws:getNegotiatedSubProtocol(conn));
        system:println("Is connection open: " + ws:isOpen(conn));
        system:println("Is connection secured: " + ws:isSecure(conn));
        system:println("Connection header value: " + ws:getUpgradeHeader(conn, "Connection"));
        system:println("Upgrade headers -> " );
        printHeaders(ws:getUpgradeHeaders(conn));
    }

    @doc:Description {value:"This resource is triggered when a new text frame is received from a client"}
    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        system:println("\ntext message: " + frame.text + " & is final fragment: " + frame.isFinalFragment);
        string text = frame.text;
        if (text == "closeMe") {
            ws:closeConnection(conn, 1001, "You asked me to close connection");
        } else {
            ws:pushText(conn, "You said: " + frame.text);
        }
    }

    @doc:Description {value:"This resource is triggered when a new binary frame is received from a client"}
    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        system:println("\nNew binary message received");
        blob b = frame.data;
        system:println("UTF-8 decoded binary message: " + blobs:toString(b, "UTF-8"));
        ws:pushBinary(conn, b);
    }

    @doc:Description {value:"This resource is triggered when a particular client reaches it's idle timeout defined in the ws:configuration annotation"}
    resource onIdleTimeout(ws:Connection conn) {
        // This resource will be triggered after 180 seconds if there is no activity in a given channel.
        system:println("\nReached idle timeout");
        system:println("Closing connection " + ws:getID(conn));
        ws:closeConnection(conn, 1001, "Connection timeout");
    }

    @doc:Description {value:"This resource is triggered when a client connection is closed from the client side"}
    resource onClose(ws:Connection conn, ws:CloseFrame closeFrame) {
        system:println("\nClient left with status code " + closeFrame.statusCode + " because " + closeFrame.reason);
    }
}

function printHeaders(map headers) {
    string [] headerKeys = maps:keys(headers);
    int len = headerKeys.length;
    int i = 0;
    while (i < len) {
        var key, e = (string) headerKeys[i];
        var value, e = (string) headers[key];
        system:println(key + ": " + value);
        i = i + 1;
    }
}