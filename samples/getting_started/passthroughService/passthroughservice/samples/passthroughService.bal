package passthroughservice.samples;

import ballerina.net.http;
@http:BasePath ("/passthrough")
service passthrough {

    @http:GET
    resource passthrough (message m) {
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.get(nyseEP, "/nyseStock", m);
        reply response;

    }

}
