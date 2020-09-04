import ballerina/http;
import ballerina/openapi;

type Pet record {
     int id;
     int name;
     string tag;
     string 'type;
};

listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});

listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});

@openapi:ServiceInfo {
    contract: "resources/petstore.yaml",
    tags: [ ]
}
@http:ServiceConfig {
    basePath: "/v1"
}
service petstore on ep0, ep1 {
    @http:ResourceConfig {
            methods:["POST"],
            path:"/pets/{petId}",
            body: "body"
            }
        resource function showPetById (http:Caller caller, http:Request req, int body) returns error? {
        }
    }
