
import ballerina/lang.messages;
import ballerina/http;

@http:configuration {basePath:"/nasdaqStocks"}
service<http> nasdaqStocksQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (message m) {
        message response = {};
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        messages:setJsonPayload(response, payload);
        response:send(response);
        
    }
    
}
