import ballerina.lang.system;
import ballerina.lang.maps;
import ballerina.lang.blobs;
import ballerina.net.ws;

@ws:configuration {
    basePath: "/functions/ws",
    subProtocols: ["xml", "json"],
    idleTimeoutInSeconds: 120,
    host: "0.0.0.0",
    port: 9090
}
service<ws> SimpleSecureServer {

    resource onHandshake(ws:HandshakeConnection conn) {
        system:println("\nConnection ID: " + conn.connectionID);
        system:println("Is connection secure: " + conn.isSecure);

        system:println("pre upgrade headers -> ");
        printHeaders(conn.upgradeHeaders);
    }

    resource onOpen(ws:Connection conn) {
        system:println("\nNew client connected: " + ws:getID(conn));
        system:println("Sub protocol: " + ws:getNegotiatedSubProtocol(conn));
        system:println("Is connection open: " + ws:isOpen(conn));
        system:println("Is connection secured: " + ws:isSecure(conn));
        system:println("Connection header: " + ws:getUpgradeHeader(conn, "Connection"));
        system:println("Upgrade headers -> " );
        printHeaders(ws:getUpgradeHeaders(conn));
    }


    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        system:println("\ntext message: " + frame.text + " & is final fragment: " + frame.isFinalFragment);
        string text = frame.text;
        if (text == "closeMe") {
            ws:closeConnection(conn, 1001, "You asked me to close connection");
        } else {
            ws:pushText(conn, "You said: " + frame.text);
        }
    }

    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        blob b = frame.data;
        system:println(blobs:toString(b, "UTF-8"));
        ws:pushBinary(conn, b);
    }

    resource onIdleTimeout(ws:Connection conn) {
        // This resource will be triggered after 180 seconds if there is no activity in a given channel.
        system:println("\nReached idle timeout");
        system:println("Closing connection " + ws:getID(conn));
        ws:closeConnection(conn, 1001, "Connection timeout");
    }

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