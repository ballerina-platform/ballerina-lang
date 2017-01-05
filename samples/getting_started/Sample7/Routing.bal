package org.wso2.ballerina.sample;

import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system;

@BasePath ("/cbr")
service ContentBasedRouting {

    @POST
    resource cbrResource (message m) {

        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090/NYSEStocks", 30000);
        http:HTTPConnector nasdaqEP = new http:HTTPConnector("http://localhost:9090/NASDAQStocks", 60000);

        message response;
        json jsonMsg;
        string nameString;
        string nyseString;

        nyseString = "NYSE";

        jsonMsg = message:getJsonPayload(m);
        nameString = json:getString(jsonMsg, "$.name");

        if (nameString == nyseString) {
            response = http:HTTPConnector.post(nyseEP, "/", m);
        } else {
            response = http:HTTPConnector.post(nasdaqEP, "/", m);
        }

        reply response;
    }
}

@BasePath ("/hbr")
service ContentBasedRouting {

    @POST
    resource cbrResource (message m) {

        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090/NYSEStocks", 30000);
        http:HTTPConnector nasdaqEP = new http:HTTPConnector("http://localhost:9090/NASDAQStocks", 60000);

        message response;
        string nameString;
        string nyseString;

        nyseString = "NYSE";


        nameString = message:getHeader(m, "name");

        if (nameString == nyseString) {
            response = http:HTTPConnector.post(nyseEP, "/", m);
        } else {
            response = http:HTTPConnector.post(nasdaqEP, "/", m);
        }

        reply response;
    }
}

@BasePath("/NYSEStocks")
service NYSEStockQuote {

    @POST
    resource stocks (message m) {

        message response;
        json payload;

        payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);

        reply response;
    }
}

@BasePath("/NASDAQStocks")
service NASDAQStockQuote {

    @POST
    resource stocks (message m) {

        message response;
        json payload;

        payload = `{"exchange":"nasdaq", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);

        reply response;
    }
}