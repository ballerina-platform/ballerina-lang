package samples.message_passthrough;

import ballerina.lang.message;
import ballerina.net.http as http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        ballerina.net.http:HTTPConnector nyseEP = new ballerina.net.http:HTTPConnector("http://localhost:8280/exchange/nyse/", 100);

        message response;
        response = ballerina.net.http:HTTPConnector.post(nyseEP, "/us", m);
        reply response;
    }
}
