import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;

@http:configuration {basePath:"/data"}
service<http> dataService {

    @http:POST {}
    @http:Path {value:"text/{id}"}
    resource sendToConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:pushTextToConnection(id, messages:getStringPayload(m));
        message res = {};
        reply res;
    }

    @http:POST {}
    @http:Path {value:"binary/{id}"}
    resource sendBinaryToConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:pushBinaryToConnection(id, messages:getBinaryPayload(m));
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
