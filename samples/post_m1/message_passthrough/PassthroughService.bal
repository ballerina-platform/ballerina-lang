package samples.message_passthrough;

import ballerina.lang.message;
import ballerina.net.http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:9000");

        message response;
        response = http:HTTPConnector.post(nyseEP, "/services/nyse", m);
        reply response;
    }
}