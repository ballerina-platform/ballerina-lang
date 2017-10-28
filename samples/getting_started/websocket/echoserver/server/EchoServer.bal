import ballerina.net.ws;

@ws:configuration {
    basePath:"/echo/ws",
    port:9090
}
service<ws> EchoServer {

    resource onOpen (ws:Connection conn) {
        println("New client : " + conn.getID());
    }

    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        string textReceived = frame.text;
        println("Text received: " + textReceived);

        if (textReceived == "closeMe") {
            conn.closeConnection(1001, "You told me to close");
        } else {
            conn.pushText(textReceived);
        }
    }

    resource onBinaryMessage (ws:Connection conn, ws:BinaryFrame frame) {
        blob data = frame.data;
        string text = data.toString("UTF-8");
        println("UTF-8 Decoded binary message: " + text);
        conn.pushBinary("You said " + text.toBlob("UTF-8"));
    }

    resource onClose (ws:Connection conn, ws:CloseFrame frame) {
        println("Client left the server: " + conn.getID());
        println("Status code: " + frame.statusCode + " reason " + frame.reason);
    }
}
