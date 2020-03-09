import ballerina/http;
import ballerina/openapi;

listener http:Listener ep3 = new(80, config = {host: "petstore.openapi.io"});

listener http:Listener ep4 = new(443, config = {host: "petstore.swagger.io"});

@openapi:ServiceInfo {
    contract: "/resources/petstore.yaml",
    tags: [ ]
}
@http:ServiceConfig {
    basePath: "/v1"
}

service GigaClient2 on ep3, ep4 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/pets"
    }
    resource function resource_post_pets (http:Caller caller, http:Request req) returns error? {

    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets/{petId}"
    }
    resource function showPetById (http:Caller caller, http:Request req) returns error? {

    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets"
    }
    resource function operation1_listPets (http:Caller caller, http:Request req) returns error? {

    }
}