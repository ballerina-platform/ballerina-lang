import ballerina.lang.message;
import ballerina.net.http;

@BasePath ("/passthrough")
service passthroughService {

    @GET
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090");
        message response;
        string httpMethod;

        httpMethod = http:getMethod(m);

        response = http:HTTPConnector.execute(nyseEP, httpMethod, "/nyseStock", m);
        reply response;
    }
}

@BasePath("/nyseStock")
service nyseStockQuoteService {

    @GET
    resource stocks (message m) {
        message response;
        json payload;

        payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);
        reply response;
    }
}