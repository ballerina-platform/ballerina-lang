import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});
listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});
@openapi:ServiceInfo {
    contract: "resources/pathParameter.yaml",
    tags: [ ]
}
@http:ServiceConfig {
    basePath: "/v1"
}
service pathService on ep0, ep1 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets/{limits}"
    }
    resource function listPets (http:Caller caller, http:Request req,  int limits02) returns error? {
    }
    @http:ResourceConfig {
        methods:["POST"],
        path:"/pets/{limits}"
    }
    resource function resource_post_pets_limits (http:Caller caller, http:Request req) returns error? {
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets/{petId}"
    }
    resource function showPetById (http:Caller caller, http:Request req,  string petId) returns error? {

    }
}
