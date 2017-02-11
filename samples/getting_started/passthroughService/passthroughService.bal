import ballerina.lang.message;
import ballerina.net.http;

@BasePath ("/passthrough")
service passthrough {

    @GET
    resource passthrough (message m) {
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.get(nyseEP, "/nyseStock", m);
        reply response;
    }
}

@BasePath("/nyseStock")
service nyseStockQuote {

    @GET
    resource stocks (message m) {
        json payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        message response = {};
        message:setJsonPayload(response, payload);
        reply response;
    }
}
