import ballerina.net.http;

endpoint<http:Service> headerServiceEP {
    port: 9090
}

endpoint<http:Client> stockqEP {
    serviceUri: "http://localhost:9090"
}

@http:serviceConfig {
    basePath:"/product",
    endpoints: [headerServiceEP]
}
service<http:Service> headerService {

    resource value (http:ServerConnector conn, http:Request req) {
        http:Response clientResponse = {};
        req.setHeader("core", "aaa");
        req.addHeader("core", "bbb");

        clientResponse, _ = stockqEP -> get("/sample/stocks", req);
        _ = conn -> forward(clientResponse);
    }

    resource id (http:ServerConnector conn, http:Request req) {
        var clientResponse, _ = stockqEP -> forward("/sample/customers", req);
        string[] headers = clientResponse.getHeaders("person");
        json payload = {header1:headers[0] , header2:headers[1]};
        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/sample",
    endpoints: [headerServiceEP]    
}
service<http:Service> quoteService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource company (http:ServerConnector conn, http:Request req) {
        string[] headers = req.getHeaders("core");
        json payload = {header1:headers[0] , header2:headers[1]};

        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource product (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setHeader("person", "kkk");
        res.addHeader("person", "jjj");
        _ = conn -> respond(res);
    }
}
