import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/swagger;

listener http:Listener ep0 = new(9090);

@swagger:ServiceInfo {
    title: "serviceName",
    serviceVersion: "1.0.0"
}
@http:ServiceConfig {
    basePath: "/"
}
service serviceName on ep0 {

    @swagger:ResourceInfo {
        summary: ""
    }
    @http:ResourceConfig {
        methods:["GET"],
        path:"/user"
    }
    resource function resource1 (http:Caller outboundEp, http:Request _resource1Req) {

    }

    @swagger:ResourceInfo {
        summary: ""
    }
    @http:ResourceConfig {
        methods:["POST"],
        path:"/user"
    }
    resource function resource2 (http:Caller outboundEp, http:Request _resource2Req) {

    }

}
