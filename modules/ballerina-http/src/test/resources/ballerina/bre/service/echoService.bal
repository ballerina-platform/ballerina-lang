import ballerina.net.http;
import ballerina.net.http.response;

@http:configuration{basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/message"
    }
    resource echo (http:Request req, http:Response res) {
        response:send(res);
    }
}
