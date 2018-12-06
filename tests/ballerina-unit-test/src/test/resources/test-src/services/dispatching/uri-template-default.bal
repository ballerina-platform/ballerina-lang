import ballerina/http;

listener http:MockListener testEP = new(9090);
listener http:MockListener mockEP = new(9091);

@http:ServiceConfig {
    cors: {
        allowCredentials: true
    }
}
service serviceName on testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:""
    }
    resource function test1 (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"dispatched to service name"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/"
}
service serviceEmptyName on testEP {

    resource function test1(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"dispatched to empty service name"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        path:"/*"
    }
    resource function proxy(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"dispatched to a proxy service"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }
}

service serviceWithNoAnnotation on testEP {

    resource function test1(http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"dispatched to a service without an annotation"};
        res.setJsonPayload(responseJson);
        _ = caller->respond(res);
    }
}

service on mockEP {

    resource function testResource(http:Caller caller, http:Request req) {
        _ = caller->respond({"echo":"dispatched to service that doesn't have a name"});
    }
}
