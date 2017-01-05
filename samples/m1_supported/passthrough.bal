package samples.message.passthrough;

import ballerina.lang.message;
import ballerina.net.http as http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9090");
        message response;
        response = http:HTTPConnector.post(nyseEP, "/NYSEStocks", m);
        reply response;
    }
}

@BasePath("/NYSEStocks")
service NYSEStockQuote {
  @POST
  @Path("/*")
  resource stocks (message m) {
    message response;
    json payload;
    response = new message;
    payload = `{"exchange":"nyse", "name":"IBM", "value":"127.50"}`;
    message:setJsonPayload(response, payload);
    reply response;
  }
}
