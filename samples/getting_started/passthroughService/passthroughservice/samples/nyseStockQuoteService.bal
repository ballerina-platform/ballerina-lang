package passthroughservice.samples;

import ballerina.lang.messages;
import ballerina.net.http;

@http:BasePath {value:"/nyseStock"}
service nyseStockQuote {

    @http:GET{}
    resource stocks (message m) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

}