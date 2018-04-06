import ballerina/lang.messages;
import ballerina/http;
import ballerina/net.ws;

@http:configuration {basePath:"/data"}
service<http> dataService {

    @http:POST {}
    @http:Path {value:"/{id}"}
    resource sendToConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:pushTextToConnection(id, messages:getStringPayload(m));
        message res = {};
        reply res;
    }

    @http:GET {}
    @http:Path {value:"/rm/{id}"}
    resource deleteConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:removeStoredConnection(id);
        message res = {};
        reply res;
    }

    @http:GET {}
    @http:Path {value:"/close/{id}"}
    resource closeConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:closeStoredConnection(id);
        message res = {};
        reply res;
    }
}
