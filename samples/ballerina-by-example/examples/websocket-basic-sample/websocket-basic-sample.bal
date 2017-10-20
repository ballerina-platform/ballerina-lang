import ballerina.lang.system;
import ballerina.lang.maps;
import ballerina.lang.blobs;
import ballerina.net.ws;
import ballerina.doc;


@doc:Description {value:"This example gives you the basic idea of WebSocket endpoint"}
@ws:configuration {
    basePath: "/basic/ws",
    subProtocols: ["xml", "json"],
    idleTimeoutInSeconds: 120,
    host: "0.0.0.0",
    port: 9090,
    wssPort: 9095,
    keyStoreFile: "${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePassword: "wso2carbon",
    certPassword: "wso2carbon"
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
        system:println("Connection ID: " + conn.getID());
        system:println("Negotiated Sub protocol: " + conn.getNegotiatedSubProtocol());
        system:println("Is connection open: " + conn.isOpen());
        system:println("Is connection secured: " + conn.isSecure());
        system:println("Connection header value: " + conn.getUpgradeHeader("Connection"));
        system:println("Upgrade headers -> " );
        printHeaders(conn.getUpgradeHeaders());
    }

    @doc:Description {value:"This resource is triggered when a new text frame is received from a client"}
    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        system:println("\ntext message: " + frame.text + " & is final fragment: " + frame.isFinalFragment);
        string text = frame.text;
        if (text == "closeMe") {
            conn.closeConnection(1001, "You asked me to close connection");
        } else {
            conn.pushText("You said: " + frame.text);
        }
    }

    @doc:Description {value:"This resource is triggered when a new binary frame is received from a client"}
    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        system:println("\nNew binary message received");
        blob b = frame.data;
        system:println("UTF-8 decoded binary message: " + blobs:toString(b, "UTF-8"));
        conn.pushBinary(b);
    }

    @doc:Description {value:"This resource is triggered when a particular client reaches it's idle timeout defined in the ws:configuration annotation"}
    resource onIdleTimeout(ws:Connection conn) {
        // This resource will be triggered after 180 seconds if there is no activity in a given channel.
        system:println("\nReached idle timeout");
        system:println("Closing connection " + conn.getID());
        conn.closeConnection(1001, "Connection timeout");
    }

    @doc:Description {value:"This resource is triggered when a client connection is closed from the client side"}
    resource onClose(ws:Connection conn, ws:CloseFrame closeFrame) {
        system:println("\nClient left with status code " + closeFrame.statusCode + " because " + closeFrame.reason);
    }
}

function printHeaders(map headers) {
    string [] headerKeys = maps:keys(headers);
    int len = lengthof headerKeys;
    int i = 0;
    while (i < len) {
        string key = headerKeys[i];
        var value, _ = (string) headers[key];
        system:println(key + ": " + value);
        i = i + 1;
    }
}