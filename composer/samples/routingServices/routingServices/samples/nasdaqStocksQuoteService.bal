package routingServices.samples;

import ballerina/http;

@http:configuration {basePath:"/nasdaqStocks"}
service<http> nasdaqStocksQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:Connection conn, http:Request req) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}
