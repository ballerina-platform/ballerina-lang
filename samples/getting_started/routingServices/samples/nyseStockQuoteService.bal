package routingServices.samples;
import ballerina.lang.messages;

@http:BasePath ("/nyseStocks")
service nyseStockQuote {
    
    @http:POST
    resource stocks (message m) {
        message response = {};
        json payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        messages:setJsonPayload(response, payload);
        reply response;
        
    }
    
}
