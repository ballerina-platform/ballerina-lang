import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.net.ws;
import ballerina.net.http;

@http:BasePath {value:"/chat-group"}
@ws:WebSocketUpgradePath {value:"/ws"}
service oddEvenWebSocketConnector {

    @ws:OnOpen {}
    resource onOpen(message m) {
        string group = messages:getHeader(m, "group");
        if (group == "even") {
            ws:addConnectionToGroup("evenGroup");
        }
        if (group == "odd") {
            ws:addConnectionToGroup("oddGroup");
        }
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        string text = messages:getStringPayload(m);
        if ("removeOddGroup" == text) {
            ws:removeConnectionGroup("oddGroup");
        } else if ("removeEvenGroup" == text) {
            ws:removeConnectionGroup("evenGroup");
        } else if ("removeOddConnection" == text) {
            ws:removeConnectionFromGroup("oddGroup");
        } else if ("removeEvenConnection" == text) {
            ws:removeConnectionFromGroup("evenGroup");
        } else {
            ws:pushTextToGroup("oddGroup", "oddGroup: " + messages:getStringPayload(m));
            ws:pushTextToGroup("evenGroup", "evenGroup: " + messages:getStringPayload(m));
        }
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
        ws:broadcastText("client left the server.");
    }
}
