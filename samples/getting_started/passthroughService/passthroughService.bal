import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.net.http;

@BasePath ("/passthrough")
service passthrough {

    @GET
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090");
        message response;
        json jsonPayload;
        string s;

        response = http:HTTPConnector.get(nyseEP, "/nyseStock", m);

        jsonPayload = message:getJsonPayload(response);
        s = json:getString(jsonPayload, "$.name");
        system:println(s);

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
