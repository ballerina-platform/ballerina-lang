import ballerina.lang.messages;
import ballerina.net.ws;
import ballerina.net.http;

@http:BasePath {value:"/data"}
service oddEvenService {

    string evenWebSocketConnectionGroupName = "evenGroup";
    string oddWebSocketConnectionGroupName = "oddGroup";

    // Send the message to to even group
    @http:POST {}
    @http:Path {value:"/even"}
    resource evenSend (message m) {
        ws:pushTextToGroup(evenWebSocketConnectionGroupName, messages:getStringPayload(m));
        message res = {};
        reply res;
    }

    // Send the message to to odd group
    @http:POST {}
    @http:Path {value:"/odd"}
    resource oddSend (message m) {
        ws:pushTextToGroup(oddWebSocketConnectionGroupName, messages:getStringPayload(m));
        message res = {};
        reply res;
    }

    // Remove the even group
    @http:GET {}
    @http:Path {value:"/rm-even"}
    resource deleteEven (message m) {
        ws:removeConnectionGroup(evenWebSocketConnectionGroupName);
        message res = {};
        reply res;
    }

    // Remove the odd group
    @http:GET {}
    @http:Path {value:"/rm-odd"}
    resource deleteOdd (message m) {
        ws:removeConnectionGroup(oddWebSocketConnectionGroupName);
        message res = {};
        reply res;
    }
    
}
