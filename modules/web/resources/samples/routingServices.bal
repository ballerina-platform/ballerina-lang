import ballerina.net.http;
import ballerina.lang.jsons;
import ballerina.lang.messages;

@http:BasePath {value:"/cbr"}
service contentBasedRouting {

    @http:POST{}
    resource cbrResource (message m) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        json jsonMsg = messages:getJsonPayload(m);
        string nameString = jsons:getString(jsonMsg, "$.name");
        message response = {};
        if (nameString == nyseString) {
            response = http:ClientConnector.post(nyseEP, "/", m);

        }
        else {
            response = http:ClientConnector.post(nasdaqEP, "/", m);

        }
        reply response;

    }

}

@http:BasePath {value:"/hbr"}
service headerBasedRouting {

    @http:GET{}
    resource cbrResource (message m) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        string nameString = messages:getHeader(m, "name");
        message response = {};
        if (nameString == nyseString) {
            response = http:ClientConnector.post(nyseEP, "/", m);

        }
        else {
            response = http:ClientConnector.post(nasdaqEP, "/", m);

        }
        reply response;

    }

}

@http:BasePath {value:"/nyseStocks"}
service nyseStockQuote {

    @http:POST{}
    resource stocks (message m) {
        message response = {};
        json payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
        messages:setJsonPayload(response, payload);
        reply response;

    }

}

@http:BasePath {value:"/nasdaqStocks"}
service nasdaqStocksQuote {

    @http:POST{}
    resource stocks (message m) {
        message response = {};
        json payload = `{"exchange":"nasdaq", "name":"IBM", "value":"127.50"}`;
        messages:setJsonPayload(response, payload);
        reply response;

    }

}
