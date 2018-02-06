package routingServices.samples;

import ballerina.net.http;
import ballerina.net.http.response;

@http:configuration {basePath:"/nasdaqStocks"}
service<http> nasdaqStocksQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:Request req, http:Response res) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        response:setJsonPayload(res, payload);
        response:send(res);
        
    }
    
}
