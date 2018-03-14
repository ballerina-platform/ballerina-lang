package src.test.resources.servicemocktest;

import ballerina.net.http;
import ballerina.io;

endpoint<http:Service> portalEP {
    port: 9090
}

@http:serviceConfig {
    endpoints:[portalEP], basePath: "/portal"
}
service<http:Service> PortalService {

    @http:resourceConfig {
        methods:["GET"],
        path:"events"
    }
    resource getEvents (http:ServerConnector conn,http:Request req) {
        _ = conn -> respond(hadleGetEvents());
    }
}