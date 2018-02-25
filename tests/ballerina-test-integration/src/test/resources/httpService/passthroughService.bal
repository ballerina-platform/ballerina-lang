import ballerina.net.http;

@http:configuration {basePath:"/passthrough"}
service<http> passthrough {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource passthrough (http:Connection conn, http:InRequest inRequest) {
        endpoint<http:HttpClient> nyseEP {
            create http:HttpClient("http://localhost:9090", {});
        }
        http:OutRequest clientRequest = {};
        var clientResponse, _ = nyseEP.get("/nyseStock/stocks", clientRequest);
        _ = conn.forward(clientResponse);
    }
}

@http:configuration {basePath:"/nyseStock"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["GET"]
    }
    resource stocks (http:Connection conn, http:InRequest inReq) {
        http:OutResponse res = {};
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}