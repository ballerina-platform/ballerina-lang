package src.test.resources.servicemocktest2;

import ballerina.net.http;
import ballerina.io;

endpoint http:ServiceEndpoint portalEP {
    port: 9090
};

@http:serviceConfig {
      basePath: "/portal"
}
service<http:Service> PortalService bind portalEP {

    @http:resourceConfig {
        methods:["GET"],
        path:"events"
    }
    getEvents (endpoint client, http:Request req) {
        _ = client -> respond(hadleGetEvents());
    }
}