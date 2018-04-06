import ballerina/http;

endpoint http:NonListeningServiceEndpoint testEP {
    port:9090
};

@http:ServiceConfig {
    cors: {
        allowCredentials: true
    }
}
service<http:Service> serviceName bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:""
    }
    test1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"dispatched to service name"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> serviceEmptyName bind testEP {

    test1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"dispatched to empty service name"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/*"
    }
    proxy (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"dispatched to a proxy service"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

service<http:Service> serviceWithNoAnnotation bind testEP {

    test1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"dispatched to a service without an annotation"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}
