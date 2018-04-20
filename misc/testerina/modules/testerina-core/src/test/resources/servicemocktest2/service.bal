import ballerina/http;
import ballerina/io;

endpoint http:Listener portalEP {
    port: 9093
};

@http:ServiceConfig {
      basePath: "/portal"
}
service PortalService bind portalEP {

    @http:ResourceConfig {
        path:"events"
    }
    getEvents (endpoint caller, http:Request req) {
        _ = caller -> respond(hadleGetEvents());
    }
}