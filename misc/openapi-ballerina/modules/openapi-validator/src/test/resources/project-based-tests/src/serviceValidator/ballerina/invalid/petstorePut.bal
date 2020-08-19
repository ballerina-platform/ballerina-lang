import ballerina/http;
import ballerina/openapi;

type Pet record {
     int id;
     string name02;
     string tag;
     string 'type;
};
type Dog record {
    *Pet;
     boolean bark;
};
type Error record {
     int code;
     string message;
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

service putService on ep0, ep1 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/pets",
        body:"body"
    }
    resource function resource_post_pets (http:Caller caller, http:Request req,  Pet  body) returns error? {

    }

    @http:ResourceConfig {
        methods:["PUT"],
        path:"/user",
        body:"body"
    }
    resource function resource_put_user (http:Caller caller, http:Request req,  Pet  body) returns error? {

    }

}
