import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/swagger;

endpoint http:Listener ep0 {
    port: 9090
};

@swagger:ServiceInfo {
    title: "serviceName",
    serviceVersion: "1.0.0"
}
@http:ServiceConfig {
    basePath: "/"
}
service serviceName bind ep0 {

    @swagger:ResourceInfo {
        summary: "Get operation for the path /user"
    }
    @http:ResourceConfig {
        methods:["GET"],
        path:"/user"
    }
    resource1 (endpoint outboundEp, http:Request _resource1Req) {

    }

    @swagger:ResourceInfo {
        summary: "Put operation for the path /user"
    }
    @http:ResourceConfig {
        methods:["PUT"],
        path:"/user"
    }
    resource2 (endpoint outboundEp, http:Request _resource2Req) {

    }

    @swagger:ResourceInfo {
        summary: "Post operation for the path /user"
    }
    @http:ResourceConfig {
        methods:["POST"],
        path:"/user"
    }
    resource3 (endpoint outboundEp, http:Request _resource3Req) {

    }

}
