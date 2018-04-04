package connectionGroupSample.sample;

import ballerina/lang.system;
import ballerina/lang.messages;
import ballerina/net.ws;
import ballerina/http;

@http:configuration {basePath:"/group"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> oddEvenWebSocketService {

    string evenConnectionGroupName = "evenGroup";
    string oddConnectionGroupName = "oddGroup";
    int i = 0;

    @ws:OnOpen {}
    resource onOpen(message m) {
        if (i % 2 == 0) {
            ws:addConnectionToGroup(evenConnectionGroupName);
        } else {
            ws:addConnectionToGroup(oddConnectionGroupName);
        }
        system:println("New client connected to the server.");
        i = i + 1;
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        if (messages:getStringPayload(m) == "removeMe") {
            ws:removeConnectionFromGroup(oddConnectionGroupName);
            ws:removeConnectionFromGroup(evenConnectionGroupName);
        } else {
            ws:pushTextToGroup(oddConnectionGroupName, oddConnectionGroupName + ": " + messages:getStringPayload(m));
            ws:pushTextToGroup(evenConnectionGroupName, evenConnectionGroupName+ ": " + messages:getStringPayload(m));
        }
    }

    @ws:OnClose {}
    resource onClose(message m) {
        system:println("client left the server.");
        ws:broadcastText("client left the server.");
    }
}
