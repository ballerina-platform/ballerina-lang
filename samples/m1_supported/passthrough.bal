package samples.message.passthrough;

import ballerina.lang.message;
import ballerina.net.http as http;


@BasePath ("/passthrough")
service PassthroughService {

    @POST
    @Path ("/stocks")
    resource passthrough (message m) {
        http:HTTPConnector nyseEP = new http:HTTPConnector("http://localhost:8280/services", 100);
        message response;
        response = http:HTTPConnector.post(nyseEP, "/NYSEProxy", m);
        reply response;
    }
}
