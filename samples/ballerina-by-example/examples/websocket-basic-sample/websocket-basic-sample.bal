import ballerina.net.ws;

@Description {value:"This example gives you the basic idea of WebSocket endpoint"}
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

    string ping = "ping";
    blob pingData = ping.toBlob("UTF-8");

    @Description {value:"This resource is responsible for handling user logic on handshake time. Note that the connection is not yet established while this code is running."}
    resource onHandshake(ws:HandshakeConnection conn) {
        println("\nNew client is going to connect");
        println("Connection ID: " + conn.connectionID);
        println("Is connection secure: " + conn.isSecure);

        println("pre upgrade headers -> ");
        printHeaders(conn.upgradeHeaders);
    }

    @Description {value:"This resource is triggered after a successful client connection."}
    resource onOpen(ws:Connection conn) {
        println("\nNew client connected");
        println("Connection ID: " + conn.getID());
        println("Negotiated Sub protocol: " + conn.getNegotiatedSubProtocol());
        println("Is connection open: " + conn.isOpen());
        println("Is connection secured: " + conn.isSecure());
        println("Connection header value: " + conn.getUpgradeHeader("Connection"));
        println("Upgrade headers -> " );
        printHeaders(conn.getUpgradeHeaders());
    }

    @Description {value:"This resource is triggered when a new text frame is received from a client"}
    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        println("\ntext message: " + frame.text + " & is final fragment: " + frame.isFinalFragment);
        string text = frame.text;

        if (text == "ping") {
            println("Pinging...");
            conn.ping(pingData);
        } else if (text == "closeMe") {
            conn.closeConnection(1001, "You asked me to close connection");
        } else {
            conn.pushText("You said: " + frame.text);
        }
    }

    @Description {value:"This resource is triggered when a new binary frame is received from a client"}
    resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {
        println("\nNew binary message received");
        blob b = frame.data;
        println("UTF-8 decoded binary message: " + b.toString("UTF-8"));
        conn.pushBinary(b);
    }

    @Description {value:"This resource is triggered when a ping message is received from the client"}
    resource onPing(ws:Connection conn, ws:PingFrame frame) {
        conn.pong(frame.data);
    }

    @Description {value:"This resource is triggered when a pong message is received"}
    resource onPong(ws:Connection conn, ws:PongFrame frame) {
        println("Pong received");
    }

    @Description {value:"This resource is triggered when a particular client reaches it's idle timeout defined in the ws:configuration annotation"}
    resource onIdleTimeout(ws:Connection conn) {
        // This resource will be triggered after 180 seconds if there is no activity in a given channel.
        println("\nReached idle timeout");
        println("Closing connection " + conn.getID());
        conn.closeConnection(1001, "Connection timeout");
    }

    @Description {value:"This resource is triggered when a client connection is closed from the client side"}
    resource onClose(ws:Connection conn, ws:CloseFrame closeFrame) {
        println("\nClient left with status code " + closeFrame.statusCode + " because " + closeFrame.reason);
    }
}

function printHeaders(map headers) {
    string [] headerKeys = headers.keys();
    int len = lengthof headerKeys;
    int i = 0;
    while (i < len) {
        string key = headerKeys[i];
        var value, _ = (string) headers[key];
        println(key + ": " + value);
        i = i + 1;
    }
}