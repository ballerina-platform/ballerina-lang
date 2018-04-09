package servicemocktest2;

import ballerina/http;
import ballerina/io;

endpoint http:ServiceEndpoint portalEP {
    port: 9093
};

@http:ServiceConfig {
      basePath: "/portal"
}
service PortalService bind portalEP {

    @http:ResourceConfig {
        path:"events"
    }
    getEvents (endpoint client, http:Request req) {
        _ = client -> respond(hadleGetEvents());
    }
}