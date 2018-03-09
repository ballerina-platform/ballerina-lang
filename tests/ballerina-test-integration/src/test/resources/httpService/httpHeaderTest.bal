import ballerina.net.http;

@http:configuration {basePath:"/product"}
service<http> headerService {

    resource value (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090", {});
        }
        //http:Request clientRequest = {};
        http:InResponse clientResponse = {};
        req.setHeader("core", "aaa");
        req.addHeader("core", "bbb");
        clientResponse, _ = endPoint.get("/sample/stocks", req);
        _ = conn.forward(clientResponse);
    }

    resource id (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090", {});
        }
        var clientResponse, _ = endPoint.forward("/sample/customers", req);
        string[] headers = clientResponse.getHeaders("person");
        json payload = {header1:headers[0] , header2:headers[1]};
        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}

@http:configuration {basePath:"/sample"}
service<http> quoteService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource company (http:Connection conn, http:Request req) {
        string[] headers = req.getHeaders("core");
        json payload = {header1:headers[0] , header2:headers[1]};

        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource product (http:Connection conn, http:Request req) {
        http:OutResponse res = {};
        res.setHeader("person", "kkk");
        res.addHeader("person", "jjj");
        _ = conn.respond(res);
    }
}
