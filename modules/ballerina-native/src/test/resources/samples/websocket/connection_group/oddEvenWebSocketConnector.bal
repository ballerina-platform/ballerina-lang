import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.net.ws;
import ballerina.net.http;

@http:BasePath {value:"/chat"}
@ws:WebSocketUpgradePath {value:"/ws"}
service oddEvenWebSocketConnector {

    string evenConnectionGroupName = "evenGroup";
    string oddConnectionGroupName = "oddGroup";

    @ws:OnOpen {}
    resource onOpen(message m) {
        system:println("New client connected to the server.");
        string group = messages:getHeader(m, "group");
        if (group == "even") {
            ws:addConnectionToGroup(evenConnectionGroupName);
        }
        if (group == "odd") {
            ws:addConnectionToGroup(oddConnectionGroupName);
        }
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        ws:pushTextToGroup(oddConnectionGroupName, oddConnectionGroupName + ": " + messages:getStringPayload(m));
        ws:pushTextToGroup(evenConnectionGroupName, evenConnectionGroupName+ ": " + messages:getStringPayload(m));
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
        ws:broadcastText("client left the server.");
    }

}
