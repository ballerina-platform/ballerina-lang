import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;

@http:configuration {basePath:"/data"}
service<http> dataService {

    @http:resourceConfig {
        methods:["POST"],
        path:"/{id}"
    }
    resource sendToConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:pushTextToConnection(id, messages:getStringPayload(m));
        message res = {};
        reply res;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/rm/{id}"
    }
    resource deleteConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:removeStoredConnection(id);
        message res = {};
        reply res;
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/close/{id}"
    }
    resource closeConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:closeStoredConnection(id);
        message res = {};
        reply res;
    }
}
