package passthroughservice.samples;

import ballerina.lang.messages;
import ballerina.net.http;

@http:config {basePath:"/nyseStock"}
service<http> nyseStockQuote {

    @http:GET{}
    resource stocks (message m) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

}