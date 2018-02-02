package restfulservice.samples;

import ballerina.lang.strings;
import ballerina.net.http;
import ballerina.net.http.request;
import ballerina.net.http.response;

@http:configuration {basePath:"/orderservice"}
service<http> OrderMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource orders (http:Request req, http:Response res) {
        json payload = {};
        string httpMethod = request:getMethod(req);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Order": {"ID": "111999", "Name": "ABC123","Description": "Sample order."}};

        }
        else {
            payload = {"Status":"Order is successfully added."};

        }
        response:setJsonPayload(res, payload);
        response:send(res);
    }

}