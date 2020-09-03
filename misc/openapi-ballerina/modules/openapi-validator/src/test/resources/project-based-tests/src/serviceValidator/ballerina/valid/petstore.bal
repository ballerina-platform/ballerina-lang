import ballerina/http;

listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});
listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});

@http:ServiceConfig {
    basePath: "/v1"
}

service petstore on ep0, ep1 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets"
    }
    resource function listPets (http:Caller caller, http:Request req) returns error? {

    }
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
    resource function showPetById (http:Caller caller, http:Request req,  string petId) returns error? {

    }
    @http:ResourceConfig {
        methods:["GET"],
        path:"/user"
    }
    resource function showUserById (http:Caller caller, http:Request req) returns error? {

    }
}
