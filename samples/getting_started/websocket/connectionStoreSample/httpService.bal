import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.ws;

@http:config {basePath:"/storeInfo"}
service<http> dataService {

    @http:POST {}
    @http:Path {value:"/{id}"}
    resource sendToConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:pushTextToConnection(id, messages:getStringPayload(m));
        message res = {};
        messages:setStringPayload(res, "done");
        reply res;
    }

    @http:GET {}
    @http:Path {value:"/rm/{id}"}
    resource removeConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:removeStoredConnection(id);
        message res = {};
        messages:setStringPayload(res, "done");
        reply res;
    }

    @http:GET {}
    @http:Path {value:"/close/{id}"}
    resource closeConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:closeStoredConnection(id);
        message res = {};
        messages:setStringPayload(res, "done");
        reply res;
    }
}
