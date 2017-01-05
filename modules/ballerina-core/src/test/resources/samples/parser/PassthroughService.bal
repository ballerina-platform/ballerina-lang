package samples.message_passthrough;

import ballerina.lang.message;
import ballerina.net.http as http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:8280/exchange/nyse/");

        message response;
        response = http:HTTPConnector.post(nyseEP, "/us", m);
        reply response;
    }
}
