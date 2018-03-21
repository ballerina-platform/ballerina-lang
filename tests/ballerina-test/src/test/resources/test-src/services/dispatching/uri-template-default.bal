import ballerina/net.http;
import ballerina/net.http.mock;

endpoint<mock:NonListeningService> testEP {
    port:9090
}

@http:serviceConfig {
    endpoints:[testEP],
    cors: {
        allowCredentials: true
    }
}
service<http:Service> serviceName {

    @http:resourceConfig {
        methods:["GET"],
        path:""
    }
    resource test1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"dispatched to service name"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"",
    endpoints:[testEP]
}
service<http:Service> serviceEmptyName {

    resource test1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"dispatched to empty service name"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/*"
    }
    resource proxy (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"dispatched to a proxy service"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    endpoints:[testEP]
}
service<http:Service> serviceWithNoAnnotation {

    resource test1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"dispatched to a service without an annotation"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}
