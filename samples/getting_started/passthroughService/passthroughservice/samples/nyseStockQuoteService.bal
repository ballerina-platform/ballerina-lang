package passthroughservice.samples;

import ballerina.lang.messages;
import ballerina.net.http;

@http:configuration {basePath:"/nyseStock"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["GET"]
    }
    resource stocks (message m) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

}