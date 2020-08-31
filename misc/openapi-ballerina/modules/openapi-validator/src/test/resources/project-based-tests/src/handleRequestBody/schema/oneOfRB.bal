import ballerina/http;
import ballerina/openapi;

type Dog record {
     boolean bark;
     string breed;
};
type Cat record {
     boolean hunts;
     int age;
};

listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});
listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});
@openapi:ServiceInfo {
    contract: "resources/oneOfRB.yaml",
    tags: [ ]
}
@http:ServiceConfig {
    basePath: "/v1"
}
service oneOf_rb on ep0, ep1 {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/pets",
        body:"body"
    }
    resource function resource_post_pets (http:Caller caller, http:Request req,  Cat| Dog| any  body) returns error? {

    }
}
