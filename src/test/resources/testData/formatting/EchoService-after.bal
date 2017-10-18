import ballerina.net.http;
import ballerina.net.http.response;
import ballerina.net.http.request;

@http:configuration {basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["GET", "POST"],
        path:"/"
    }
    resource echo (http:Request req, http:Response res) {
        string payload = request:getStringPayload(req);
        response:setStringPayload(res, payload);
        response:send(res);
    }
}
