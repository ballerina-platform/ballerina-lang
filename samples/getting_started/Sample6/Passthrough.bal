package org.wso2.ballerina.sample;

import ballerina.lang.message;
import ballerina.net.http;

@BasePath ("/passthrough")
service PassthroughService {

    @GET
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090", 100);
        message response;
        response = http:HTTPConnector.get(nyseEP, "/NYSEStocks", m);
        reply response;
    }
}

@BasePath("/NYSEStocks")
service NYSEStockQuote {

    @GET
    resource stocks (message m) {
        message response;
        json payload;

        payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);
        reply response;
    }
}