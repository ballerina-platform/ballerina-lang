package routingServices.samples;

import ballerina.lang.messages;
import ballerina.net.http;

@http:BasePath {value:"/nasdaqStocks"}
service nasdaqStocksQuote {
    
    @http:POST{}
    resource stocks (message m) {
        message response = {};
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        messages:setJsonPayload(response, payload);
        reply response;
        
    }
    
}
