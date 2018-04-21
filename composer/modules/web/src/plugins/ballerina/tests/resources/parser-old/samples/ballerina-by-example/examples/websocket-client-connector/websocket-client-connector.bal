import ballerina/lang.system;
import ballerina/lang.messages;
import ballerina/doc;
import ballerina/http;
import ballerina/net.ws;

@http:configuration {basePath:"/client-connector"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> serverConnector {

    ws:ClientConnector con = create ws:ClientConnector("wss://echo.websocket.org", "clientService");

    @doc:Description {value:"This is where the messages from WebSocket clients are received."}
    @ws:OnTextMessage {}
    resource onText(message m) {
        string textReceived = messages:getStringPayload(m);

        if ("closeMe" == textReceived) {
            system:println("Removing the connection...");
            ws:closeConnection();
        } else {
            system:println("Client connector sending message: " +
                           messages:getStringPayload(m));
            con.pushText(messages:getStringPayload(m));
        }
    }
}


@doc:Description {value:"This is the client service for WebSocket client connector con mentioned above."}
@ws:ClientService {}
service<ws> clientService {

    @ws:OnTextMessage {}
    resource onText(message m) {
        system:println("client service: " + messages:getStringPayload(m));
        // Sends message back to the client who sent the message from the server connector.
        ws:pushText("client service: " + messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("Closed client connection");
    }
}
