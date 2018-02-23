package restfulservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/orderservice"}
service<http> OrderMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource orders (http:Connection conn, http:InRequest req) {
        json payload = {};
        string httpMethod = req.method;
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Order":{"ID":"111999", "Name":"ABC123", "Description":"Sample order."}};
        } else {
            payload = {"Status":"Order is successfully added."};
        }

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}
