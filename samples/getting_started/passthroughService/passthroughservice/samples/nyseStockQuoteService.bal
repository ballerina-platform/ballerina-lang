package passthroughservice.samples;

import ballerina.lang.messages;
import ballerina.net.http;

@http:BasePath {value:"/nyseStock"}
service nyseStockQuote {

    @http:GET{}
    resource stocks (message m) {
        //a json object is instantiated to be send back as the response payload.
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        message response = {};
        //setJsonPayload is a function in the messages package that attaches a json object to a message object.
        messages:setJsonPayload(response, payload);
        //the message containing the json payload is sent out as a HTTP response.
        reply response;

    }

}