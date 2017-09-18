import ballerina.net.http;
import ballerina.net.http.response as res;
import ballerina.net.http.request as req;

@http:configuration {basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Request request, http:Response response) {
        string payload = req:getStringPayload(request);
        res:setStringPayload(response, payload);
        res:send(response);
    }
    
}
