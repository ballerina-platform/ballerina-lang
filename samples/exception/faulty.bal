package samples.exception;

import ballerina.net.http;

service FaultyService;

actor http.HttpEndpoint ex = alloc http.HttpEndpoint ("http://localhost:3333");
actor http.HttpEndpoint ey = alloc http.HttpEndpoint ("http://localhost:4444");

@GET
@Path ("/*")
resource faultyResource (message m) {
    // exception thrown by faultyFunction will be handled by ballerina runtime.
    reply faultyFunction(m);
}


function faultyFunction(message in) (message) throws exception {
    actor http.HttpEndpoint e1;
    http.setURL(e1, "http://localhost:2222");

     = new http.HttpEndpoint ("http://localhost:2222");
    message response = http.get (e1, in, endpoint.getPath(in));
    return response;
}

