import ballerina/http;
import ballerina/openapi;

listener http:Listener ep7 = new(80, config = {host: "petstore.openapi.io"});

listener http:Listener ep8 = new(443, config = {host: "petstore.swagger.io"});

@openapi:ServiceInfo {
    contract: "/resources/petstore2.yaml",
    tags: [ ]
}
@http:ServiceConfig {
    basePath: "/v1"
}

service GigaClient4 on ep7, ep8 {

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
