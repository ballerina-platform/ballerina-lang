package restfulservice.samples;

import ballerina.lang.strings;
import ballerina.net.http;

@http:configuration{basePath:"/customerservice"}
service<http> CustomerMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource customers (http:Request req, http:Response res) {
        json payload = {};
        string httpMethod = req.getMethod();
        if (strings:equalsIgnoreCase(httpMethod, "GET")) {
            payload = {"Customer": {"ID": "987654", "Name": "ABC PQR","Description": "Sample Customer."}};
        }
        else {
            payload = {"Status":"Customer is successfully added."};
        }
        res.setJsonPayload(payload);
        res.send();
    }
}