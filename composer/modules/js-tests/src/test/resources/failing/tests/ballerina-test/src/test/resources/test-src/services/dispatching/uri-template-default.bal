import ballerina.net.http;

@http:configuration {
    allowCredentials : true
}
service<http> serviceName {

    @http:resourceConfig {
        methods:["GET"],
        path:""
    }
    resource test1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"dispatched to service name"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

@http:configuration {
    basePath:""
}
service<http> serviceEmptyName {

    resource test1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"dispatched to empty service name"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/*"
    }
    resource proxy (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"dispatched to a proxy service"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

service<http> serviceWithNoAnnotation {

    resource test1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json responseJson = {"echo":"dispatched to a service without an annotation"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}
