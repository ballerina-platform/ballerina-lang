import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});

listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});

@openapi:ServiceInfo {
    contract: "/var/folders/mz/xmjfm34s1n99v74_jtsbsdcw0000gn/T/openapi-cmd2185287776867704749/src/oneOfModule/resources/oneof-petstore.yaml",
    tags: [ ]
}
@http:ServiceConfig {
    basePath: "/v1"
}

service oneOfService on ep0, ep1 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/pets",
        body:"body"
    }
    resource function getPets (http:Caller caller, http:Request req,  Dog| Cat| any  body) returns error? {

    }

}