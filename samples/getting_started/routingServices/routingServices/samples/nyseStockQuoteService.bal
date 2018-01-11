package routingServices.samples;

import ballerina.net.http;

@http:configuration {basePath:"/nyseStocks"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:Connection con, http:InRequest req) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = con.respond(res);
    }
}
