package samples.message_passthrough;

import ballerina.lang.message;
import ballerina.net.connectors.http as http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        ballerina.net.connectors.http:HTTPConnector nyseEP = new ballerina.net.connectors.http:HTTPConnector("http://localhost:8080/exchange/nyse/", 100);

        message response;
        response = ballerina.net.connectors.http:http.post(nyseEP, "/us", m);
        reply response;
    }
}
