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

    @swagger:MultiResourceInfo {
        resourceInformation: {
            "GET":{
                summary: "Get operation for the path /user"
            },
            "POST":{
                summary: "Post operation for the path /user"
            }
        }
    }
    @http:ResourceConfig {
        methods:["GET", "POST"],
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

}
