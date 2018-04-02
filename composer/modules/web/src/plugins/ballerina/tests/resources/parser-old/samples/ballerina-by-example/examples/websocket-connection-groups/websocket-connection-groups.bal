import ballerina/lang.jsons;
import ballerina/lang.messages;
import ballerina/http;
import ballerina/net.ws;

@http:configuration {basePath:"/groups"}
@ws:WebSocketUpgradePath {value:"/ws"}
service<ws> echoServer {
    int i = 0;
    string groupEven = "even";
    string groupOdd = "odd";

    @ws:OnOpen {}
    resource onOpen(message m) {
        if (i % 2 == 0) {
            ws:addConnectionToGroup(groupEven);
        } else {
            ws:addConnectionToGroup(groupOdd);
        }
        i = i + 1;
    }

    @ws:OnTextMessage {}
    resource onTextMessage(message m) {
        json jsonPayload = messages:getJsonPayload(m);
        string command = jsons:toString(jsonPayload["command"]);
        string groupName = jsons:toString(jsonPayload["group"]);
        string msg = jsons:toString(jsonPayload["msg"]);

        if ("send" == command) {
            // Broadcast text to given connection group.
            ws:pushTextToGroup(groupName, msg);
        } else if ("remove" == command) {
            // Remove connection from the mentioned group.
            ws:removeConnectionFromGroup(groupName);
        } else if ("removeGroup" == command) {
            // Remove the connection group.
            ws:removeConnectionGroup(groupName);
        } else if ("closeGroup" == command) {
            // Close the connection group.
            ws:closeConnectionGroup(groupName);
        }
    }

    @ws:OnClose {}
    resource onClose(message m) {
        // Broadcast text to all connected clients.
        ws:broadcastText("Client left");
    }
}

