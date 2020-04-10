import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});

listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});

@openapi:ServiceInfo {
    contract: "/resources/petstore.yaml",
    tags: [ ]
}
@http:ServiceConfig {
    basePath: "/v1"
}

service GigaClient on ep0, ep1 {

}