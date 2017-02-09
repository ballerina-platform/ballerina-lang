import ballerina.net.http;
import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.system;

@BasePath ("/cbr")
service contentBasedRouting {

    @POST
    resource cbrResource (message m) {

        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090/nyseStocks");
        http:HTTPConnector nasdaqEP = new http:HTTPConnector("http://localhost:9090/nasdaqStocks");

        message response;
        json jsonMsg;
        string nameString;
        string nyseString;

        nyseString = "nyse";

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
service headerBasedRouting {

    @GET
    resource cbrResource (message m) {

        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090/nyseStocks");
        http:HTTPConnector nasdaqEP = new http:HTTPConnector("http://localhost:9090/nasdaqStocks");

        message response;
        string nameString;
        string nyseString;

        nyseString = "nyse";


        nameString = message:getHeader(m, "name");

        if (nameString == nyseString) {
            response = http:HTTPConnector.post(nyseEP, "/", m);
        } else {
            response = http:HTTPConnector.post(nasdaqEP, "/", m);
        }

        reply response;
    }
}

@BasePath("/nyseStocks")
service nyseStockQuote {

    @POST
    resource stocks (message m) {

        message response;
        json payload;

        payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);

        reply response;
    }
}

@BasePath("/nasdaqStocks")
service nasdaqStocksQuote {

    @POST
    resource stocks (message m) {

        message response;
        json payload;

        payload = `{"exchange":"nasdaq", "name":"IBM", "value":"127.50"}`;
        message:setJsonPayload(response, payload);

        reply response;
    }
}
