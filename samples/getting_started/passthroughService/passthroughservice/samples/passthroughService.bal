package passthroughservice.samples;

import ballerina.net.http;

@http:BasePath {value:"/passthrough"}
service passthrough {

    @http:GET{}
    @http:Path {value:"/"}
    resource passthrough (message m) {
        //a new client connector is instantiated with the backend endpoint.
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090");
        //the get action of the connector makes an HTTP GET to the backend and returns the response as a message object.
        message response = http:ClientConnector.get(nyseEP, "/nyseStock/stocks", m);
        //the message returned from the backend call is sent out as a HTTP response.
        reply response;

    }

}
