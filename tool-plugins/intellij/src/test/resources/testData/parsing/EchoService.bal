import ballerina/http;
import ballerina/http.response;
import ballerina/http.request;

@http:configuration {basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Request req, http:Response res) {
        string payload = request:getTextPayload(req);
        response:setTextPayload(res, payload);
        response:send(res);
    }
}
