
import ballerina/lang.messages;
import ballerina/http;

@http:configuration {basePath:"/nyseStocks"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (message m) {
        message response = {};
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        messages:setJsonPayload(response, payload);
        response:send(response);
        
    }
    
}
