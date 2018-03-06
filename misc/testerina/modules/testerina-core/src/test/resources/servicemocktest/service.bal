package src.test.resources.servicemocktest;

import ballerina.net.http;
import ballerina.io;

@http:configuration {
    basePath:"/portal"
}
service<http> PortalService {

    @http:resourceConfig {
        methods:["GET"],
        path:"events"
    }
    resource getEvents (http:Connection conn,http:InRequest req) {
        _ = conn.respond(hadleGetEvents());
    }
}