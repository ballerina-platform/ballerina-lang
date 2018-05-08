
import ballerina/lang.messages;
import ballerina/net.ws;
import ballerina/http;

@http:configuration {basePath:"/groupInfo"}
service<http> oddEvenHttpService {

    string evenWebSocketConnectionGroupName = "evenGroup";
    string oddWebSocketConnectionGroupName = "oddGroup";

    @http:resourceConfig {
        methods:["POST"],
        path:"/even"
    }
    resource evenSend (message m) {
        ws:pushTextToGroup(evenWebSocketConnectionGroupName, messages:getStringPayload(m));
        message res = {};
        messages:setStringPayload(res, "done");
        response:send(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/odd"
    }
    resource oddSend (message m) {
        ws:pushTextToGroup(oddWebSocketConnectionGroupName, messages:getStringPayload(m));
        message res = {};
        messages:setStringPayload(res, "done");
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/rm-even"
    }
    resource deleteEven (message m) {
        ws:removeConnectionGroup(evenWebSocketConnectionGroupName);
        message res = {};
        messages:setStringPayload(res, "done");
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/rm-odd"
    }
    resource deleteOdd (message m) {
        ws:removeConnectionGroup(oddWebSocketConnectionGroupName);
        message res = {};
        messages:setStringPayload(res, "done");
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/close-even"
    }
    resource closeEven (message m) {
        ws:closeConnectionGroup(evenWebSocketConnectionGroupName);
        message res = {};
        messages:setStringPayload(res, "done");
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/close-odd"
    }
    resource closeOdd (message m) {
        ws:closeConnectionGroup(oddWebSocketConnectionGroupName);
        message res = {};
        messages:setStringPayload(res, "done");
        response:send(res);
    }
}
