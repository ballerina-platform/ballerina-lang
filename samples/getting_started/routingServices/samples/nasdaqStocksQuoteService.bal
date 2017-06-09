package routingServices.samples;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.system;
@http:BasePath{value:"/nasdaqStocks"} 
service nasdaqStocksQuote {
    
    @http:POST{}
    resource stocks (message m) {
        message response = {};
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        messages:setJsonPayload(response, payload);
		system:log(3, "NASDAQ Stock Quote Service was invoked, responding to caller.");
        reply response;
        
    }
    
}
