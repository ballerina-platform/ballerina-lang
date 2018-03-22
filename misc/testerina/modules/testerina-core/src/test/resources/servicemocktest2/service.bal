package servicemocktest2;

import ballerina/net.http;
import ballerina/io;

endpoint http:ServiceEndpoint portalEP {
    port: 9090
};

@http:ServiceConfig {
      basePath: "/portal"
}
service<http:Service> PortalService bind portalEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"events"
    }
    getEvents (endpoint client, http:Request req) {
        _ = client -> respond(hadleGetEvents());
    }
}