package restfulservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/customerservice"}
service<http> CustomerMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource customers (http:Connection conn, http:InRequest req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Customer":{"ID":"987654", "Name":"ABC PQR", "Description":"Sample Customer."}};
        } else {
            payload = {"Status":"Customer is successfully added."};
        }

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}
