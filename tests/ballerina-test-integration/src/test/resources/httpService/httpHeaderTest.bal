import ballerina.net.http;

@http:configuration {basePath:"/product"}
service<http> headerService {

    resource value (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090", {});
        }
        http:OutRequest clientRequest = {};
        http:InResponse clientResponse = {};
        clientRequest.setHeader("core", "aaa");
        clientRequest.addHeader("core", "bbb");
        clientResponse, _ = endPoint.doGet("/sample/stocks", clientRequest);
        _ = conn.forward(clientResponse);
    }

    resource id (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090", {});
        }
        var clientResponse, _ = endPoint.doForward("/sample/customers", req);
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
    resource company (http:Connection conn, http:InRequest req) {
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
    resource product (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setHeader("person", "kkk");
        res.addHeader("person", "jjj");
        _ = conn.respond(res);
    }
}
