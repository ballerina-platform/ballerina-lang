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
    user (endpoint outboundEp, http:Request _userReq) {

    }

}
