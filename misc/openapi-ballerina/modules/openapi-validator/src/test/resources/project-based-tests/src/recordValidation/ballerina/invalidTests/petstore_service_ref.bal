import ballerina/http;

listener http:Listener ep1 = new(9090, config = {host: "localhost"});
@openapi:ServiceInfo {
    contract: "/resources/petstore.yaml",
    excludeTags: [ "user"]
}
@http:ServiceConfig {
        basePath: "/v2"
}
service petstore_service_bal on ep1 {
 @http:ResourceConfig {
     methods:["POST"],
     path:"/pet",
     body:"body"
 }
 resource function addPet (http:Caller caller, http:Request req,  Pet  body) returns error? {

 }

}
