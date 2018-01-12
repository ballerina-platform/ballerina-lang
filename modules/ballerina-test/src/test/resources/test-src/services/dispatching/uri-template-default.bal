import ballerina.net.http;

@http:configuration {
    allowCredentials : true
}
service<http> serviceName {

    @http:resourceConfig {
        methods:["GET"],
        path:""
    }
    resource test1 (http:Request req, http:Response res) {
        json responseJson = {"echo":"dispatched to service name"};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }
}

@http:configuration {
    basePath:""
}
service<http> serviceEmptyName {

    resource test1 (http:Request req, http:Response res) {
        json responseJson = {"echo":"dispatched to empty service name"};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }
}

service<http> serviceWithNoAnnotation {

    resource test1 (http:Request req, http:Response res) {
        json responseJson = {"echo":"dispatched to a service without an annotation"};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }
}
