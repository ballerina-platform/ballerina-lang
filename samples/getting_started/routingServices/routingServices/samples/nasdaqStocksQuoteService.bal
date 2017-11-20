package routingServices.samples;

import ballerina.net.http;

@http:configuration {basePath:"/nasdaqStocks"}
service<http> nasdaqStocksQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:Request req, http:Response res) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        res.send();
    }
}
