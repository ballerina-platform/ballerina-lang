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
        summary: ""
    }
    @http:ResourceConfig {
        methods:["GET"],
        path:"/user"
    }
    resource1 (endpoint outboundEp, http:Request _resource1Req) {

    }

    @swagger:ResourceInfo {
        summary: ""
    }
    @http:ResourceConfig {
        methods:["POST"],
        path:"/user"
    }
    resource2 (endpoint outboundEp, http:Request _resource2Req) {

    }

}
