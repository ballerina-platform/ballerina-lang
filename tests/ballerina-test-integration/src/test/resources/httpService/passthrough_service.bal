import ballerina.net.http;

endpoint http:ServiceEndpoint passthroughEP {
    port:9090
};

endpoint http:ClientEndpoint nyseEP {
    targets: [{uri:"http://localhost:9090"}]
};

@http:serviceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService bind passthroughEP {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint outboundEP, http:Request clientRequest) {
        var response, _ = nyseEP -> get("/nyseStock/stocks", clientRequest);
        _ = outboundEP -> forward(response);
    }
}

@http:serviceConfig {basePath:"/nyseStock"}
service<http:Service> nyseStockQuote bind passthroughEP {

    @http:resourceConfig {
        methods:["GET"]
    }
    stocks (endpoint outboundEP, http:Request clientRequest) {
        http:Response res = {};
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }
}