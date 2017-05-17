import ballerina.lang.messages;
import ballerina.net.ws;
import ballerina.net.http;

@http:BasePath {value:"/groupInfo"}
service oddEvenHttpService {

    string evenWebSocketConnectionGroupName = "evenGroup";
    string oddWebSocketConnectionGroupName = "oddGroup";

    @http:POST {}
    @http:Path {value:"/even"}
    resource evenSend (message m) {
        ws:pushTextToGroup(evenWebSocketConnectionGroupName, messages:getStringPayload(m));
        message res = {};
        messages:setStringPayload(res, "done");
        reply res;
    }

    @http:POST {}
    @http:Path {value:"/odd"}
    resource oddSend (message m) {
        ws:pushTextToGroup(oddWebSocketConnectionGroupName, messages:getStringPayload(m));
        message res = {};
        messages:setStringPayload(res, "done");
        reply res;
    }

    @http:GET {}
    @http:Path {value:"/rm-even"}
    resource deleteEven (message m) {
        ws:removeConnectionGroup(evenWebSocketConnectionGroupName);
        message res = {};
        messages:setStringPayload(res, "done");
        reply res;
    }

    @http:GET {}
    @http:Path {value:"/rm-odd"}
    resource deleteOdd (message m) {
        ws:removeConnectionGroup(oddWebSocketConnectionGroupName);
        message res = {};
        messages:setStringPayload(res, "done");
        reply res;
    }
}
