import ballerina/lang.system;
import ballerina/lang.strings;
import ballerina/lang.blobs;
import ballerina/net.ws;

@ws:configuration {
    basePath:"/echo/ws",
    port:9090
}
service<ws> EchoServer {

    resource onOpen (ws:Connection conn) {
        system:println("New client : " + ws:getID(conn));
    }

    resource onTextMessage (ws:Connection conn, ws:TextFrame frame) {
        string textReceived = frame.text;
        system:println("Text received: " + textReceived);

        if (textReceived == "closeMe") {
            ws:closeConnection(conn, 1001, "You told me to close");
        } else {
            ws:pushText(conn, textReceived);
        }
    }

    resource onBinaryMessage (ws:Connection conn, ws:BinaryFrame frame) {
        blob data = frame.data;
        string text = blobs:toString(data, "UTF-8");
        system:println("UTF-8 Decoded binary message: " + text);
        ws:pushBinary(conn, strings:toBlob("You said " + text, "UTF-8"));
    }

    resource onClose (ws:Connection conn, ws:CloseFrame frame) {
        system:println("Client left the server: " + ws:getID(conn));
        system:println("Status code: " + frame.statusCode + " reason " + frame.reason);
    }
}
