import ballerina.lang.messages;
import ballerina.net.ws;

@http:BasePath {value:"/data"}
service dataService {

    // Send the content to the connection represented by the given id
    @http:POST {}
    @http:Path {value:"/{id}"}
    resource sendToConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:pushTextToConnection(id, messages:getStringPayload(m));
        message res = {};
        reply res;
    }

    // Remove the connection represented by the given id
    @http:GET {}
    @http:Path {value:"/rm/{id}"}
    resource deleteConnection (message m, @http:PathParam {value:"id"} string id) {
        ws:removeStoredConnection(id);
        message res = {};
        reply res;
    }

}
