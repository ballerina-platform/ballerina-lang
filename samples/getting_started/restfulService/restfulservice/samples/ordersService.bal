package restfulservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/orderservice"}
service<http> OrderMgtService {

    @http:resourceConfig {
        methods:["GET", "POST"]
    }
    resource orders (http:Connection con, http:Request req) {
        json payload = {};
        string httpMethod = req.getMethod();
        if (httpMethod.equalsIgnoreCase("GET")) {
            payload = {"Order":{"ID":"111999", "Name":"ABC123", "Description":"Sample order."}};
        } else {
            payload = {"Status":"Order is successfully added."};
        }

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = con.respond(res);
    }
}
