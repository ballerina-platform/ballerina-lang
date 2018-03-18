import ballerina.net.http;

endpoint<http:Service> passthroughEP {
    port:9090
}
endpoint<http:Client> nyseEP {
    serviceUri: "http://localhost:9090"
}

@http:serviceConfig {basePath:"/passthrough", endpoints:[passthroughEP]}
service<http:Service> passthrough {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource passthrough (http:ServerConnector conn, http:Request clientRequest) {
        var response, _ = nyseEP -> get("/nyseStock/stocks", clientRequest);
        _ = conn -> forward(response);
    }
}

@http:serviceConfig {basePath:"/nyseStock", endpoints:[passthroughEP]}
service<http:Service> nyseStockQuote {

    @http:resourceConfig {
        methods:["GET"]
    }
    resource stocks (http:ServerConnector conn, http:Request clientRequest) {
        http:Response res = {};
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}