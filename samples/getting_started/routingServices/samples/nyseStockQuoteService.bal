package routingServices.samples;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.system;

@http:BasePath {value:"/nyseStocks"}
service nyseStockQuote {
    
    @http:POST{}
    resource stocks (message m) {
        message response = {};
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        messages:setJsonPayload(response, payload);
		system:log(3, "NYSE Stock Quote Service was invoked, responding to caller.");
        reply response;
        
    }
    
}
