import ballerina.lang.message;
import ballerina.net.http;

@BasePath ("/passthrough")
service passthrough {

    @GET
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090");
        message response;

        response = http:HTTPConnector.get(nyseEP, "/nyseStock", m);

        reply response;
    }
}

@BasePath("/nyseStock")
service nyseStockQuote {

    @GET
    resource stocks (message m) {
        message response;
        json payload;

        payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);
        reply response;
    }
}