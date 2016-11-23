package samples.exception;

import ballerina.net.http;

service FaultyService;

@GET
@Path ("/*")
resource faultyResource (message m) {
    // exception thrown by faultyFunction will be handled by ballerina runtime.
    reply faultyFunction(m);
}


function faultyFunction(message in) (message) throws exception {
    actor http.HttpEndpoint e1 = new http.HttpEndpoint ("http://localhost:2222");
    message response = http.get (e1, in, "/foo");
    return response;
}

