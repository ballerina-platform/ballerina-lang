import ballerina/lang.messages;
import ballerina/http;
@http:BasePath ("/passthrough")
service passthrough {

    @http:GET
    resource passthrough (message m) {
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.get(nyseEP, "/nyseStock", m);
        reply response;

    }

}
@http:BasePath ("/nyseStock")
service nyseStockQuote {

    @http:GET
    resource stocks (message m) {
        json payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        message response = {};
        messages:setJsonPayload(response, payload);
        reply response;

    }

}
