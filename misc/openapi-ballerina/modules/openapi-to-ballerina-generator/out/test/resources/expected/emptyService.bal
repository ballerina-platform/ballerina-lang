import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/openapi;

listener http:Listener ep0 = new(9090);

@openapi:ServiceInfo {
    title: "serviceName",
    serviceVersion: "1.0.0"
}
@http:ServiceConfig {
    basePath: "/"
}
service serviceName on ep0 {

}
