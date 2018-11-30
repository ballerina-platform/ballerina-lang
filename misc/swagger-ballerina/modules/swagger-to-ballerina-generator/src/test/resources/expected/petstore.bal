import ballerina/http;
import ballerina/log;
import ballerina/mime;
import ballerina/swagger;

listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});

@swagger:ServiceInfo {
    title: "Swagger Petstore",
    serviceVersion: "1.0.0",
    license: {name: "MIT", url: ""},
    tags: [
        {name: "pets", description: "Pets Tag", externalDocs: {}},
        {name: "list", description: "List Tag", externalDocs: {}}
    ],
    security: [
        {name: "petstore_auth", requirements: []},
        {name: "user_auth", requirements: []}
    ]
}
@http:ServiceConfig {
    basePath: "/v1"
}
service SwaggerPetstore on ep0 {
    resource function action (http:Caller outboundEp, http:Request _actionReq) {

    }

    @swagger:ResourceInfo {
        summary: "List all pets",
        tags: ["pets","list"],
        description: "Show a list of pets in the system",
        parameters: [
            {
                name: "limit",
                inInfo: "query",
                paramType: "int",
                description: "How many items to return at one time (max 100)",
                allowEmptyValue: ""
            }
        ]
    }
    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets"
    }
    resource function listPets (http:Caller outboundEp, http:Request _listPetsReq) {

    }

    @swagger:ResourceInfo {
        summary: "Create a pet",
        tags: ["pets"]
    }
    @http:ResourceConfig {
        methods:["POST"],
        path:"/pets"
    }
    resource function resource1 (http:Caller outboundEp, http:Request _resource1Req) {

    }

    @swagger:ResourceInfo {
        summary: "Info for a specific pet",
        tags: ["pets"],
        parameters: [
            {
                name: "petId",
                inInfo: "path",
                paramType: "string",
                description: "The id of the pet to retrieve",
                required: true,
                allowEmptyValue: ""
            }
        ]
    }
    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets/{petId}"
    }
    resource function showPetById (http:Caller outboundEp, http:Request _showPetByIdReq, string petId) {

    }

}
