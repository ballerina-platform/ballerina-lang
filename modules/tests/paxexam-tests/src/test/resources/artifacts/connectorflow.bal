package samples.message.passthrough;

import ballerina.lang.messages;
import ballerina.net.http as http;


@BasePath ("/passthrough")
service PassthroughService {

@GET
@Path ("/employees")
    resource passthrough (message m) {
            http:DummyHTTPConnector nyseEP = new http:DummyHTTPConnector("http://empdir-nuwanbando.herokuapp.com");
            message response;
            response = http:DummyHTTPConnector.get(nyseEP, "/employees", m);
            reply response;
            }
            }
