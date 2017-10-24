package restfulservice.samples;

import ballerina.lang.strings;
import ballerina.net.http;
import ballerina.net.http.request;
import ballerina.net.http.response;

@http:configuration{basePath:"/customerservice"}
service<http> CustomerMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource customers (http:Request req, http:Response res) {
        json payload = {};
        string httpMethod = request:getMethod(req);
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Customer": {"ID": "987654", "Name": "ABC PQR","Description": "Sample Customer."}};
        }
        else {
            payload = {"Status":"Customer is successfully added."};
        }
        response:setJsonPayload(res, payload);
        response:send(res);
    }
}